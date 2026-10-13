package com.wastewatch.controller;

import com.wastewatch.dto.HealthResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @GetMapping
    public ResponseEntity<HealthResponseDto> getHealth() {
        return ResponseEntity.ok(new HealthResponseDto("UP", "WasteWatch"));
    }
}
