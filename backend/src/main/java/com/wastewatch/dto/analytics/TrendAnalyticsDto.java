package com.wastewatch.dto.analytics;

import java.util.List;

public class TrendAnalyticsDto {
    private String period; // daily, weekly, monthly
    private int daysRequested;
    private List<TrendPointDto> points;

    public TrendAnalyticsDto() {
    }

    public TrendAnalyticsDto(String period, int daysRequested, List<TrendPointDto> points) {
        this.period = period;
        this.daysRequested = daysRequested;
        this.points = points;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public int getDaysRequested() {
        return daysRequested;
    }

    public void setDaysRequested(int daysRequested) {
        this.daysRequested = daysRequested;
    }

    public List<TrendPointDto> getPoints() {
        return points;
    }

    public void setPoints(List<TrendPointDto> points) {
        this.points = points;
    }
}
