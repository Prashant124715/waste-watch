package com.wastewatch.dto.report;

import java.time.LocalDateTime;

public class OverviewComplianceReportDto {

    private LocalDateTime from;
    private LocalDateTime to;
    private long totalInspections;
    private double averageScore;
    private double complianceRate;
    private long excellentCount;
    private long goodCount;
    private long needsImprovementCount;
    private long poorCount;
    private long totalRooms;
    private long inspectedRooms;
    private double coveragePercent;
    private long totalViolations;
    private long openViolations;
    private long resolvedViolations;
    private double resolutionRate;
    private long activeVolunteers;

    public OverviewComplianceReportDto() {
    }

    // Getters and Setters
    public LocalDateTime getFrom() { return from; }
    public void setFrom(LocalDateTime from) { this.from = from; }

    public LocalDateTime getTo() { return to; }
    public void setTo(LocalDateTime to) { this.to = to; }

    public long getTotalInspections() { return totalInspections; }
    public void setTotalInspections(long totalInspections) { this.totalInspections = totalInspections; }

    public double getAverageScore() { return averageScore; }
    public void setAverageScore(double averageScore) { this.averageScore = averageScore; }

    public double getComplianceRate() { return complianceRate; }
    public void setComplianceRate(double complianceRate) { this.complianceRate = complianceRate; }

    public long getExcellentCount() { return excellentCount; }
    public void setExcellentCount(long excellentCount) { this.excellentCount = excellentCount; }

    public long getGoodCount() { return goodCount; }
    public void setGoodCount(long goodCount) { this.goodCount = goodCount; }

    public long getNeedsImprovementCount() { return needsImprovementCount; }
    public void setNeedsImprovementCount(long needsImprovementCount) { this.needsImprovementCount = needsImprovementCount; }

    public long getPoorCount() { return poorCount; }
    public void setPoorCount(long poorCount) { this.poorCount = poorCount; }

    public long getTotalRooms() { return totalRooms; }
    public void setTotalRooms(long totalRooms) { this.totalRooms = totalRooms; }

    public long getInspectedRooms() { return inspectedRooms; }
    public void setInspectedRooms(long inspectedRooms) { this.inspectedRooms = inspectedRooms; }

    public double getCoveragePercent() { return coveragePercent; }
    public void setCoveragePercent(double coveragePercent) { this.coveragePercent = coveragePercent; }

    public long getTotalViolations() { return totalViolations; }
    public void setTotalViolations(long totalViolations) { this.totalViolations = totalViolations; }

    public long getOpenViolations() { return openViolations; }
    public void setOpenViolations(long openViolations) { this.openViolations = openViolations; }

    public long getResolvedViolations() { return resolvedViolations; }
    public void setResolvedViolations(long resolvedViolations) { this.resolvedViolations = resolvedViolations; }

    public double getResolutionRate() { return resolutionRate; }
    public void setResolutionRate(double resolutionRate) { this.resolutionRate = resolutionRate; }

    public long getActiveVolunteers() { return activeVolunteers; }
    public void setActiveVolunteers(long activeVolunteers) { this.activeVolunteers = activeVolunteers; }
}
