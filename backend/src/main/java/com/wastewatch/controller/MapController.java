package com.wastewatch.controller;

import com.wastewatch.dto.ApiResponse;
import com.wastewatch.dto.map.BuildingMapMarkerDto;
import com.wastewatch.service.MapService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public ResponseEntity<ApiResponse<List<BuildingMapMarkerDto>>> getBuildingMapMarkers() {
        List<BuildingMapMarkerDto> markers = mapService.getBuildingMapMarkers();
        return ResponseEntity.ok(ApiResponse.ok(markers));
    }
}
