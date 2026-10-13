package com.wastewatch.dto.report;

import java.time.LocalDateTime;

public class VolunteerActivityReportDto {

    private Long volunteerId;
    private String username;
    private String fullName;
    private String email;
    private String status;
    private long inspectionsInPeriod;
    private double averageScore;
    private long uniqueRoomsAudited;
    private long uniqueBuildingsAudited;
    private long violationsFlagged;
    private LocalDateTime firstInspectionDate;
    private LocalDateTime lastInspectionDate;

    public VolunteerActivityReportDto() {
    }

    // Getters and Setters
    public Long getVolunteerId() { return volunteerId; }
    public void setVolunteerId(Long volunteerId) { this.volunteerId = volunteerId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getInspectionsInPeriod() { return inspectionsInPeriod; }
    public void setInspectionsInPeriod(long inspectionsInPeriod) { this.inspectionsInPeriod = inspectionsInPeriod; }

    public double getAverageScore() { return averageScore; }
    public void setAverageScore(double averageScore) { this.averageScore = averageScore; }

    public long getUniqueRoomsAudited() { return uniqueRoomsAudited; }
    public void setUniqueRoomsAudited(long uniqueRoomsAudited) { this.uniqueRoomsAudited = uniqueRoomsAudited; }

    public long getUniqueBuildingsAudited() { return uniqueBuildingsAudited; }
    public void setUniqueBuildingsAudited(long uniqueBuildingsAudited) { this.uniqueBuildingsAudited = uniqueBuildingsAudited; }

    public long getViolationsFlagged() { return violationsFlagged; }
    public void setViolationsFlagged(long violationsFlagged) { this.violationsFlagged = violationsFlagged; }

    public LocalDateTime getFirstInspectionDate() { return firstInspectionDate; }
    public void setFirstInspectionDate(LocalDateTime firstInspectionDate) { this.firstInspectionDate = firstInspectionDate; }

    public LocalDateTime getLastInspectionDate() { return lastInspectionDate; }
    public void setLastInspectionDate(LocalDateTime lastInspectionDate) { this.lastInspectionDate = lastInspectionDate; }
}
