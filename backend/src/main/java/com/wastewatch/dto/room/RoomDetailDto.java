package com.wastewatch.dto.room;

import com.wastewatch.entity.enums.ComplianceClassification;

import java.time.LocalDateTime;

public class RoomDetailDto {

    private Long id;
    private Long buildingId;
    private String buildingName;
    private Long areaId;
    private String areaName;
    private Integer floorNumber;
    private String roomNumber;
    private String qrCodeToken;
    private String occupantName;
    private String contactPhone;
    private String status;
    private LocalDateTime lastInspectedAt;
    private Integer lastScore;
    private ComplianceClassification lastClassification;
    private long totalInspections;

    public RoomDetailDto() {
    }

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

    public Long getAreaId() {
        return areaId;
    }

    public void setAreaId(Long areaId) {
        this.areaId = areaId;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
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

    public LocalDateTime getLastInspectedAt() {
        return lastInspectedAt;
    }

    public void setLastInspectedAt(LocalDateTime lastInspectedAt) {
        this.lastInspectedAt = lastInspectedAt;
    }

    public Integer getLastScore() {
        return lastScore;
    }

    public void setLastScore(Integer lastScore) {
        this.lastScore = lastScore;
    }

    public ComplianceClassification getLastClassification() {
        return lastClassification;
    }

    public void setLastClassification(ComplianceClassification lastClassification) {
        this.lastClassification = lastClassification;
    }

    public long getTotalInspections() {
        return totalInspections;
    }

    public void setTotalInspections(long totalInspections) {
        this.totalInspections = totalInspections;
    }
}
