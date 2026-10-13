package com.wastewatch.dto.analytics;

import java.util.Map;

public class OverviewAnalyticsDto {
    private long totalInspections;
    private long totalRooms;
    private long inspectedRooms;
    private long totalAreas;
    private long totalBuildings;
    private long totalVolunteers;
    private double overallComplianceRate;
    private double overallInspectionCoverage;
    private double overallViolationResolutionRate;
    private double systemHealthScore;
    private long openViolationsCount;
    private long resolvedViolationsCount;
    private Map<String, Long> classificationCounts;

    public OverviewAnalyticsDto() {
    }

    public OverviewAnalyticsDto(long totalInspections, long totalRooms, long inspectedRooms,
                                long totalAreas, long totalBuildings, long totalVolunteers,
                                double overallComplianceRate, double overallInspectionCoverage,
                                double overallViolationResolutionRate, double systemHealthScore,
                                long openViolationsCount, long resolvedViolationsCount,
                                Map<String, Long> classificationCounts) {
        this.totalInspections = totalInspections;
        this.totalRooms = totalRooms;
        this.inspectedRooms = inspectedRooms;
        this.totalAreas = totalAreas;
        this.totalBuildings = totalBuildings;
        this.totalVolunteers = totalVolunteers;
        this.overallComplianceRate = overallComplianceRate;
        this.overallInspectionCoverage = overallInspectionCoverage;
        this.overallViolationResolutionRate = overallViolationResolutionRate;
        this.systemHealthScore = systemHealthScore;
        this.openViolationsCount = openViolationsCount;
        this.resolvedViolationsCount = resolvedViolationsCount;
        this.classificationCounts = classificationCounts;
    }

    public long getTotalInspections() {
        return totalInspections;
    }

    public void setTotalInspections(long totalInspections) {
        this.totalInspections = totalInspections;
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

    public long getTotalVolunteers() {
        return totalVolunteers;
    }

    public void setTotalVolunteers(long totalVolunteers) {
        this.totalVolunteers = totalVolunteers;
    }

    public double getOverallComplianceRate() {
        return overallComplianceRate;
    }

    public void setOverallComplianceRate(double overallComplianceRate) {
        this.overallComplianceRate = overallComplianceRate;
    }

    public double getOverallInspectionCoverage() {
        return overallInspectionCoverage;
    }

    public void setOverallInspectionCoverage(double overallInspectionCoverage) {
        this.overallInspectionCoverage = overallInspectionCoverage;
    }

    public double getOverallViolationResolutionRate() {
        return overallViolationResolutionRate;
    }

    public void setOverallViolationResolutionRate(double overallViolationResolutionRate) {
        this.overallViolationResolutionRate = overallViolationResolutionRate;
    }

    public double getSystemHealthScore() {
        return systemHealthScore;
    }

    public void setSystemHealthScore(double systemHealthScore) {
        this.systemHealthScore = systemHealthScore;
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

    public Map<String, Long> getClassificationCounts() {
        return classificationCounts;
    }

    public void setClassificationCounts(Map<String, Long> classificationCounts) {
        this.classificationCounts = classificationCounts;
    }
}
