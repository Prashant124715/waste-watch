package com.wastewatch.dto.dashboard;

public class DailyTrendDto {

    private String date;
    private double averageScore;
    private long inspectionsCount;

    public DailyTrendDto() {
    }

    public DailyTrendDto(String date, double averageScore, long inspectionsCount) {
        this.date = date;
        this.averageScore = averageScore;
        this.inspectionsCount = inspectionsCount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(double averageScore) {
        this.averageScore = averageScore;
    }

    public long getInspectionsCount() {
        return inspectionsCount;
    }

    public void setInspectionsCount(long inspectionsCount) {
        this.inspectionsCount = inspectionsCount;
    }
}
