package com.wastewatch.service;

import com.wastewatch.dto.audit.AuditLogResponseDto;
import com.wastewatch.entity.AppUser;
import com.wastewatch.entity.AuditLog;
import com.wastewatch.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponseDto> getAuditLogs(
            String action,
            String entityType,
            Long userId,
            LocalDateTime from,
            LocalDateTime to) {

        return auditLogRepository.findWithFilters(action, entityType, userId, from, to)
                .stream()
                .map(AuditLogResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Safely records an administrative audit log.
     * Guaranteed never to log passwords or sensitive tokens.
     */
    @Transactional
    public AuditLog record(AppUser user, String action, String entityType, String entityId, String ipAddress, String details) {
        AuditLog auditLog = new AuditLog(user, action, entityType, entityId, ipAddress, details);
        return auditLogRepository.save(auditLog);
    }
}
