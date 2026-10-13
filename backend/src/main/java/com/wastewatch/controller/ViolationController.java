package com.wastewatch.controller;

import com.wastewatch.dto.ApiResponse;
import com.wastewatch.dto.violation.ViolationResponseDto;
import com.wastewatch.dto.violation.ViolationStatusUpdateDto;
import com.wastewatch.entity.enums.ViolationStatus;
import com.wastewatch.security.UserPrincipal;
import com.wastewatch.service.ViolationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/violations")
@PreAuthorize("hasRole('ADMIN')")
public class ViolationController {

    private final ViolationService violationService;

    public ViolationController(ViolationService violationService) {
        this.violationService = violationService;
    }

    @GetMapping
    public ResponseEntity<List<ViolationResponseDto>> getAllViolations(
            @RequestParam(required = false) ViolationStatus status,
            @RequestParam(required = false) Long roomId) {
        return ResponseEntity.ok(violationService.getAllViolations(status, roomId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ViolationResponseDto> getViolationById(@PathVariable Long id) {
        return ResponseEntity.ok(violationService.getViolationById(id));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ViolationResponseDto>> updateViolationStatus(
            @PathVariable Long id,
            @Valid @RequestBody ViolationStatusUpdateDto dto,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        ViolationResponseDto updated = violationService.updateViolationStatus(id, dto, currentUser);
        ApiResponse<ViolationResponseDto> response = new ApiResponse<>(
                true,
                "Violation status updated successfully",
                updated
        );
        return ResponseEntity.ok(response);
    }
}
