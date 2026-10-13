package com.wastewatch.dto.analytics;

public class BuildingAnalyticsDto {
    private Long buildingId;
    private String buildingName;
    private String buildingCode;
    private String buildingType;
    private Long areaId;
    private String areaName;
    private Double latitude;
    private Double longitude;
    private long totalRooms;
    private long inspectedRooms;
    private long totalInspections;
    private double inspectionCoverage;
    private double averageScore;
    private String complianceStatus;
    private long poorRoomCount;
    private long openViolationsCount;
    private long resolvedViolationsCount;

    public BuildingAnalyticsDto() {
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

    public String getBuildingCode() {
        return buildingCode;
    }

    public void setBuildingCode(String buildingCode) {
        this.buildingCode = buildingCode;
    }

    public String getBuildingType() {
        return buildingType;
    }

    public void setBuildingType(String buildingType) {
        this.buildingType = buildingType;
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

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public long getTotalRooms() {
        return totalRooms;
    }

    public void setTotalRooms(long totalRooms) {
        this.totalRooms = totalRooms;
    }

    public long getInspectedRooms() {
        return inspectedRooms;
    }

    public void setInspectedRooms(long inspectedRooms) {
        this.inspectedRooms = inspectedRooms;
    }

    public long getTotalInspections() {
        return totalInspections;
    }

    public void setTotalInspections(long totalInspections) {
        this.totalInspections = totalInspections;
    }

    public double getInspectionCoverage() {
        return inspectionCoverage;
    }

    public void setInspectionCoverage(double inspectionCoverage) {
        this.inspectionCoverage = inspectionCoverage;
    }

    public double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(double averageScore) {
        this.averageScore = averageScore;
    }

    public String getComplianceStatus() {
        return complianceStatus;
    }

    public void setComplianceStatus(String complianceStatus) {
        this.complianceStatus = complianceStatus;
    }

    public long getPoorRoomCount() {
        return poorRoomCount;
    }

    public void setPoorRoomCount(long poorRoomCount) {
        this.poorRoomCount = poorRoomCount;
    }

    public long getOpenViolationsCount() {
        return openViolationsCount;
    }

    public void setOpenViolationsCount(long openViolationsCount) {
        this.openViolationsCount = openViolationsCount;
    }

    public long getResolvedViolationsCount() {
        return resolvedViolationsCount;
    }

    public void setResolvedViolationsCount(long resolvedViolationsCount) {
        this.resolvedViolationsCount = resolvedViolationsCount;
    }
}
