package com.wastewatch.dto.report;

import java.util.List;

public class CoverageReportDto {

    private long totalRooms;
    private long inspectedRooms;
    private long uninspectedRooms;
    private double overallCoveragePercent;
    private List<AreaCoverageItem> areaCoverage;
    private List<BuildingCoverageItem> buildingCoverage;

    public CoverageReportDto() {
    }

    public static class AreaCoverageItem {
        private Long areaId;
        private String areaName;
        private long totalRooms;
        private long inspectedRooms;
        private double coveragePercent;

        public AreaCoverageItem() {}

        public AreaCoverageItem(Long areaId, String areaName, long totalRooms, long inspectedRooms, double coveragePercent) {
            this.areaId = areaId;
            this.areaName = areaName;
            this.totalRooms = totalRooms;
            this.inspectedRooms = inspectedRooms;
            this.coveragePercent = coveragePercent;
        }

        public Long getAreaId() { return areaId; }
        public void setAreaId(Long areaId) { this.areaId = areaId; }
        public String getAreaName() { return areaName; }
        public void setAreaName(String areaName) { this.areaName = areaName; }
        public long getTotalRooms() { return totalRooms; }
        public void setTotalRooms(long totalRooms) { this.totalRooms = totalRooms; }
        public long getInspectedRooms() { return inspectedRooms; }
        public void setInspectedRooms(long inspectedRooms) { this.inspectedRooms = inspectedRooms; }
        public double getCoveragePercent() { return coveragePercent; }
        public void setCoveragePercent(double coveragePercent) { this.coveragePercent = coveragePercent; }
    }

    public static class BuildingCoverageItem {
        private Long buildingId;
        private String buildingName;
        private String areaName;
        private long totalRooms;
        private long inspectedRooms;
        private double coveragePercent;

        public BuildingCoverageItem() {}

        public BuildingCoverageItem(Long buildingId, String buildingName, String areaName, long totalRooms, long inspectedRooms, double coveragePercent) {
            this.buildingId = buildingId;
            this.buildingName = buildingName;
            this.areaName = areaName;
            this.totalRooms = totalRooms;
            this.inspectedRooms = inspectedRooms;
            this.coveragePercent = coveragePercent;
        }

        public Long getBuildingId() { return buildingId; }
        public void setBuildingId(Long buildingId) { this.buildingId = buildingId; }
        public String getBuildingName() { return buildingName; }
        public void setBuildingName(String buildingName) { this.buildingName = buildingName; }
        public String getAreaName() { return areaName; }
        public void setAreaName(String areaName) { this.areaName = areaName; }
        public long getTotalRooms() { return totalRooms; }
        public void setTotalRooms(long totalRooms) { this.totalRooms = totalRooms; }
        public long getInspectedRooms() { return inspectedRooms; }
        public void setInspectedRooms(long inspectedRooms) { this.inspectedRooms = inspectedRooms; }
        public double getCoveragePercent() { return coveragePercent; }
        public void setCoveragePercent(double coveragePercent) { this.coveragePercent = coveragePercent; }
    }

    // Getters and Setters
    public long getTotalRooms() { return totalRooms; }
    public void setTotalRooms(long totalRooms) { this.totalRooms = totalRooms; }
    public long getInspectedRooms() { return inspectedRooms; }
    public void setInspectedRooms(long inspectedRooms) { this.inspectedRooms = inspectedRooms; }
    public long getUninspectedRooms() { return uninspectedRooms; }
    public void setUninspectedRooms(long uninspectedRooms) { this.uninspectedRooms = uninspectedRooms; }
    public double getOverallCoveragePercent() { return overallCoveragePercent; }
    public void setOverallCoveragePercent(double overallCoveragePercent) { this.overallCoveragePercent = overallCoveragePercent; }
    public List<AreaCoverageItem> getAreaCoverage() { return areaCoverage; }
    public void setAreaCoverage(List<AreaCoverageItem> areaCoverage) { this.areaCoverage = areaCoverage; }
    public List<BuildingCoverageItem> getBuildingCoverage() { return buildingCoverage; }
    public void setBuildingCoverage(List<BuildingCoverageItem> buildingCoverage) { this.buildingCoverage = buildingCoverage; }
}
