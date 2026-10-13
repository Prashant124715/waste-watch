package com.wastewatch.dto.analytics;

import java.time.LocalDateTime;

public class RepeatOffenderRoomDto {
    private Long roomId;
    private String roomNumber;
    private Integer floorNumber;
    private Long buildingId;
    private String buildingName;
    private Long areaId;
    private String areaName;
    private long failedInspectionsCount;
    private long totalInspectionsCount;
    private double failureRate;
    private Integer latestScore;
    private String latestClassification;
    private LocalDateTime lastInspectionDate;
    private long openViolationsCount;
    private String riskLevel; // CRITICAL, HIGH, MEDIUM

    public RepeatOffenderRoomDto() {
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

    public long getFailedInspectionsCount() {
        return failedInspectionsCount;
    }

    public void setFailedInspectionsCount(long failedInspectionsCount) {
        this.failedInspectionsCount = failedInspectionsCount;
    }

    public long getTotalInspectionsCount() {
        return totalInspectionsCount;
    }

    public void setTotalInspectionsCount(long totalInspectionsCount) {
        this.totalInspectionsCount = totalInspectionsCount;
    }

    public double getFailureRate() {
        return failureRate;
    }

    public void setFailureRate(double failureRate) {
        this.failureRate = failureRate;
    }

    public Integer getLatestScore() {
        return latestScore;
    }

    public void setLatestScore(Integer latestScore) {
        this.latestScore = latestScore;
    }

    public String getLatestClassification() {
        return latestClassification;
    }

    public void setLatestClassification(String latestClassification) {
        this.latestClassification = latestClassification;
    }

    public LocalDateTime getLastInspectionDate() {
        return lastInspectionDate;
    }

    public void setLastInspectionDate(LocalDateTime lastInspectionDate) {
        this.lastInspectionDate = lastInspectionDate;
    }

    public long getOpenViolationsCount() {
        return openViolationsCount;
    }

    public void setOpenViolationsCount(long openViolationsCount) {
        this.openViolationsCount = openViolationsCount;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }
}
