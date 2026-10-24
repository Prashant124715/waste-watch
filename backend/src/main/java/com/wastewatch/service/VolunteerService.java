package com.wastewatch.service;

import com.wastewatch.dto.volunteer.VolunteerStatusUpdateDto;
import com.wastewatch.dto.volunteer.VolunteerSummaryDto;
import com.wastewatch.entity.AppUser;
import com.wastewatch.entity.AuditLog;
import com.wastewatch.entity.Inspection;
import com.wastewatch.entity.enums.UserRole;
import com.wastewatch.entity.enums.UserStatus;
import com.wastewatch.exception.InvalidRequestException;
import com.wastewatch.exception.ResourceNotFoundException;
import com.wastewatch.repository.AuditLogRepository;
import com.wastewatch.repository.InspectionRepository;
import com.wastewatch.repository.UserRepository;
import com.wastewatch.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class VolunteerService {

    private final UserRepository userRepository;
    private final InspectionRepository inspectionRepository;
    private final AuditLogRepository auditLogRepository;

    public VolunteerService(UserRepository userRepository,
                            InspectionRepository inspectionRepository,
                            AuditLogRepository auditLogRepository) {
        this.userRepository = userRepository;
        this.inspectionRepository = inspectionRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(readOnly = true)
    public List<VolunteerSummaryDto> getAllVolunteers() {
        List<AppUser> volunteers = userRepository.findByRole(UserRole.VOLUNTEER);
        List<VolunteerSummaryDto> result = new ArrayList<>();

        for (AppUser volunteer : volunteers) {
            result.add(buildVolunteerSummary(volunteer));
        }

        return result;
    }

    @Transactional(readOnly = true)
    public VolunteerSummaryDto getVolunteerPerformance(Long volunteerId) {
        AppUser volunteer = userRepository.findById(volunteerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", volunteerId));

        if (volunteer.getRole() != UserRole.VOLUNTEER) {
            throw new InvalidRequestException("Cannot retrieve volunteer performance for non-volunteer user");
        }

        return buildVolunteerSummary(volunteer);
    }

    /**
     * Phase 14: Activate or deactivate a volunteer.
     * Prevents privilege escalation: only VOLUNTEER role users can be modified here.
     */
    @Transactional
    public VolunteerSummaryDto updateVolunteerStatus(Long volunteerId, VolunteerStatusUpdateDto dto, UserPrincipal currentUser) {
        AppUser volunteer = userRepository.findById(volunteerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", volunteerId));

        if (volunteer.getRole() != UserRole.VOLUNTEER) {
            throw new InvalidRequestException("Cannot modify non-volunteer user accounts via volunteer management");
        }

        UserStatus previousStatus = volunteer.getStatus();
        volunteer.setStatus(dto.getStatus());
        AppUser updated = userRepository.save(volunteer);

        // Record Audit Log (Phase 14 Governance)
        AppUser adminActor = currentUser != null ? userRepository.findById(currentUser.getId()).orElse(null) : null;
        AuditLog auditLog = new AuditLog(
                adminActor,
                "VOLUNTEER_STATUS_CHANGED",
                "AppUser",
                updated.getId().toString(),
                null,
                String.format("Volunteer '%s' (ID: %d) status changed from %s to %s",
                        updated.getUsername(), updated.getId(), previousStatus, dto.getStatus())
        );
        auditLogRepository.save(auditLog);

        return buildVolunteerSummary(updated);
    }

    private VolunteerSummaryDto buildVolunteerSummary(AppUser user) {
        VolunteerSummaryDto dto = new VolunteerSummaryDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setStatus(user.getStatus().name());

        List<Inspection> inspections = inspectionRepository.findByVolunteerIdOrderByInspectionDateDesc(user.getId());
        dto.setInspectionsCount(inspections.size());

        if (!inspections.isEmpty()) {
            double avg = inspections.stream().mapToInt(Inspection::getTotalScore).average().orElse(0.0);
            dto.setAverageScoreGiven(BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP).doubleValue());
            dto.setLastInspectionDate(inspections.get(0).getInspectionDate());
        } else {
            dto.setAverageScoreGiven(0.0);
            dto.setLastInspectionDate(null);
        }

        // Granular Phase 14 Statistics
        Long distinctRooms = inspectionRepository.countDistinctInspectedRoomsByVolunteerId(user.getId());
        dto.setDistinctRoomsInspected(distinctRooms != null ? distinctRooms : 0L);

        Long distinctBuildings = inspectionRepository.countDistinctInspectedBuildingsByVolunteerId(user.getId());
        dto.setDistinctBuildingsInspected(distinctBuildings != null ? distinctBuildings : 0L);

        Long violationsFlagged = inspectionRepository.countViolationsFlaggedByVolunteerId(user.getId());
        dto.setViolationsFlagged(violationsFlagged != null ? violationsFlagged : 0L);

        return dto;
    }
}
