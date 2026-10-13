package com.wastewatch.dto.analytics;

public class AreaAnalyticsDto {
    private Long areaId;
    private String areaName;
    private String areaCode;
    private String description;
    private long totalBuildings;
    private long totalRooms;
    private long inspectedRooms;
    private long totalInspections;
    private double inspectionCoverage;
    private double averageCompliance;
    private String complianceStatus;
    private long poorRoomCount;
    private long totalViolations;
    private long openViolations;
    private long resolvedViolations;
    private double violationResolutionRate;
    private double areaHealthScore;
    private String confidenceRating;

    public AreaAnalyticsDto() {
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

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getTotalBuildings() {
        return totalBuildings;
    }

    public void setTotalBuildings(long totalBuildings) {
        this.totalBuildings = totalBuildings;
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

    public double getAverageCompliance() {
        return averageCompliance;
    }

    public void setAverageCompliance(double averageCompliance) {
        this.averageCompliance = averageCompliance;
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

    public long getTotalViolations() {
        return totalViolations;
    }

    public void setTotalViolations(long totalViolations) {
        this.totalViolations = totalViolations;
    }

    public long getOpenViolations() {
        return openViolations;
    }

    public void setOpenViolations(long openViolations) {
        this.openViolations = openViolations;
    }

    public long getResolvedViolations() {
        return resolvedViolations;
    }

    public void setResolvedViolations(long resolvedViolations) {
        this.resolvedViolations = resolvedViolations;
    }

    public double getViolationResolutionRate() {
        return violationResolutionRate;
    }

    public void setViolationResolutionRate(double violationResolutionRate) {
        this.violationResolutionRate = violationResolutionRate;
    }

    public double getAreaHealthScore() {
        return areaHealthScore;
    }

    public void setAreaHealthScore(double areaHealthScore) {
        this.areaHealthScore = areaHealthScore;
    }

    public String getConfidenceRating() {
        return confidenceRating;
    }

    public void setConfidenceRating(String confidenceRating) {
        this.confidenceRating = confidenceRating;
    }
}
