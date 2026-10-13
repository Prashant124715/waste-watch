package com.wastewatch.controller;

import com.wastewatch.dto.ApiResponse;
import com.wastewatch.dto.area.AreaRequestDto;
import com.wastewatch.dto.area.AreaResponseDto;
import com.wastewatch.service.AreaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/areas")
public class AreaController {

    private final AreaService areaService;

    public AreaController(AreaService areaService) {
        this.areaService = areaService;
    }

    @GetMapping
    public ResponseEntity<List<AreaResponseDto>> getAllAreas() {
        return ResponseEntity.ok(areaService.getAllAreas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AreaResponseDto> getAreaById(@PathVariable Long id) {
        return ResponseEntity.ok(areaService.getAreaById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AreaResponseDto> createArea(@Valid @RequestBody AreaRequestDto requestDto) {
        AreaResponseDto created = areaService.createArea(requestDto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AreaResponseDto> updateArea(@PathVariable Long id, @Valid @RequestBody AreaRequestDto requestDto) {
        AreaResponseDto updated = areaService.updateArea(id, requestDto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteArea(@PathVariable Long id) {
        areaService.deleteArea(id);
        return ResponseEntity.ok(ApiResponse.ok("Area deleted successfully"));
    }
}
