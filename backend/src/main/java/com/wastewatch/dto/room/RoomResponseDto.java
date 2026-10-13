package com.wastewatch.dto.room;

import com.wastewatch.entity.Room;
import java.time.LocalDateTime;

public class RoomResponseDto {

    private Long id;
    private Long buildingId;
    private String buildingName;
    private Integer floorNumber;
    private String roomNumber;
    private String qrCodeToken;
    private String occupantName;
    private String contactPhone;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public RoomResponseDto() {
    }

    public static RoomResponseDto fromEntity(Room room) {
        RoomResponseDto dto = new RoomResponseDto();
        dto.setId(room.getId());
        if (room.getBuilding() != null) {
            dto.setBuildingId(room.getBuilding().getId());
            dto.setBuildingName(room.getBuilding().getName());
        }
        dto.setFloorNumber(room.getFloorNumber());
        dto.setRoomNumber(room.getRoomNumber());
        dto.setQrCodeToken(room.getQrCodeToken());
        dto.setOccupantName(room.getOccupantName());
        dto.setContactPhone(room.getContactPhone());
        dto.setStatus(room.getStatus());
        dto.setCreatedAt(room.getCreatedAt());
        dto.setUpdatedAt(room.getUpdatedAt());
        return dto;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(Long buildingId) {
        this.buildingId = buildingId;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }

    public Integer getFloorNumber() {
        return floorNumber;
    }

    public void setFloorNumber(Integer floorNumber) {
        this.floorNumber = floorNumber;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getQrCodeToken() {
        return qrCodeToken;
    }

    public void setQrCodeToken(String qrCodeToken) {
        this.qrCodeToken = qrCodeToken;
    }

    public String getOccupantName() {
        return occupantName;
    }

    public void setOccupantName(String occupantName) {
        this.occupantName = occupantName;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
