package com.wastewatch.controller;

import com.wastewatch.dto.ApiResponse;
import com.wastewatch.dto.analytics.*;
import com.wastewatch.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@PreAuthorize("hasRole('ADMIN')")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<OverviewAnalyticsDto>> getOverviewAnalytics() {
        OverviewAnalyticsDto overview = analyticsService.getOverviewAnalytics();
        return ResponseEntity.ok(ApiResponse.ok(overview));
    }

    @GetMapping("/areas")
    public ResponseEntity<ApiResponse<List<AreaAnalyticsDto>>> getAreaAnalytics() {
        List<AreaAnalyticsDto> areas = analyticsService.getAreaAnalytics();
        return ResponseEntity.ok(ApiResponse.ok(areas));
    }

    @GetMapping("/buildings")
    public ResponseEntity<ApiResponse<List<BuildingAnalyticsDto>>> getBuildingAnalytics() {
        List<BuildingAnalyticsDto> buildings = analyticsService.getBuildingAnalytics();
        return ResponseEntity.ok(ApiResponse.ok(buildings));
    }

    @GetMapping("/trends")
    public ResponseEntity<ApiResponse<TrendAnalyticsDto>> getTrendAnalytics(
            @RequestParam(name = "period", required = false, defaultValue = "daily") String period,
            @RequestParam(name = "days", required = false, defaultValue = "30") Integer days) {
        TrendAnalyticsDto trends = analyticsService.getTrendAnalytics(period, days);
        return ResponseEntity.ok(ApiResponse.ok(trends));
    }

    @GetMapping("/improvements")
    public ResponseEntity<ApiResponse<List<AreaImprovementDto>>> getAreaImprovements(
            @RequestParam(name = "days", required = false, defaultValue = "14") Integer days) {
        List<AreaImprovementDto> improvements = analyticsService.getAreaImprovements(days);
        return ResponseEntity.ok(ApiResponse.ok(improvements));
    }

    @GetMapping("/improved-areas")
    public ResponseEntity<ApiResponse<List<AreaImprovementDto>>> getImprovedAreas(
            @RequestParam(name = "days", required = false, defaultValue = "14") Integer days) {
        List<AreaImprovementDto> improvements = analyticsService.getAreaImprovements(days);
        return ResponseEntity.ok(ApiResponse.ok(improvements));
    }

    @GetMapping("/hotspots")
    public ResponseEntity<ApiResponse<HotspotAnalyticsDto>> getHotspots() {
        HotspotAnalyticsDto hotspots = analyticsService.getHotspots();
        return ResponseEntity.ok(ApiResponse.ok(hotspots));
    }

    @GetMapping("/volunteers")
    public ResponseEntity<ApiResponse<List<VolunteerAnalyticsDto>>> getVolunteerAnalytics() {
        List<VolunteerAnalyticsDto> volunteers = analyticsService.getVolunteerAnalytics();
        return ResponseEntity.ok(ApiResponse.ok(volunteers));
    }
}
