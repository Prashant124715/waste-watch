package com.wastewatch.controller;

import com.wastewatch.dto.ApiResponse;
import com.wastewatch.dto.volunteer.VolunteerStatusUpdateDto;
import com.wastewatch.dto.volunteer.VolunteerSummaryDto;
import com.wastewatch.security.UserPrincipal;
import com.wastewatch.service.VolunteerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/volunteers")
public class VolunteerController {

    private final VolunteerService volunteerService;

    public VolunteerController(VolunteerService volunteerService) {
        this.volunteerService = volunteerService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<VolunteerSummaryDto>>> getAllVolunteers(
            @RequestParam(required = false) com.wastewatch.entity.enums.UserStatus status) {
        List<VolunteerSummaryDto> volunteers = volunteerService.getAllVolunteers(status);
        return ResponseEntity.ok(ApiResponse.ok(volunteers));
    }

    @GetMapping("/{id}/performance")
    @PreAuthorize("hasAnyRole('ADMIN', 'VOLUNTEER')")
    public ResponseEntity<ApiResponse<VolunteerSummaryDto>> getVolunteerPerformance(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        if (!currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))
                && !currentUser.getId().equals(id)) {
            throw new AccessDeniedException("Volunteers can only access their own performance metrics");
        }
        VolunteerSummaryDto performance = volunteerService.getVolunteerPerformance(id);
        return ResponseEntity.ok(ApiResponse.ok(performance));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<VolunteerSummaryDto>> updateVolunteerStatus(
            @PathVariable Long id,
            @Valid @RequestBody VolunteerStatusUpdateDto dto,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        VolunteerSummaryDto updated = volunteerService.updateVolunteerStatus(id, dto, currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Volunteer status updated successfully", updated));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<VolunteerSummaryDto>> approveVolunteer(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        VolunteerSummaryDto approved = volunteerService.approveVolunteer(id, currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Volunteer application approved successfully", approved));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<VolunteerSummaryDto>> rejectVolunteer(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        VolunteerSummaryDto rejected = volunteerService.rejectVolunteer(id, currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Volunteer application rejected", rejected));
    }
}
