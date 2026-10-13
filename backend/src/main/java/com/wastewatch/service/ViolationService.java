package com.wastewatch.service;

import com.wastewatch.dto.violation.ViolationResponseDto;
import com.wastewatch.dto.violation.ViolationStatusUpdateDto;
import com.wastewatch.entity.AppUser;
import com.wastewatch.entity.AuditLog;
import com.wastewatch.entity.Violation;
import com.wastewatch.entity.enums.ViolationStatus;
import com.wastewatch.exception.ResourceNotFoundException;
import com.wastewatch.repository.AuditLogRepository;
import com.wastewatch.repository.UserRepository;
import com.wastewatch.repository.ViolationRepository;
import com.wastewatch.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ViolationService {

    private final ViolationRepository violationRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;

    public ViolationService(ViolationRepository violationRepository,
                            UserRepository userRepository,
                            AuditLogRepository auditLogRepository) {
        this.violationRepository = violationRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(readOnly = true)
    public List<ViolationResponseDto> getAllViolations(ViolationStatus status, Long roomId) {
        List<Violation> violations;
        if (roomId != null) {
            violations = violationRepository.findByRoomId(roomId);
        } else if (status != null) {
            violations = violationRepository.findByStatusOrderByCreatedAtDesc(status);
        } else {
            violations = violationRepository.findAllByOrderByCreatedAtDesc();
        }

        return violations.stream()
                .map(ViolationResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ViolationResponseDto getViolationById(Long id) {
        Violation violation = violationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Violation", "id", id));
        return ViolationResponseDto.fromEntity(violation);
    }

    @Transactional
    public ViolationResponseDto updateViolationStatus(Long id, ViolationStatusUpdateDto dto, UserPrincipal currentUser) {
        Violation violation = violationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Violation", "id", id));

        ViolationStatus previousStatus = violation.getStatus();
        violation.setStatus(dto.getStatus());

        if (dto.getResolutionNotes() != null) {
            violation.setResolutionNotes(dto.getResolutionNotes().trim());
        }

        if (dto.getFineAmount() != null) {
            violation.setFineAmount(dto.getFineAmount());
        }

        if (dto.getStatus() == ViolationStatus.RESOLVED && violation.getResolvedAt() == null) {
            violation.setResolvedAt(LocalDateTime.now());
        } else if (dto.getStatus() != ViolationStatus.RESOLVED) {
            violation.setResolvedAt(null);
        }

        Violation updated = violationRepository.save(violation);

        // Record Audit Log
        if (currentUser != null) {
            AppUser user = userRepository.findById(currentUser.getId()).orElse(null);
            AuditLog auditLog = new AuditLog(
                    user,
                    "UPDATE_VIOLATION_STATUS",
                    "Violation",
                    updated.getId().toString(),
                    null,
                    String.format("Violation #%d status updated from %s to %s", id, previousStatus, dto.getStatus())
            );
            auditLogRepository.save(auditLog);
        }

        return ViolationResponseDto.fromEntity(updated);
    }
}
