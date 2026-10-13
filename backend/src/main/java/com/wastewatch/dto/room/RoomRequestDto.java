package com.wastewatch.dto.room;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class RoomRequestDto {

    @NotNull(message = "Building ID is required")
    private Long buildingId;

    @NotNull(message = "Floor number is required")
    private Integer floorNumber;

    @NotBlank(message = "Room number is required")
    @Size(max = 30, message = "Room number must not exceed 30 characters")
    private String roomNumber;

    @Size(max = 64, message = "QR code token must not exceed 64 characters")
    private String qrCodeToken;

    @Size(max = 100, message = "Occupant name must not exceed 100 characters")
    private String occupantName;

    @Size(max = 20, message = "Contact phone must not exceed 20 characters")
    private String contactPhone;

    @Size(max = 20, message = "Status must not exceed 20 characters")
    private String status;

    public RoomRequestDto() {
    }

    public Long getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(Long buildingId) {
        this.buildingId = buildingId;
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
}
