package com.wastewatch.controller;

import com.wastewatch.dto.ApiResponse;
import com.wastewatch.dto.room.RoomDetailDto;
import com.wastewatch.dto.room.RoomRequestDto;
import com.wastewatch.dto.room.RoomResponseDto;
import com.wastewatch.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

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

    /**
     * Authenticated QR lookup endpoint.
     * Accessible by authenticated VOLUNTEER and ADMIN.
     * Returns sanitized room metadata with building, area, and recent inspection info.
     */
    @GetMapping("/api/rooms/qr/{code}")
    public ResponseEntity<RoomDetailDto> getRoomByQrCode(@PathVariable String code) {
        return ResponseEntity.ok(roomService.getRoomByQrCode(code));
    }

    /**
     * Generate raw PNG image bytes for a room's QR code by room ID.
     */
    @GetMapping(value = "/api/rooms/{id}/qr-image", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getRoomQrCodeImage(
            @PathVariable Long id,
            @RequestParam(defaultValue = "300") int size) {
        byte[] imageBytes = roomService.getRoomQrCodeImage(id, size);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .cacheControl(CacheControl.maxAge(30, TimeUnit.MINUTES).cachePublic())
                .body(imageBytes);
    }

    /**
     * Generate raw PNG image bytes for a room's QR code by QR token.
     */
    @GetMapping(value = "/api/rooms/qr/{code}/image", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getQrCodeImageByToken(
            @PathVariable String code,
            @RequestParam(defaultValue = "300") int size) {
        byte[] imageBytes = roomService.getQrCodeImageByToken(code, size);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .cacheControl(CacheControl.maxAge(30, TimeUnit.MINUTES).cachePublic())
                .body(imageBytes);
    }

    /**
     * Admin action to regenerate a room's QR code identifier.
     */
    @PostMapping("/api/rooms/{id}/regenerate-qr")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoomResponseDto> regenerateRoomQrCode(@PathVariable Long id) {
        RoomResponseDto updated = roomService.regenerateRoomQrCode(id);
        return ResponseEntity.ok(updated);
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
