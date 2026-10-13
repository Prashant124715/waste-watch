package com.wastewatch.dto.report;

public class BuildingPerformanceReportDto {

    private Long buildingId;
    private String buildingCode;
    private String buildingName;
    private String areaName;
    private String buildingType;
    private int totalFloors;
    private long totalRooms;
    private long inspectedRooms;
    private double coveragePercent;
    private long totalInspections;
    private double averageScore;
    private String complianceStatus;
    private long openViolations;
    private long resolvedViolations;

    public BuildingPerformanceReportDto() {
    }

    // Getters and Setters
    public Long getBuildingId() { return buildingId; }
    public void setBuildingId(Long buildingId) { this.buildingId = buildingId; }

    public String getBuildingCode() { return buildingCode; }
    public void setBuildingCode(String buildingCode) { this.buildingCode = buildingCode; }

    public String getBuildingName() { return buildingName; }
    public void setBuildingName(String buildingName) { this.buildingName = buildingName; }

    public String getAreaName() { return areaName; }
    public void setAreaName(String areaName) { this.areaName = areaName; }

    public String getBuildingType() { return buildingType; }
    public void setBuildingType(String buildingType) { this.buildingType = buildingType; }

    public int getTotalFloors() { return totalFloors; }
    public void setTotalFloors(int totalFloors) { this.totalFloors = totalFloors; }

    public long getTotalRooms() { return totalRooms; }
    public void setTotalRooms(long totalRooms) { this.totalRooms = totalRooms; }

    public long getInspectedRooms() { return inspectedRooms; }
    public void setInspectedRooms(long inspectedRooms) { this.inspectedRooms = inspectedRooms; }

    public double getCoveragePercent() { return coveragePercent; }
    public void setCoveragePercent(double coveragePercent) { this.coveragePercent = coveragePercent; }

    public long getTotalInspections() { return totalInspections; }
    public void setTotalInspections(long totalInspections) { this.totalInspections = totalInspections; }

    public double getAverageScore() { return averageScore; }
    public void setAverageScore(double averageScore) { this.averageScore = averageScore; }

    public String getComplianceStatus() { return complianceStatus; }
    public void setComplianceStatus(String complianceStatus) { this.complianceStatus = complianceStatus; }

    public long getOpenViolations() { return openViolations; }
    public void setOpenViolations(long openViolations) { this.openViolations = openViolations; }

    public long getResolvedViolations() { return resolvedViolations; }
    public void setResolvedViolations(long resolvedViolations) { this.resolvedViolations = resolvedViolations; }
}
