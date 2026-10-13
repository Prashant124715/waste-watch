package com.wastewatch.dto.map;

import com.wastewatch.entity.enums.BuildingType;
import com.wastewatch.entity.enums.ComplianceClassification;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BuildingMapMarkerDto {

    private Long id;
    private String name;
    private String buildingCode;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Long areaId;
    private String areaName;
    private String areaCode;
    private BuildingType buildingType;
    private Integer totalFloors;
    private Integer totalRooms;
    private Integer inspectedRooms;
    private Double inspectionCoverage;
    private Double latestScore;
    private Double complianceScore;
    private ComplianceClassification healthStatus;
    private String complianceStatus;
    private int openViolations;
    private long totalInspections;
    private long poorRoomCount;
    private LocalDateTime lastInspectionDate;
    private boolean hasValidCoordinates;

    public BuildingMapMarkerDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBuildingCode() {
        return buildingCode;
    }

    public void setBuildingCode(String buildingCode) {
        this.buildingCode = buildingCode;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
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

    public BuildingType getBuildingType() {
        return buildingType;
    }

    public void setBuildingType(BuildingType buildingType) {
        this.buildingType = buildingType;
    }

    public Integer getTotalFloors() {
        return totalFloors;
    }

    public void setTotalFloors(Integer totalFloors) {
        this.totalFloors = totalFloors;
    }

    public Integer getTotalRooms() {
        return totalRooms;
    }

    public void setTotalRooms(Integer totalRooms) {
        this.totalRooms = totalRooms;
    }

    public Integer getInspectedRooms() {
        return inspectedRooms;
    }

    public void setInspectedRooms(Integer inspectedRooms) {
        this.inspectedRooms = inspectedRooms;
    }

    public Double getInspectionCoverage() {
        return inspectionCoverage;
    }

    public void setInspectionCoverage(Double inspectionCoverage) {
        this.inspectionCoverage = inspectionCoverage;
    }

    public Double getLatestScore() {
        return latestScore;
    }

    public void setLatestScore(Double latestScore) {
        this.latestScore = latestScore;
        this.complianceScore = latestScore;
    }

    public Double getComplianceScore() {
        return complianceScore;
    }

    public void setComplianceScore(Double complianceScore) {
        this.complianceScore = complianceScore;
        this.latestScore = complianceScore;
    }

    public ComplianceClassification getHealthStatus() {
        return healthStatus;
    }

    public void setHealthStatus(ComplianceClassification healthStatus) {
        this.healthStatus = healthStatus;
        this.complianceStatus = healthStatus != null ? healthStatus.name() : "NO_DATA";
    }

    public String getComplianceStatus() {
        return complianceStatus;
    }

    public void setComplianceStatus(String complianceStatus) {
        this.complianceStatus = complianceStatus;
    }

    public int getOpenViolations() {
        return openViolations;
    }

    public void setOpenViolations(int openViolations) {
        this.openViolations = openViolations;
    }

    public long getTotalInspections() {
        return totalInspections;
    }

    public void setTotalInspections(long totalInspections) {
        this.totalInspections = totalInspections;
    }

    public long getPoorRoomCount() {
        return poorRoomCount;
    }

    public void setPoorRoomCount(long poorRoomCount) {
        this.poorRoomCount = poorRoomCount;
    }

    public LocalDateTime getLastInspectionDate() {
        return lastInspectionDate;
    }

    public void setLastInspectionDate(LocalDateTime lastInspectionDate) {
        this.lastInspectionDate = lastInspectionDate;
    }

    public boolean isHasValidCoordinates() {
        return hasValidCoordinates;
    }

    public void setHasValidCoordinates(boolean hasValidCoordinates) {
        this.hasValidCoordinates = hasValidCoordinates;
    }
}
