package com.wastewatch.dto.analytics;

public class AreaImprovementDto {
    private Long areaId;
    private String areaName;
    private String areaCode;
    private String recentPeriodLabel;
    private String previousPeriodLabel;
    private double recentAverageScore;
    private double previousAverageScore;
    private double scoreDelta;
    private double recentCoverage;
    private double previousCoverage;
    private double coverageDelta;
    private long recentInspectionsCount;
    private long previousInspectionsCount;
    private String trendStatus; // IMPROVED, DECLINED, STABLE, INSUFFICIENT_DATA

    public AreaImprovementDto() {
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

    public String getRecentPeriodLabel() {
        return recentPeriodLabel;
    }

    public void setRecentPeriodLabel(String recentPeriodLabel) {
        this.recentPeriodLabel = recentPeriodLabel;
    }

    public String getPreviousPeriodLabel() {
        return previousPeriodLabel;
    }

    public void setPreviousPeriodLabel(String previousPeriodLabel) {
        this.previousPeriodLabel = previousPeriodLabel;
    }

    public double getRecentAverageScore() {
        return recentAverageScore;
    }

    public void setRecentAverageScore(double recentAverageScore) {
        this.recentAverageScore = recentAverageScore;
    }

    public double getPreviousAverageScore() {
        return previousAverageScore;
    }

    public void setPreviousAverageScore(double previousAverageScore) {
        this.previousAverageScore = previousAverageScore;
    }

    public double getScoreDelta() {
        return scoreDelta;
    }

    public void setScoreDelta(double scoreDelta) {
        this.scoreDelta = scoreDelta;
    }

    public double getRecentCoverage() {
        return recentCoverage;
    }

    public void setRecentCoverage(double recentCoverage) {
        this.recentCoverage = recentCoverage;
    }

    public double getPreviousCoverage() {
        return previousCoverage;
    }

    public void setPreviousCoverage(double previousCoverage) {
        this.previousCoverage = previousCoverage;
    }

    public double getCoverageDelta() {
        return coverageDelta;
    }

    public void setCoverageDelta(double coverageDelta) {
        this.coverageDelta = coverageDelta;
    }

    public long getRecentInspectionsCount() {
        return recentInspectionsCount;
    }

    public void setRecentInspectionsCount(long recentInspectionsCount) {
        this.recentInspectionsCount = recentInspectionsCount;
    }

    public long getPreviousInspectionsCount() {
        return previousInspectionsCount;
    }

    public void setPreviousInspectionsCount(long previousInspectionsCount) {
        this.previousInspectionsCount = previousInspectionsCount;
    }

    public String getTrendStatus() {
        return trendStatus;
    }

    public void setTrendStatus(String trendStatus) {
        this.trendStatus = trendStatus;
    }
}
