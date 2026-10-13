package com.wastewatch.dto.analytics;

import java.time.LocalDateTime;

public class VolunteerAnalyticsDto {
    private Long volunteerId;
    private String fullName;
    private String username;
    private String email;
    private long inspectionsCount;
    private double averageScoreAwarded;
    private LocalDateTime lastInspectionDate;
    private long distinctRoomsInspected;
    private long distinctBuildingsCovered;
    private long violationsFlagged;
    private String activityStatus; // ACTIVE, RECENT, INACTIVE
    private long activeDaysCount;

    public VolunteerAnalyticsDto() {
    }

    public Long getVolunteerId() {
        return volunteerId;
    }

    public void setVolunteerId(Long volunteerId) {
        this.volunteerId = volunteerId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getInspectionsCount() {
        return inspectionsCount;
    }

    public void setInspectionsCount(long inspectionsCount) {
        this.inspectionsCount = inspectionsCount;
    }

    public double getAverageScoreAwarded() {
        return averageScoreAwarded;
    }

    public void setAverageScoreAwarded(double averageScoreAwarded) {
        this.averageScoreAwarded = averageScoreAwarded;
    }

    public LocalDateTime getLastInspectionDate() {
        return lastInspectionDate;
    }

    public void setLastInspectionDate(LocalDateTime lastInspectionDate) {
        this.lastInspectionDate = lastInspectionDate;
    }

    public long getDistinctRoomsInspected() {
        return distinctRoomsInspected;
    }

    public void setDistinctRoomsInspected(long distinctRoomsInspected) {
        this.distinctRoomsInspected = distinctRoomsInspected;
    }

    public long getDistinctBuildingsCovered() {
        return distinctBuildingsCovered;
    }

    public void setDistinctBuildingsCovered(long distinctBuildingsCovered) {
        this.distinctBuildingsCovered = distinctBuildingsCovered;
    }

    public long getViolationsFlagged() {
        return violationsFlagged;
    }

    public void setViolationsFlagged(long violationsFlagged) {
        this.violationsFlagged = violationsFlagged;
    }

    public String getActivityStatus() {
        return activityStatus;
    }

    public void setActivityStatus(String activityStatus) {
        this.activityStatus = activityStatus;
    }

    public long getActiveDaysCount() {
        return activeDaysCount;
    }

    public void setActiveDaysCount(long activeDaysCount) {
        this.activeDaysCount = activeDaysCount;
    }
}
