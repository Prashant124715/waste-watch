package com.wastewatch.dto.analytics;

public class TrendPointDto {
    private String periodKey;
    private String label;
    private long inspectionCount;
    private double averageScore;
    private long excellentCount;
    private long goodCount;
    private long needsImprovementCount;
    private long poorCount;
    private long violationsCount;

    public TrendPointDto() {
    }

    public TrendPointDto(String periodKey, String label, long inspectionCount, double averageScore,
                         long excellentCount, long goodCount, long needsImprovementCount,
                         long poorCount, long violationsCount) {
        this.periodKey = periodKey;
        this.label = label;
        this.inspectionCount = inspectionCount;
        this.averageScore = averageScore;
        this.excellentCount = excellentCount;
        this.goodCount = goodCount;
        this.needsImprovementCount = needsImprovementCount;
        this.poorCount = poorCount;
        this.violationsCount = violationsCount;
    }

    public String getPeriodKey() {
        return periodKey;
    }

    public void setPeriodKey(String periodKey) {
        this.periodKey = periodKey;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public long getInspectionCount() {
        return inspectionCount;
    }

    public void setInspectionCount(long inspectionCount) {
        this.inspectionCount = inspectionCount;
    }

    public double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(double averageScore) {
        this.averageScore = averageScore;
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

    public long getViolationsCount() {
        return violationsCount;
    }

    public void setViolationsCount(long violationsCount) {
        this.violationsCount = violationsCount;
    }
}
