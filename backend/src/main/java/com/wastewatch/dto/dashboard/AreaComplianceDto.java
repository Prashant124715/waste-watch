package com.wastewatch.dto.dashboard;

import com.wastewatch.entity.enums.ComplianceClassification;

public class AreaComplianceDto {

    private Long areaId;
    private String areaCode;
    private String name;
    private String city;
    private long totalBuildings;
    private long totalRooms;
    private long inspectionsCount;
    private double averageComplianceScore;
    private long openViolationsCount;
    private ComplianceClassification status;

    public AreaComplianceDto() {
    }

    public AreaComplianceDto(Long areaId, String areaCode, String name, String city, long totalBuildings,
                             long totalRooms, long inspectionsCount, double averageComplianceScore,
                             long openViolationsCount, ComplianceClassification status) {
        this.areaId = areaId;
        this.areaCode = areaCode;
        this.name = name;
        this.city = city;
        this.totalBuildings = totalBuildings;
        this.totalRooms = totalRooms;
        this.inspectionsCount = inspectionsCount;
        this.averageComplianceScore = averageComplianceScore;
        this.openViolationsCount = openViolationsCount;
        this.status = status;
    }

    public Long getAreaId() {
        return areaId;
    }

    public void setAreaId(Long areaId) {
        this.areaId = areaId;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public long getInspectionsCount() {
        return inspectionsCount;
    }

    public void setInspectionsCount(long inspectionsCount) {
        this.inspectionsCount = inspectionsCount;
    }

    public double getAverageComplianceScore() {
        return averageComplianceScore;
    }

    public void setAverageComplianceScore(double averageComplianceScore) {
        this.averageComplianceScore = averageComplianceScore;
    }

    public long getOpenViolationsCount() {
        return openViolationsCount;
    }

    public void setOpenViolationsCount(long openViolationsCount) {
        this.openViolationsCount = openViolationsCount;
    }

    public ComplianceClassification getStatus() {
        return status;
    }

    public void setStatus(ComplianceClassification status) {
        this.status = status;
    }
}
