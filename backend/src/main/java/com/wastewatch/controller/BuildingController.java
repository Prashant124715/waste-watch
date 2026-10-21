package com.wastewatch.controller;

import com.wastewatch.dto.ApiResponse;
import com.wastewatch.dto.building.BuildingRequestDto;
import com.wastewatch.dto.building.BuildingResponseDto;
import com.wastewatch.service.BuildingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class BuildingController {

    private final BuildingService buildingService;

    public BuildingController(BuildingService buildingService) {
        this.buildingService = buildingService;
    }

    @GetMapping("/api/buildings")
    public ResponseEntity<List<BuildingResponseDto>> getAllBuildings(
            @RequestParam(required = false) Long areaId,
            @RequestParam(required = false) String search) {
        if (search != null && !search.isBlank()) {
            return ResponseEntity.ok(buildingService.searchBuildings(search));
        }
        if (areaId != null) {
            return ResponseEntity.ok(buildingService.getBuildingsByAreaId(areaId));
        }
        return ResponseEntity.ok(buildingService.getAllBuildings());
    }

    @GetMapping("/api/buildings/search")
    public ResponseEntity<List<BuildingResponseDto>> searchBuildings(
            @RequestParam(required = false, defaultValue = "") String query) {
        return ResponseEntity.ok(buildingService.searchBuildings(query));
    }

    @GetMapping("/api/buildings/{id}")
    public ResponseEntity<BuildingResponseDto> getBuildingById(@PathVariable Long id) {
        return ResponseEntity.ok(buildingService.getBuildingById(id));
    }

    @GetMapping("/api/areas/{areaId}/buildings")
    public ResponseEntity<List<BuildingResponseDto>> getBuildingsByArea(@PathVariable Long areaId) {
        return ResponseEntity.ok(buildingService.getBuildingsByAreaId(areaId));
    }

    @PostMapping("/api/buildings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BuildingResponseDto> createBuilding(@Valid @RequestBody BuildingRequestDto requestDto) {
        BuildingResponseDto created = buildingService.createBuilding(requestDto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/api/buildings/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BuildingResponseDto> updateBuilding(@PathVariable Long id, @Valid @RequestBody BuildingRequestDto requestDto) {
        BuildingResponseDto updated = buildingService.updateBuilding(id, requestDto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/api/buildings/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteBuilding(@PathVariable Long id) {
        buildingService.deleteBuilding(id);
        return ResponseEntity.ok(ApiResponse.ok("Building deleted successfully"));
    }
}
