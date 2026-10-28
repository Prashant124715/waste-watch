package com.wastewatch.service;

import com.wastewatch.dto.violation.ViolationResolveDto;
import com.wastewatch.dto.violation.ViolationResponseDto;
import com.wastewatch.dto.violation.ViolationStatusUpdateDto;
import com.wastewatch.entity.AppUser;
import com.wastewatch.entity.AuditLog;
import com.wastewatch.entity.Inspection;
import com.wastewatch.entity.Room;
import com.wastewatch.entity.Violation;
import com.wastewatch.entity.enums.ComplianceClassification;
import com.wastewatch.entity.enums.NotificationType;
import com.wastewatch.entity.enums.ViolationSeverity;
import com.wastewatch.entity.enums.ViolationStatus;
import com.wastewatch.exception.InvalidRequestException;
import com.wastewatch.exception.ResourceNotFoundException;
import com.wastewatch.repository.AuditLogRepository;
import com.wastewatch.repository.InspectionRepository;
import com.wastewatch.repository.RoomRepository;
import com.wastewatch.repository.UserRepository;
import com.wastewatch.repository.ViolationRepository;
import com.wastewatch.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Phase 13 ViolationService — Violation and Notification Management.
 *
 * Business Rules:
 * - Violations are AUTOMATICALLY created when an inspection produces a POOR score (< 40 points).
 * - Duplicate prevention: Only one violation per inspection (enforced by DB unique constraint on inspection_id).
 * - Severity Tiers (applied to POOR-range 0–39):
 *     0–19  → CRITICAL
 *     20–39 → HIGH
 *   (MEDIUM and LOW are informational only; violations only trigger for POOR scores.)
 * - Persistent Non-Compliance: If a room has 3 consecutive POOR inspections, the room is flagged
 *   with persistentNonCompliance = true. A PERSISTENT_NON_COMPLIANCE notification is sent to admins.
 *   This flag is stored on the Room entity (no separate table needed).
 * - Status transitions:
 *     OPEN → ACKNOWLEDGED → ACTION_REQUIRED → RESOLVED
 *   Any admin can perform any valid forward transition.
 */
@Service
public class ViolationService {

    /** Number of consecutive POOR inspections required to mark a room as persistent non-compliant. */
    private static final int PERSISTENT_NON_COMPLIANCE_THRESHOLD = 3;

    private final ViolationRepository violationRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final InspectionRepository inspectionRepository;
    private final RoomRepository roomRepository;
    private final NotificationService notificationService;

    public ViolationService(ViolationRepository violationRepository,
                            UserRepository userRepository,
                            AuditLogRepository auditLogRepository,
                            InspectionRepository inspectionRepository,
                            RoomRepository roomRepository,
                            NotificationService notificationService) {
        this.violationRepository = violationRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
        this.inspectionRepository = inspectionRepository;
        this.roomRepository = roomRepository;
        this.notificationService = notificationService;
    }

    // -------------------------------------------------------------------------
    // READ
    // -------------------------------------------------------------------------

    /**
     * Returns violations with optional multi-dimensional filtering.
     */
    @Transactional(readOnly = true)
    public List<ViolationResponseDto> getAllViolations(
            ViolationStatus status,
            ViolationSeverity severity,
            Long roomId,
            Long buildingId,
            Long areaId,
            LocalDateTime from,
            LocalDateTime to) {

        return violationRepository
                .findWithFilters(status, severity, buildingId, areaId, roomId, from, to)
                .stream()
                .map(ViolationResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Backward-compatible overload (status + roomId only) used by legacy callers.
     */
    @Transactional(readOnly = true)
    public List<ViolationResponseDto> getAllViolations(ViolationStatus status, Long roomId) {
        return getAllViolations(status, null, roomId, null, null, null, null);
    }

    @Transactional(readOnly = true)
    public ViolationResponseDto getViolationById(Long id) {
        Violation violation = violationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Violation", "id", id));
        return ViolationResponseDto.fromEntity(violation);
    }

    // -------------------------------------------------------------------------
    // STATUS UPDATE
    // -------------------------------------------------------------------------

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

        recordAudit(currentUser, "UPDATE_VIOLATION_STATUS",
                String.format("Violation #%d status updated from %s to %s", id, previousStatus, dto.getStatus()),
                updated.getId());

        return ViolationResponseDto.fromEntity(updated);
    }

    // -------------------------------------------------------------------------
    // RESOLVE
    // -------------------------------------------------------------------------

    /**
     * Resolves a violation with mandatory resolution notes.
     */
    @Transactional
    public ViolationResponseDto resolveViolation(Long id, ViolationResolveDto dto, UserPrincipal currentUser) {
        Violation violation = violationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Violation", "id", id));

        if (violation.getStatus() == ViolationStatus.RESOLVED) {
            throw new InvalidRequestException("Violation #" + id + " is already resolved.");
        }

        if (dto.getResolutionNotes() == null || dto.getResolutionNotes().isBlank()) {
            throw new InvalidRequestException("Resolution notes are required to resolve a violation.");
        }

        ViolationStatus previousStatus = violation.getStatus();
        violation.setStatus(ViolationStatus.RESOLVED);
        violation.setResolutionNotes(dto.getResolutionNotes().trim());
        violation.setResolvedAt(LocalDateTime.now());

        if (dto.getFineAmount() != null) {
            violation.setFineAmount(dto.getFineAmount());
        }

        Violation updated = violationRepository.save(violation);

        recordAudit(currentUser, "RESOLVE_VIOLATION",
                String.format("Violation #%d resolved (was %s). Notes: %s", id, previousStatus, dto.getResolutionNotes()),
                updated.getId());

        return ViolationResponseDto.fromEntity(updated);
    }

    // -------------------------------------------------------------------------
    // AUTO-VIOLATION (called from InspectionService)
    // -------------------------------------------------------------------------

    /**
     * Evaluates the saved inspection and:
     * 1. Creates a violation if the score is POOR (< 40) and no violation exists for this inspection.
     * 2. Detects persistent non-compliance (3+ consecutive POOR inspections).
     * 3. Sends appropriate admin notifications.
     */
    @Transactional
    public Violation processInspectionForViolation(Inspection inspection, Room room) {
        return processInspectionForViolation(inspection, room, room != null ? room.getBuilding() : inspection.getEffectiveBuilding());
    }

    @Transactional
    public Violation processInspectionForViolation(Inspection inspection, Room room, com.wastewatch.entity.Building building) {
        if (inspection.getClassification() != ComplianceClassification.POOR) {
            return null;
        }

        // Duplicate prevention — one violation per inspection (DB constraint + guard)
        if (violationRepository.findByInspectionId(inspection.getId()).isPresent()) {
            return null;
        }

        int score = inspection.getTotalScore();
        ViolationSeverity severity = determineSeverity(score);

        String description = buildViolationDescription(score, inspection.getClassification(), inspection.getRemarks());

        com.wastewatch.entity.Building effectiveBuilding = building != null ? building : (room != null ? room.getBuilding() : inspection.getEffectiveBuilding());

        Violation violation = new Violation();
        violation.setInspection(inspection);
        violation.setRoom(room);
        violation.setBuilding(effectiveBuilding);
        violation.setSeverity(severity);
        violation.setStatus(ViolationStatus.OPEN);
        violation.setDescription(description);

        // Fine amount: CRITICAL = ₹500, HIGH = ₹250
        violation.setFineAmount(severity == ViolationSeverity.CRITICAL
                ? new BigDecimal("500.00")
                : new BigDecimal("250.00"));

        Violation saved = violationRepository.save(violation);

        String buildingName = effectiveBuilding != null ? effectiveBuilding.getName() : "Unknown Building";
        String areaName = (effectiveBuilding != null && effectiveBuilding.getArea() != null) ? effectiveBuilding.getArea().getName() : "Unknown Area";

        // Send violation notification to all admins
        String violTitle = room != null
                ? String.format("New %s Violation — %s, Room %s", severity, buildingName, room.getRoomNumber())
                : String.format("New %s Violation — %s", severity, buildingName);

        String violMsg = room != null
                ? String.format("%s %s violation raised for Room %s in %s (%s). Score: %d/100. %s",
                    severity == ViolationSeverity.CRITICAL ? "⛔" : "⚠️",
                    severity, room.getRoomNumber(), buildingName, areaName, score, description)
                : String.format("%s %s violation raised for %s (%s). Score: %d/100. %s",
                    severity == ViolationSeverity.CRITICAL ? "⛔" : "⚠️",
                    severity, buildingName, areaName, score, description);

        NotificationType notifType = (severity == ViolationSeverity.CRITICAL || severity == ViolationSeverity.HIGH)
                ? NotificationType.VIOLATION_SEVERITY_HIGH
                : NotificationType.VIOLATION_RAISED;
        notificationService.notifyAllAdmins(violTitle, violMsg, notifType);

        // Check persistent non-compliance for room
        if (room != null) {
            checkAndFlagPersistentNonCompliance(room);
        }
        if (effectiveBuilding != null) {
            checkAndNotifyPersistentNonComplianceForBuilding(effectiveBuilding);
        }

        return saved;
    }

    private void checkAndNotifyPersistentNonComplianceForBuilding(com.wastewatch.entity.Building building) {
        List<Inspection> recentInspections = inspectionRepository
                .findTopNByEffectiveBuildingIdOrderByInspectionDateDesc(building.getId(), PERSISTENT_NON_COMPLIANCE_THRESHOLD);

        if (recentInspections.size() < PERSISTENT_NON_COMPLIANCE_THRESHOLD) {
            return;
        }

        boolean allPoor = recentInspections.stream()
                .allMatch(i -> i.getClassification() == ComplianceClassification.POOR);

        if (allPoor) {
            String title = "⚠️ Persistent Non-Compliance — " + building.getName();
            String message = String.format(
                    "Building %s (%s) has had %d consecutive POOR inspections and requires immediate intervention.",
                    building.getName(),
                    building.getArea() != null ? building.getArea().getName() : "Unknown Area",
                    PERSISTENT_NON_COMPLIANCE_THRESHOLD);

            notificationService.notifyAllAdmins(title, message, NotificationType.PERSISTENT_NON_COMPLIANCE);
        }
    }

    // -------------------------------------------------------------------------
    // PERSISTENT NON-COMPLIANCE DETECTION
    // -------------------------------------------------------------------------

    /**
     * Checks if the room's last PERSISTENT_NON_COMPLIANCE_THRESHOLD inspections
     * are all POOR. If so, flags the room and sends a notification.
     */
    private void checkAndFlagPersistentNonCompliance(Room room) {
        List<Inspection> recentInspections = inspectionRepository
                .findTopNByRoomIdOrderByInspectionDateDesc(room.getId(), PERSISTENT_NON_COMPLIANCE_THRESHOLD);

        if (recentInspections.size() < PERSISTENT_NON_COMPLIANCE_THRESHOLD) {
            return;
        }

        boolean allPoor = recentInspections.stream()
                .allMatch(i -> i.getClassification() == ComplianceClassification.POOR);

        if (allPoor && !Boolean.TRUE.equals(room.getPersistentNonCompliance())) {
            room.setPersistentNonCompliance(true);
            roomRepository.save(room);

            String title = "⚠️ Persistent Non-Compliance — Room " + room.getRoomNumber();
            String message = String.format(
                    "Room %s in %s (%s) has had %d consecutive POOR inspections and is flagged as PERSISTENT_NON_COMPLIANCE.",
                    room.getRoomNumber(),
                    room.getBuilding().getName(),
                    room.getBuilding().getArea() != null ? room.getBuilding().getArea().getName() : "Unknown Area",
                    PERSISTENT_NON_COMPLIANCE_THRESHOLD);

            notificationService.notifyAllAdmins(title, message, NotificationType.PERSISTENT_NON_COMPLIANCE);
        }
    }

    // -------------------------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------------------------

    /**
     * Severity mapping for POOR range:
     * 0–19  → CRITICAL
     * 20–39 → HIGH
     */
    public ViolationSeverity determineSeverity(int score) {
        if (score < 20) return ViolationSeverity.CRITICAL;
        if (score < 40) return ViolationSeverity.HIGH;
        if (score < 60) return ViolationSeverity.MEDIUM;
        return ViolationSeverity.LOW;
    }

    private String buildViolationDescription(int score, ComplianceClassification classification, String remarks) {
        String base = String.format("Automatic non-compliance flag: Score was %d/100 (%s).", score, classification.name());
        if (remarks != null && !remarks.isBlank()) {
            return base + " Remarks: " + remarks.trim();
        }
        return base + " Failed required waste segregation thresholds.";
    }

    private void recordAudit(UserPrincipal currentUser, String action, String detail, Long entityId) {
        if (currentUser == null) return;
        AppUser user = userRepository.findById(currentUser.getId()).orElse(null);
        AuditLog auditLog = new AuditLog(user, action, "Violation", entityId.toString(), null, detail);
        auditLogRepository.save(auditLog);
    }
}
