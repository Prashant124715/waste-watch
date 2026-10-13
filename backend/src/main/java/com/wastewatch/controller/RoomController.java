package com.wastewatch.controller;

import com.wastewatch.dto.ApiResponse;
import com.wastewatch.dto.room.RoomRequestDto;
import com.wastewatch.dto.room.RoomResponseDto;
import com.wastewatch.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping("/api/rooms")
    public ResponseEntity<List<RoomResponseDto>> getAllRooms(
            @RequestParam(required = false) Long buildingId) {
        if (buildingId != null) {
            return ResponseEntity.ok(roomService.getRoomsByBuildingId(buildingId));
        }
        return ResponseEntity.ok(roomService.getAllRooms());
    }

    @GetMapping("/api/rooms/{id}")
    public ResponseEntity<RoomResponseDto> getRoomById(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.getRoomById(id));
    }

    @GetMapping("/api/buildings/{buildingId}/rooms")
    public ResponseEntity<List<RoomResponseDto>> getRoomsByBuilding(@PathVariable Long buildingId) {
        return ResponseEntity.ok(roomService.getRoomsByBuildingId(buildingId));
    }

    @GetMapping("/api/rooms/qr/{code}")
    public ResponseEntity<com.wastewatch.dto.room.RoomDetailDto> getRoomByQrCode(@PathVariable String code) {
        return ResponseEntity.ok(roomService.getRoomByQrCode(code));
    }

    @PostMapping("/api/rooms")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoomResponseDto> createRoom(@Valid @RequestBody RoomRequestDto requestDto) {
        RoomResponseDto created = roomService.createRoom(requestDto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/api/rooms/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoomResponseDto> updateRoom(@PathVariable Long id, @Valid @RequestBody RoomRequestDto requestDto) {
        RoomResponseDto updated = roomService.updateRoom(id, requestDto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/api/rooms/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.ok(ApiResponse.ok("Room deleted successfully"));
    }
}
