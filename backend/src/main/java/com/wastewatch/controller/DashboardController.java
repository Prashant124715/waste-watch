package com.wastewatch.controller;

import com.wastewatch.dto.ApiResponse;
import com.wastewatch.dto.dashboard.*;
import com.wastewatch.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@PreAuthorize("hasRole('ADMIN')")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryDto>> getDashboardSummary() {
        DashboardSummaryDto summary = dashboardService.getSummaryMetrics();
        return ResponseEntity.ok(ApiResponse.ok(summary));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardSummaryDto>> getDashboardStats() {
        DashboardSummaryDto summary = dashboardService.getSummaryMetrics();
        return ResponseEntity.ok(ApiResponse.ok(summary));
    }

    @GetMapping("/compliance")
    public ResponseEntity<ApiResponse<ComplianceDistributionDto>> getComplianceDistribution() {
        ComplianceDistributionDto dist = dashboardService.getComplianceDistribution();
        return ResponseEntity.ok(ApiResponse.ok(dist));
    }

    @GetMapping("/areas")
    public ResponseEntity<ApiResponse<List<AreaComplianceDto>>> getAreaCompliance() {
        List<AreaComplianceDto> areas = dashboardService.getAreaComplianceStats();
        return ResponseEntity.ok(ApiResponse.ok(areas));
    }

    @GetMapping("/trends")
    public ResponseEntity<ApiResponse<List<DailyTrendDto>>> getDashboardTrends() {
        List<DailyTrendDto> trends = dashboardService.getDailyTrends();
        return ResponseEntity.ok(ApiResponse.ok(trends));
    }

    @GetMapping("/attention-required")
    public ResponseEntity<ApiResponse<List<AttentionRequiredDto>>> getAttentionRequired() {
        List<AttentionRequiredDto> items = dashboardService.getAttentionRequired();
        return ResponseEntity.ok(ApiResponse.ok(items));
    }
}
