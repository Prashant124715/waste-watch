package com.wastewatch.dto.report;

public class AreaPerformanceReportDto {

    private Long areaId;
    private String areaCode;
    private String areaName;
    private String city;
    private long totalBuildings;
    private long totalRooms;
    private long inspectedRooms;
    private double coveragePercent;
    private long totalInspections;
    private double averageScore;
    private long excellentCount;
    private long goodCount;
    private long needsImprovementCount;
    private long poorCount;
    private long openViolations;
    private long resolvedViolations;
    private double healthScore;

    public AreaPerformanceReportDto() {
    }

    // Getters and Setters
    public Long getAreaId() { return areaId; }
    public void setAreaId(Long areaId) { this.areaId = areaId; }

    public String getAreaCode() { return areaCode; }
    public void setAreaCode(String areaCode) { this.areaCode = areaCode; }

    public String getAreaName() { return areaName; }
    public void setAreaName(String areaName) { this.areaName = areaName; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public long getTotalBuildings() { return totalBuildings; }
    public void setTotalBuildings(long totalBuildings) { this.totalBuildings = totalBuildings; }

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

    public long getExcellentCount() { return excellentCount; }
    public void setExcellentCount(long excellentCount) { this.excellentCount = excellentCount; }

    public long getGoodCount() { return goodCount; }
    public void setGoodCount(long goodCount) { this.goodCount = goodCount; }

    public long getNeedsImprovementCount() { return needsImprovementCount; }
    public void setNeedsImprovementCount(long needsImprovementCount) { this.needsImprovementCount = needsImprovementCount; }

    public long getPoorCount() { return poorCount; }
    public void setPoorCount(long poorCount) { this.poorCount = poorCount; }

    public long getOpenViolations() { return openViolations; }
    public void setOpenViolations(long openViolations) { this.openViolations = openViolations; }

    public long getResolvedViolations() { return resolvedViolations; }
    public void setResolvedViolations(long resolvedViolations) { this.resolvedViolations = resolvedViolations; }

    public double getHealthScore() { return healthScore; }
    public void setHealthScore(double healthScore) { this.healthScore = healthScore; }
}
