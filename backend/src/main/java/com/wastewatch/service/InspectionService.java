package com.wastewatch.service;

import com.wastewatch.dto.inspection.InspectionRequestDto;
import com.wastewatch.dto.inspection.InspectionResponseDto;
import com.wastewatch.entity.*;
import com.wastewatch.entity.enums.InspectionStatus;
import com.wastewatch.entity.enums.ViolationStatus;
import com.wastewatch.exception.DuplicateResourceException;
import com.wastewatch.exception.ResourceNotFoundException;
import com.wastewatch.repository.*;
import com.wastewatch.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InspectionService {

    private final InspectionRepository inspectionRepository;
    private final ViolationRepository violationRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final ComplianceScoringService scoringService;

    public InspectionService(InspectionRepository inspectionRepository,
                             ViolationRepository violationRepository,
                             RoomRepository roomRepository,
                             UserRepository userRepository,
                             AuditLogRepository auditLogRepository,
                             ComplianceScoringService scoringService) {
        this.inspectionRepository = inspectionRepository;
        this.violationRepository = violationRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
        this.scoringService = scoringService;
    }

    @Transactional
    public InspectionResponseDto createInspection(InspectionRequestDto dto, UserPrincipal currentUser) {
        Room room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", dto.getRoomId()));

        AppUser volunteer = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        // Rule: Duplicate prevention - check if the same volunteer inspected this room within 30 seconds
        Optional<Inspection> lastInspection = inspectionRepository.findFirstByRoomIdAndVolunteerIdOrderByCreatedAtDesc(room.getId(), volunteer.getId());
        if (lastInspection.isPresent() && lastInspection.get().getCreatedAt() != null) {
            LocalDateTime cooldownCutoff = LocalDateTime.now().minusSeconds(30);
            if (lastInspection.get().getCreatedAt().isAfter(cooldownCutoff)) {
                throw new DuplicateResourceException("Duplicate submission detected: An inspection for room '" + 
                        room.getRoomNumber() + "' was already submitted within the last 30 seconds.");
            }
        }

        ComplianceScoringService.ScoringResult scoringResult = scoringService.calculateScore(dto);

        Inspection inspection = new Inspection();
        inspection.setRoom(room);
        inspection.setVolunteer(volunteer);
        inspection.setInspectionDate(LocalDateTime.now());
        inspection.setWetWasteSeparated(dto.getWetWasteSeparated());
        inspection.setDryWasteSeparated(dto.getDryWasteSeparated());
        inspection.setSanitaryWasteSeparated(dto.getSanitaryWasteSeparated());
        inspection.setCorrectBinsUsed(dto.getCorrectBinsUsed());
        inspection.setBinsLabelled(dto.getBinsLabelled());
        inspection.setNoMixedWaste(dto.getNoMixedWaste());
        inspection.setTotalScore(scoringResult.getTotalScore());
        inspection.setClassification(scoringResult.getClassification());
        inspection.setRemarks(dto.getRemarks() != null ? dto.getRemarks().trim() : null);
        inspection.setEvidenceImageUrl(dto.getEvidenceImageUrl());

        if (scoringResult.isViolationRequired()) {
            inspection.setStatus(InspectionStatus.FLAGGED);
        } else {
            inspection.setStatus(InspectionStatus.COMPLETED);
        }

        Inspection savedInspection = inspectionRepository.save(inspection);

        boolean violationCreated = false;
        Long violationId = null;

        if (scoringResult.isViolationRequired()) {
            Violation violation = new Violation();
            violation.setInspection(savedInspection);
            violation.setRoom(room);
            violation.setSeverity(scoringResult.getViolationSeverity());
            violation.setStatus(ViolationStatus.OPEN);
            violation.setDescription(String.format("Automatic non-compliance flag: Score was %d/100 (%s). Remarks: %s",
                    scoringResult.getTotalScore(),
                    scoringResult.getClassification().name(),
                    dto.getRemarks() != null && !dto.getRemarks().isBlank() ? dto.getRemarks().trim() : "Failed required waste segregation thresholds."));
            
            BigDecimal fine = scoringResult.getTotalScore() < 20 ? new BigDecimal("500.00") : new BigDecimal("250.00");
            violation.setFineAmount(fine);

            Violation savedViolation = violationRepository.save(violation);
            violationCreated = true;
            violationId = savedViolation.getId();
        }

        // Record Audit Log
        AuditLog auditLog = new AuditLog(
                volunteer,
                "CREATE_INSPECTION",
                "Inspection",
                savedInspection.getId().toString(),
                null,
                String.format("Audit created for Room %s (%s) with score %d/100. Violation created: %s",
                        room.getRoomNumber(), room.getBuilding().getName(), scoringResult.getTotalScore(), violationCreated)
        );
        auditLogRepository.save(auditLog);

        return InspectionResponseDto.fromEntity(savedInspection, violationCreated, violationId);
    }

    @Transactional(readOnly = true)
    public InspectionResponseDto getInspectionById(Long id, UserPrincipal currentUser) {
        Inspection inspection = inspectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inspection", "id", id));

        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !inspection.getVolunteer().getId().equals(currentUser.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Volunteers can only access their own inspection records");
        }

        Optional<Violation> violationOpt = violationRepository.findByInspectionId(id);
        boolean violationCreated = violationOpt.isPresent();
        Long violationId = violationOpt.map(Violation::getId).orElse(null);

        return InspectionResponseDto.fromEntity(inspection, violationCreated, violationId);
    }

    @Transactional(readOnly = true)
    public List<InspectionResponseDto> getAllInspections(Long roomId, Long volunteerId, Long buildingId, UserPrincipal currentUser) {
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // If not admin, restrict querying only to the current volunteer
        if (!isAdmin) {
            volunteerId = currentUser.getId();
        }

        List<Inspection> inspections;

        if (roomId != null) {
            inspections = inspectionRepository.findByRoomIdOrderByInspectionDateDesc(roomId);
            if (!isAdmin) {
                Long authVolId = currentUser.getId();
                inspections = inspections.stream()
                        .filter(i -> i.getVolunteer().getId().equals(authVolId))
                        .collect(Collectors.toList());
            }
        } else if (volunteerId != null) {
            inspections = inspectionRepository.findByVolunteerIdOrderByInspectionDateDesc(volunteerId);
        } else if (buildingId != null) {
            inspections = inspectionRepository.findByRoomBuildingIdOrderByInspectionDateDesc(buildingId);
            if (!isAdmin) {
                Long authVolId = currentUser.getId();
                inspections = inspections.stream()
                        .filter(i -> i.getVolunteer().getId().equals(authVolId))
                        .collect(Collectors.toList());
            }
        } else {
            inspections = inspectionRepository.findAllByOrderByInspectionDateDesc();
        }

        return inspections.stream().map(inspection -> {
            Optional<Violation> v = violationRepository.findByInspectionId(inspection.getId());
            return InspectionResponseDto.fromEntity(inspection, v.isPresent(), v.map(Violation::getId).orElse(null));
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InspectionResponseDto> getInspectionsByRoomId(Long roomId) {
        if (!roomRepository.existsById(roomId)) {
            throw new ResourceNotFoundException("Room", "id", roomId);
        }
        return inspectionRepository.findByRoomIdOrderByInspectionDateDesc(roomId).stream()
                .map(inspection -> {
                    Optional<Violation> v = violationRepository.findByInspectionId(inspection.getId());
                    return InspectionResponseDto.fromEntity(inspection, v.isPresent(), v.map(Violation::getId).orElse(null));
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InspectionResponseDto> getMyInspections(UserPrincipal currentUser) {
        return inspectionRepository.findByVolunteerIdOrderByInspectionDateDesc(currentUser.getId()).stream()
                .map(inspection -> {
                    Optional<Violation> v = violationRepository.findByInspectionId(inspection.getId());
                    return InspectionResponseDto.fromEntity(inspection, v.isPresent(), v.map(Violation::getId).orElse(null));
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InspectionResponseDto> getRecentInspections(UserPrincipal currentUser) {
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        List<Inspection> inspections;
        if (isAdmin) {
            inspections = inspectionRepository.findTop20ByOrderByInspectionDateDesc();
        } else {
            inspections = inspectionRepository.findTop20ByVolunteerIdOrderByInspectionDateDesc(currentUser.getId());
        }

        return inspections.stream().map(inspection -> {
            Optional<Violation> v = violationRepository.findByInspectionId(inspection.getId());
            return InspectionResponseDto.fromEntity(inspection, v.isPresent(), v.map(Violation::getId).orElse(null));
        }).collect(Collectors.toList());
    }
}
