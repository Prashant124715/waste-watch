package com.wastewatch.controller;

import com.wastewatch.dto.ApiResponse;
import com.wastewatch.dto.map.AreaHotspotDto;
import com.wastewatch.dto.map.BuildingMapMarkerDto;
import com.wastewatch.service.MapService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/map")
public class MapController {

    private final MapService mapService;

    public MapController(MapService mapService) {
        this.mapService = mapService;
    }

    @GetMapping("/buildings")
    @PreAuthorize("hasAnyRole('ADMIN', 'VOLUNTEER')")
    public ResponseEntity<ApiResponse<List<BuildingMapMarkerDto>>> getBuildingMapMarkers(
            @RequestParam(required = false) Long areaId,
            @RequestParam(required = false) String complianceStatus,
            @RequestParam(required = false) Long buildingId,
            @RequestParam(required = false) String inspectionStatus) {
        List<BuildingMapMarkerDto> markers = mapService.getBuildingMapMarkers(areaId, complianceStatus, buildingId, inspectionStatus);
        return ResponseEntity.ok(ApiResponse.ok(markers));
    }

    @GetMapping("/hotspots")
    @PreAuthorize("hasAnyRole('ADMIN', 'VOLUNTEER')")
    public ResponseEntity<ApiResponse<List<AreaHotspotDto>>> getAreaHotspots() {
        List<AreaHotspotDto> hotspots = mapService.getAreaHotspots();
        return ResponseEntity.ok(ApiResponse.ok(hotspots));
    }
}
