package com.wastewatch.controller;

import com.wastewatch.dto.ApiResponse;
import com.wastewatch.dto.inspection.InspectionRequestDto;
import com.wastewatch.dto.inspection.InspectionResponseDto;
import com.wastewatch.security.UserPrincipal;
import com.wastewatch.service.InspectionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class InspectionController {

    private final InspectionService inspectionService;

    public InspectionController(InspectionService inspectionService) {
        this.inspectionService = inspectionService;
    }

    @PostMapping("/api/inspections")
    @PreAuthorize("hasAnyRole('ADMIN', 'VOLUNTEER')")
    public ResponseEntity<ApiResponse<InspectionResponseDto>> createInspection(
            @Valid @RequestBody InspectionRequestDto requestDto,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        InspectionResponseDto created = inspectionService.createInspection(requestDto, currentUser);
        ApiResponse<InspectionResponseDto> response = new ApiResponse<>(
                true,
                "Inspection recorded successfully",
                created
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/api/inspections")
    @PreAuthorize("hasAnyRole('ADMIN', 'VOLUNTEER')")
    public ResponseEntity<List<InspectionResponseDto>> getAllInspections(
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) Long volunteerId,
            @RequestParam(required = false) Long buildingId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(inspectionService.getAllInspections(roomId, volunteerId, buildingId, currentUser));
    }

    @GetMapping("/api/inspections/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VOLUNTEER')")
    public ResponseEntity<InspectionResponseDto> getInspectionById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(inspectionService.getInspectionById(id, currentUser));
    }

    @GetMapping("/api/rooms/{roomId}/inspections")
    @PreAuthorize("hasAnyRole('ADMIN', 'VOLUNTEER')")
    public ResponseEntity<List<InspectionResponseDto>> getInspectionsByRoom(@PathVariable Long roomId) {
        return ResponseEntity.ok(inspectionService.getInspectionsByRoomId(roomId));
    }

    @GetMapping("/api/volunteers/me/inspections")
    @PreAuthorize("hasAnyRole('ADMIN', 'VOLUNTEER')")
    public ResponseEntity<List<InspectionResponseDto>> getMyInspections(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(inspectionService.getMyInspections(currentUser));
    }

    @GetMapping("/api/inspections/recent")
    @PreAuthorize("hasAnyRole('ADMIN', 'VOLUNTEER')")
    public ResponseEntity<List<InspectionResponseDto>> getRecentInspections(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(inspectionService.getRecentInspections(currentUser));
    }
}
