package com.wastewatch.dto.volunteer;

import java.time.LocalDateTime;

public class VolunteerSummaryDto {

    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String status;
    private String city;
    private String state;
    private String address;
    private String reason;
    private LocalDateTime createdAt;
    private long inspectionsCount;
    private double averageScoreGiven;
    private LocalDateTime lastInspectionDate;
    private long distinctRoomsInspected;
    private long distinctBuildingsInspected;
    private long violationsFlagged;

    public VolunteerSummaryDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getInspectionsCount() {
        return inspectionsCount;
    }

    public void setInspectionsCount(long inspectionsCount) {
        this.inspectionsCount = inspectionsCount;
    }

    public double getAverageScoreGiven() {
        return averageScoreGiven;
    }

    public void setAverageScoreGiven(double averageScoreGiven) {
        this.averageScoreGiven = averageScoreGiven;
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

    public long getDistinctBuildingsInspected() {
        return distinctBuildingsInspected;
    }

    public void setDistinctBuildingsInspected(long distinctBuildingsInspected) {
        this.distinctBuildingsInspected = distinctBuildingsInspected;
    }

    public long getViolationsFlagged() {
        return violationsFlagged;
    }

    public void setViolationsFlagged(long violationsFlagged) {
        this.violationsFlagged = violationsFlagged;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
