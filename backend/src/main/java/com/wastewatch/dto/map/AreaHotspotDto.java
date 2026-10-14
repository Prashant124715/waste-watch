package com.wastewatch.dto.map;

import com.wastewatch.entity.enums.ComplianceClassification;

import java.math.BigDecimal;

public class AreaHotspotDto {

    private Long areaId;
    private String areaName;
    private String areaCode;
    private int totalBuildings;
    private int mappedBuildings;
    private BigDecimal centerLatitude;
    private BigDecimal centerLongitude;
    private Double averageScore;
    private String complianceStatus;
    private ComplianceClassification healthStatus;
    private double inspectionCoverage;
    private int poorRooms;
    private int openViolations;
    private boolean isHotspot;
    private String hotspotSeverity;

    public AreaHotspotDto() {
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

    public int getTotalBuildings() {
        return totalBuildings;
    }

    public void setTotalBuildings(int totalBuildings) {
        this.totalBuildings = totalBuildings;
    }

    public int getMappedBuildings() {
        return mappedBuildings;
    }

    public void setMappedBuildings(int mappedBuildings) {
        this.mappedBuildings = mappedBuildings;
    }

    public BigDecimal getCenterLatitude() {
        return centerLatitude;
    }

    public void setCenterLatitude(BigDecimal centerLatitude) {
        this.centerLatitude = centerLatitude;
    }

    public BigDecimal getCenterLongitude() {
        return centerLongitude;
    }

    public void setCenterLongitude(BigDecimal centerLongitude) {
        this.centerLongitude = centerLongitude;
    }

    public Double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(Double averageScore) {
        this.averageScore = averageScore;
    }

    public String getComplianceStatus() {
        return complianceStatus;
    }

    public void setComplianceStatus(String complianceStatus) {
        this.complianceStatus = complianceStatus;
    }

    public ComplianceClassification getHealthStatus() {
        return healthStatus;
    }

    public void setHealthStatus(ComplianceClassification healthStatus) {
        this.healthStatus = healthStatus;
    }

    public double getInspectionCoverage() {
        return inspectionCoverage;
    }

    public void setInspectionCoverage(double inspectionCoverage) {
        this.inspectionCoverage = inspectionCoverage;
    }

    public int getPoorRooms() {
        return poorRooms;
    }

    public void setPoorRooms(int poorRooms) {
        this.poorRooms = poorRooms;
    }

    public int getOpenViolations() {
        return openViolations;
    }

    public void setOpenViolations(int openViolations) {
        this.openViolations = openViolations;
    }

    public boolean isHotspot() {
        return isHotspot;
    }

    public void setHotspot(boolean hotspot) {
        isHotspot = hotspot;
    }

    public String getHotspotSeverity() {
        return hotspotSeverity;
    }

    public void setHotspotSeverity(String hotspotSeverity) {
        this.hotspotSeverity = hotspotSeverity;
    }
}
