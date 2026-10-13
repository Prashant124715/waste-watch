package com.wastewatch.service;

import com.wastewatch.dto.analytics.*;

import java.util.List;

public interface AnalyticsService {

    /**
     * Retrieves high-level executive compliance summary, system health score,
     * and inspection coverage metrics.
     */
    OverviewAnalyticsDto getOverviewAnalytics();

    /**
     * Retrieves area-by-area breakdown including Area Health Scores,
     * segregation compliance, coverage percentage, and confidence ratings.
     */
    List<AreaAnalyticsDto> getAreaAnalytics();

    /**
     * Retrieves building-level compliance, inspection coverage,
     * average scores, and open violations.
     */
    List<BuildingAnalyticsDto> getBuildingAnalytics();

    /**
     * Retrieves time-series trend analysis across daily, weekly, or monthly aggregations.
     *
     * @param period "daily", "weekly", or "monthly"
     * @param days   number of days to look back (e.g. 7, 14, 30, 90)
     */
    TrendAnalyticsDto getTrendAnalytics(String period, Integer days);

    /**
     * Compares recent period compliance vs previous period to identify
     * most improved and declining areas.
     *
     * @param periodDays window size in days (default: 14)
     */
    List<AreaImprovementDto> getAreaImprovements(Integer periodDays);

    /**
     * Identifies chronic persistent problems and non-compliance hotspots
     * across rooms, buildings, and areas.
     */
    HotspotAnalyticsDto getHotspots();

    /**
     * Retrieves volunteer activity, productivity, average score distribution,
     * and active engagement metrics.
     */
    List<VolunteerAnalyticsDto> getVolunteerAnalytics();
}
