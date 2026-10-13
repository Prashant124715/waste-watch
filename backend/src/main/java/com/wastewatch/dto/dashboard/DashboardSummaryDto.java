package com.wastewatch.dto.dashboard;

import java.util.Map;

public class DashboardSummaryDto {

    private long totalAreas;
    private long totalBuildings;
    private long totalRooms;
    private long totalInspections;
    private long inspectionsToday;
    private double overallComplianceRate;
    private long poorRoomsCount;
    private long openViolationsCount;
    private double inspectionCoveragePercent;
    private long totalAreasAudited;
    private long totalBuildingsAudited;
    private long totalRoomsCovered;
    private long activeVolunteersCount;
    private Map<String, Long> scoreDistribution;

    public DashboardSummaryDto() {
    }

    public DashboardSummaryDto(long totalAreas, long totalBuildings, long totalRooms, long totalInspections,
                               long inspectionsToday, double overallComplianceRate, long poorRoomsCount,
                               long openViolationsCount, double inspectionCoveragePercent, long totalAreasAudited,
                               long totalBuildingsAudited, long totalRoomsCovered, long activeVolunteersCount,
                               Map<String, Long> scoreDistribution) {
        this.totalAreas = totalAreas;
        this.totalBuildings = totalBuildings;
        this.totalRooms = totalRooms;
        this.totalInspections = totalInspections;
        this.inspectionsToday = inspectionsToday;
        this.overallComplianceRate = overallComplianceRate;
        this.poorRoomsCount = poorRoomsCount;
        this.openViolationsCount = openViolationsCount;
        this.inspectionCoveragePercent = inspectionCoveragePercent;
        this.totalAreasAudited = totalAreasAudited;
        this.totalBuildingsAudited = totalBuildingsAudited;
        this.totalRoomsCovered = totalRoomsCovered;
        this.activeVolunteersCount = activeVolunteersCount;
        this.scoreDistribution = scoreDistribution;
    }

    public long getTotalAreas() {
        return totalAreas;
    }

    public void setTotalAreas(long totalAreas) {
        this.totalAreas = totalAreas;
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

    public long getTotalInspections() {
        return totalInspections;
    }

    public void setTotalInspections(long totalInspections) {
        this.totalInspections = totalInspections;
    }

    public long getInspectionsToday() {
        return inspectionsToday;
    }

    public void setInspectionsToday(long inspectionsToday) {
        this.inspectionsToday = inspectionsToday;
    }

    public double getOverallComplianceRate() {
        return overallComplianceRate;
    }

    public void setOverallComplianceRate(double overallComplianceRate) {
        this.overallComplianceRate = overallComplianceRate;
    }

    public long getPoorRoomsCount() {
        return poorRoomsCount;
    }

    public void setPoorRoomsCount(long poorRoomsCount) {
        this.poorRoomsCount = poorRoomsCount;
    }

    public long getOpenViolationsCount() {
        return openViolationsCount;
    }

    public void setOpenViolationsCount(long openViolationsCount) {
        this.openViolationsCount = openViolationsCount;
    }

    public double getInspectionCoveragePercent() {
        return inspectionCoveragePercent;
    }

    public void setInspectionCoveragePercent(double inspectionCoveragePercent) {
        this.inspectionCoveragePercent = inspectionCoveragePercent;
    }

    public long getTotalAreasAudited() {
        return totalAreasAudited;
    }

    public void setTotalAreasAudited(long totalAreasAudited) {
        this.totalAreasAudited = totalAreasAudited;
    }

    public long getTotalBuildingsAudited() {
        return totalBuildingsAudited;
    }

    public void setTotalBuildingsAudited(long totalBuildingsAudited) {
        this.totalBuildingsAudited = totalBuildingsAudited;
    }

    public long getTotalRoomsCovered() {
        return totalRoomsCovered;
    }

    public void setTotalRoomsCovered(long totalRoomsCovered) {
        this.totalRoomsCovered = totalRoomsCovered;
    }

    public long getActiveVolunteersCount() {
        return activeVolunteersCount;
    }

    public void setActiveVolunteersCount(long activeVolunteersCount) {
        this.activeVolunteersCount = activeVolunteersCount;
    }

    public Map<String, Long> getScoreDistribution() {
        return scoreDistribution;
    }

    public void setScoreDistribution(Map<String, Long> scoreDistribution) {
        this.scoreDistribution = scoreDistribution;
    }
}
