package com.wastewatch.controller;

import com.wastewatch.dto.ApiResponse;
import com.wastewatch.dto.violation.ViolationResolveDto;
import com.wastewatch.dto.violation.ViolationResponseDto;
import com.wastewatch.dto.violation.ViolationStatusUpdateDto;
import com.wastewatch.entity.enums.ViolationSeverity;
import com.wastewatch.entity.enums.ViolationStatus;
import com.wastewatch.security.UserPrincipal;
import com.wastewatch.service.ViolationService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Phase 13: Violation Management REST Controller.
 *
 * All endpoints require ADMIN role.
 *
 * Endpoints:
 *   GET  /api/violations                    — List with multi-filter support
 *   GET  /api/violations/{id}               — Violation detail
 *   PUT  /api/violations/{id}/status        — Status transition
 *   PUT  /api/violations/{id}/resolve       — Resolve with mandatory notes
 */
@RestController
@RequestMapping("/api/violations")
@PreAuthorize("hasRole('ADMIN')")
public class ViolationController {

    private final ViolationService violationService;

    public ViolationController(ViolationService violationService) {
        this.violationService = violationService;
    }

    /**
     * GET /api/violations
     *
     * Optional query parameters:
     *   status    — OPEN | ACKNOWLEDGED | ACTION_REQUIRED | RESOLVED
     *   severity  — CRITICAL | HIGH | MEDIUM | LOW
     *   roomId    — filter by specific room
     *   buildingId — filter by building
     *   areaId    — filter by area
     *   from      — ISO datetime lower bound on createdAt (inclusive)
     *   to        — ISO datetime upper bound on createdAt (inclusive)
     */
    @GetMapping
    public ResponseEntity<List<ViolationResponseDto>> getAllViolations(
            @RequestParam(required = false) ViolationStatus status,
            @RequestParam(required = false) ViolationSeverity severity,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) Long buildingId,
            @RequestParam(required = false) Long areaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        return ResponseEntity.ok(
                violationService.getAllViolations(status, severity, roomId, buildingId, areaId, from, to));
    }

    /**
     * GET /api/violations/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ViolationResponseDto> getViolationById(@PathVariable Long id) {
        return ResponseEntity.ok(violationService.getViolationById(id));
    }

    /**
     * PUT /api/violations/{id}/status
     *
     * Transitions violation to any status. Use /resolve for the terminal RESOLVED state.
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ViolationResponseDto>> updateViolationStatus(
            @PathVariable Long id,
            @Valid @RequestBody ViolationStatusUpdateDto dto,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        ViolationResponseDto updated = violationService.updateViolationStatus(id, dto, currentUser);
        return ResponseEntity.ok(new ApiResponse<>(true, "Violation status updated successfully", updated));
    }

    /**
     * PUT /api/violations/{id}/resolve
     *
     * Terminal resolution endpoint requiring mandatory resolution notes.
     */
    @PutMapping("/{id}/resolve")
    public ResponseEntity<ApiResponse<ViolationResponseDto>> resolveViolation(
            @PathVariable Long id,
            @Valid @RequestBody ViolationResolveDto dto,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        ViolationResponseDto resolved = violationService.resolveViolation(id, dto, currentUser);
        return ResponseEntity.ok(new ApiResponse<>(true, "Violation resolved successfully", resolved));
    }
}
