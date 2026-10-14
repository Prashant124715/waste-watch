package com.wastewatch.dto.map;

/**
 * DTO used by GET /api/map/areas.
 * Provides area-level aggregate data for the geographic hotspot analysis
 * panel on the admin map view.
 */
public class AreaMapSummaryDto {

    private Long areaId;
    private String areaName;
    private String areaCode;
    private String city;
    private long totalBuildings;
    private long totalRooms;
    private long inspectedRooms;
    private double inspectionCoverage;
    private double averageCompliance;
    private String complianceStatus;
    private long poorRoomCount;
    private long openViolations;
    private double areaHealthScore;
    private String confidenceRating;

    public AreaMapSummaryDto() {
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

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
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

    public long getOpenViolations() {
        return openViolations;
    }

    public void setOpenViolations(long openViolations) {
        this.openViolations = openViolations;
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
