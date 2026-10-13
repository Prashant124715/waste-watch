package com.wastewatch.dto.dashboard;

public class ComplianceDistributionDto {

    private double averageScore;
    private long totalInspections;
    private long excellentCount;
    private long goodCount;
    private long needsImprovementCount;
    private long poorCount;
    private double excellentPercent;
    private double goodPercent;
    private double needsImprovementPercent;
    private double poorPercent;

    public ComplianceDistributionDto() {
    }

    public ComplianceDistributionDto(double averageScore, long totalInspections, long excellentCount,
                                     long goodCount, long needsImprovementCount, long poorCount,
                                     double excellentPercent, double goodPercent,
                                     double needsImprovementPercent, double poorPercent) {
        this.averageScore = averageScore;
        this.totalInspections = totalInspections;
        this.excellentCount = excellentCount;
        this.goodCount = goodCount;
        this.needsImprovementCount = needsImprovementCount;
        this.poorCount = poorCount;
        this.excellentPercent = excellentPercent;
        this.goodPercent = goodPercent;
        this.needsImprovementPercent = needsImprovementPercent;
        this.poorPercent = poorPercent;
    }

    public double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(double averageScore) {
        this.averageScore = averageScore;
    }

    public long getTotalInspections() {
        return totalInspections;
    }

    public void setTotalInspections(long totalInspections) {
        this.totalInspections = totalInspections;
    }

    public long getExcellentCount() {
        return excellentCount;
    }

    public void setExcellentCount(long excellentCount) {
        this.excellentCount = excellentCount;
    }

    public long getGoodCount() {
        return goodCount;
    }

    public void setGoodCount(long goodCount) {
        this.goodCount = goodCount;
    }

    public long getNeedsImprovementCount() {
        return needsImprovementCount;
    }

    public void setNeedsImprovementCount(long needsImprovementCount) {
        this.needsImprovementCount = needsImprovementCount;
    }

    public long getPoorCount() {
        return poorCount;
    }

    public void setPoorCount(long poorCount) {
        this.poorCount = poorCount;
    }

    public double getExcellentPercent() {
        return excellentPercent;
    }

    public void setExcellentPercent(double excellentPercent) {
        this.excellentPercent = excellentPercent;
    }

    public double getGoodPercent() {
        return goodPercent;
    }

    public void setGoodPercent(double goodPercent) {
        this.goodPercent = goodPercent;
    }

    public double getNeedsImprovementPercent() {
        return needsImprovementPercent;
    }

    public void setNeedsImprovementPercent(double needsImprovementPercent) {
        this.needsImprovementPercent = needsImprovementPercent;
    }

    public double getPoorPercent() {
        return poorPercent;
    }

    public void setPoorPercent(double poorPercent) {
        this.poorPercent = poorPercent;
    }
}
