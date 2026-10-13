package com.wastewatch.dto.dashboard;

import com.wastewatch.entity.enums.ComplianceClassification;
import java.time.LocalDateTime;

public class AttentionRequiredDto {

    private Long roomId;
    private String roomNumber;
    private Integer floorNumber;
    private Long buildingId;
    private String buildingName;
    private String areaName;
    private Integer latestScore;
    private ComplianceClassification latestClassification;
    private LocalDateTime lastInspectedAt;
    private boolean activeViolation;
    private String reason;

    public AttentionRequiredDto() {
    }

    public AttentionRequiredDto(Long roomId, String roomNumber, Integer floorNumber, Long buildingId,
                                String buildingName, String areaName, Integer latestScore,
                                ComplianceClassification latestClassification, LocalDateTime lastInspectedAt,
                                boolean activeViolation, String reason) {
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.floorNumber = floorNumber;
        this.buildingId = buildingId;
        this.buildingName = buildingName;
        this.areaName = areaName;
        this.latestScore = latestScore;
        this.latestClassification = latestClassification;
        this.lastInspectedAt = lastInspectedAt;
        this.activeViolation = activeViolation;
        this.reason = reason;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public Integer getFloorNumber() {
        return floorNumber;
    }

    public void setFloorNumber(Integer floorNumber) {
        this.floorNumber = floorNumber;
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

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public Integer getLatestScore() {
        return latestScore;
    }

    public void setLatestScore(Integer latestScore) {
        this.latestScore = latestScore;
    }

    public ComplianceClassification getLatestClassification() {
        return latestClassification;
    }

    public void setLatestClassification(ComplianceClassification latestClassification) {
        this.latestClassification = latestClassification;
    }

    public LocalDateTime getLastInspectedAt() {
        return lastInspectedAt;
    }

    public void setLastInspectedAt(LocalDateTime lastInspectedAt) {
        this.lastInspectedAt = lastInspectedAt;
    }

    public boolean isActiveViolation() {
        return activeViolation;
    }

    public void setActiveViolation(boolean activeViolation) {
        this.activeViolation = activeViolation;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
