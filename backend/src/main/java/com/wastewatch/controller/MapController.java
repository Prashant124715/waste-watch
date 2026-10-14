package com.wastewatch.controller;

import com.wastewatch.dto.ApiResponse;
import com.wastewatch.dto.map.AreaHotspotDto;
import com.wastewatch.dto.map.AreaMapSummaryDto;
import com.wastewatch.dto.map.BuildingMapMarkerDto;
import com.wastewatch.service.MapService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST Controller exposing geographic data for the Leaflet.js admin map view.
 * Phase 11 — Geographic Monitoring.
 */
@RestController
@RequestMapping("/api/map")
public class MapController {

    private final MapService mapService;

    public MapController(MapService mapService) {
        this.mapService = mapService;
    }

    /**
     * GET /api/map/buildings
     * Returns building-level geographic data for map markers.
     * Optional filters can be applied by area, compliance status,
     * building, and inspection status.
     * Access: ADMIN, VOLUNTEER
     */
    @GetMapping("/buildings")
    @PreAuthorize("hasAnyRole('ADMIN', 'VOLUNTEER')")
    public ResponseEntity<ApiResponse<List<BuildingMapMarkerDto>>> getBuildingMapMarkers(
            @RequestParam(required = false) Long areaId,
            @RequestParam(required = false) String complianceStatus,
            @RequestParam(required = false) Long buildingId,
            @RequestParam(required = false) String inspectionStatus) {

        List<BuildingMapMarkerDto> markers =
                mapService.getBuildingMapMarkers(
                        areaId,
                        complianceStatus,
                        buildingId,
                        inspectionStatus);

        return ResponseEntity.ok(ApiResponse.ok(markers));
    }

    /**
     * GET /api/map/hotspots
     * Returns area-level hotspot information for geographic monitoring.
     * Access: ADMIN, VOLUNTEER
     */
    @GetMapping("/hotspots")
    @PreAuthorize("hasAnyRole('ADMIN', 'VOLUNTEER')")
    public ResponseEntity<ApiResponse<List<AreaHotspotDto>>> getAreaHotspots() {
        List<AreaHotspotDto> hotspots = mapService.getAreaHotspots();
        return ResponseEntity.ok(ApiResponse.ok(hotspots));
    }

    /**
     * GET /api/map/areas
     * Returns area-level aggregate compliance, inspection coverage,
     * poor-room counts, open violations, and health scores.
     * Access: ADMIN only
     */
    @GetMapping("/areas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AreaMapSummaryDto>>> getAreaMapSummaries() {
        List<AreaMapSummaryDto> summaries = mapService.getAreaMapSummaries();
        return ResponseEntity.ok(ApiResponse.ok(summaries));
    }
}