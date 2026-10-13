package com.wastewatch.dto.inspection;

import com.wastewatch.entity.Inspection;
import com.wastewatch.entity.enums.ComplianceClassification;
import com.wastewatch.entity.enums.InspectionStatus;

import java.time.LocalDateTime;

public class InspectionResponseDto {

    private Long id;
    private Long roomId;
    private String roomNumber;
    private Integer floorNumber;
    private Long buildingId;
    private String buildingName;
    private Long areaId;
    private String areaName;
    private Long volunteerId;
    private String volunteerName;
    private LocalDateTime inspectionDate;
    private Boolean wetWasteSeparated;
    private Boolean dryWasteSeparated;
    private Boolean sanitaryWasteSeparated;
    private Boolean correctBinsUsed;
    private Boolean binsLabelled;
    private Boolean noMixedWaste;
    private Integer totalScore;
    private ComplianceClassification classification;
    private InspectionStatus status;
    private String remarks;
    private String evidenceImageUrl;
    private Boolean violationCreated;
    private Long violationId;
    private LocalDateTime createdAt;

    public InspectionResponseDto() {
    }

    public static InspectionResponseDto fromEntity(Inspection inspection) {
        return fromEntity(inspection, false, null);
    }

    public static InspectionResponseDto fromEntity(Inspection inspection, Boolean violationCreated, Long violationId) {
        InspectionResponseDto dto = new InspectionResponseDto();
        dto.setId(inspection.getId());

        if (inspection.getRoom() != null) {
            dto.setRoomId(inspection.getRoom().getId());
            dto.setRoomNumber(inspection.getRoom().getRoomNumber());
            dto.setFloorNumber(inspection.getRoom().getFloorNumber());

            if (inspection.getRoom().getBuilding() != null) {
                dto.setBuildingId(inspection.getRoom().getBuilding().getId());
                dto.setBuildingName(inspection.getRoom().getBuilding().getName());

                if (inspection.getRoom().getBuilding().getArea() != null) {
                    dto.setAreaId(inspection.getRoom().getBuilding().getArea().getId());
                    dto.setAreaName(inspection.getRoom().getBuilding().getArea().getName());
                }
            }
        }

        if (inspection.getVolunteer() != null) {
            dto.setVolunteerId(inspection.getVolunteer().getId());
            dto.setVolunteerName(inspection.getVolunteer().getFullName());
        }

        dto.setInspectionDate(inspection.getInspectionDate());
        dto.setWetWasteSeparated(inspection.getWetWasteSeparated());
        dto.setDryWasteSeparated(inspection.getDryWasteSeparated());
        dto.setSanitaryWasteSeparated(inspection.getSanitaryWasteSeparated());
        dto.setCorrectBinsUsed(inspection.getCorrectBinsUsed());
        dto.setBinsLabelled(inspection.getBinsLabelled());
        dto.setNoMixedWaste(inspection.getNoMixedWaste());
        dto.setTotalScore(inspection.getTotalScore());
        dto.setClassification(inspection.getClassification());
        dto.setStatus(inspection.getStatus());
        dto.setRemarks(inspection.getRemarks());
        dto.setEvidenceImageUrl(inspection.getEvidenceImageUrl());
        dto.setViolationCreated(violationCreated);
        dto.setViolationId(violationId);
        dto.setCreatedAt(inspection.getCreatedAt());

        int wet = Boolean.TRUE.equals(inspection.getWetWasteSeparated()) ? 25 : 0;
        int dry = Boolean.TRUE.equals(inspection.getDryWasteSeparated()) ? 25 : 0;
        int sanitary = Boolean.TRUE.equals(inspection.getSanitaryWasteSeparated()) ? 20 : 0;
        int correctBins = Boolean.TRUE.equals(inspection.getCorrectBinsUsed()) ? 15 : 0;
        int labelledBins = Boolean.TRUE.equals(inspection.getBinsLabelled()) ? 10 : 0;
        boolean hasSegregation = Boolean.TRUE.equals(inspection.getWetWasteSeparated()) || Boolean.TRUE.equals(inspection.getDryWasteSeparated()) || Boolean.TRUE.equals(inspection.getSanitaryWasteSeparated());
        int noMixed = Boolean.TRUE.equals(inspection.getNoMixedWaste()) && hasSegregation ? 5 : 0;

        dto.setBreakdown(new ScoreBreakdownDto(wet, dry, sanitary, correctBins, labelledBins, noMixed));

        return dto;
    }

    private ScoreBreakdownDto breakdown;

    public ScoreBreakdownDto getBreakdown() {
        return breakdown;
    }

    public void setBreakdown(ScoreBreakdownDto breakdown) {
        this.breakdown = breakdown;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public Integer getFloorNumber() {
        return floorNumber;
    }

    public void setFloorNumber(Integer floorNumber) {
        this.floorNumber = floorNumber;
    }

    public Long getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(Long buildingId) {
        this.buildingId = buildingId;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
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

    public Long getVolunteerId() {
        return volunteerId;
    }

    public void setVolunteerId(Long volunteerId) {
        this.volunteerId = volunteerId;
    }

    public String getVolunteerName() {
        return volunteerName;
    }

    public void setVolunteerName(String volunteerName) {
        this.volunteerName = volunteerName;
    }

    public LocalDateTime getInspectionDate() {
        return inspectionDate;
    }

    public void setInspectionDate(LocalDateTime inspectionDate) {
        this.inspectionDate = inspectionDate;
    }

    public Boolean getWetWasteSeparated() {
        return wetWasteSeparated;
    }

    public void setWetWasteSeparated(Boolean wetWasteSeparated) {
        this.wetWasteSeparated = wetWasteSeparated;
    }

    public Boolean getDryWasteSeparated() {
        return dryWasteSeparated;
    }

    public void setDryWasteSeparated(Boolean dryWasteSeparated) {
        this.dryWasteSeparated = dryWasteSeparated;
    }

    public Boolean getSanitaryWasteSeparated() {
        return sanitaryWasteSeparated;
    }

    public void setSanitaryWasteSeparated(Boolean sanitaryWasteSeparated) {
        this.sanitaryWasteSeparated = sanitaryWasteSeparated;
    }

    public Boolean getCorrectBinsUsed() {
        return correctBinsUsed;
    }

    public void setCorrectBinsUsed(Boolean correctBinsUsed) {
        this.correctBinsUsed = correctBinsUsed;
    }

    public Boolean getBinsLabelled() {
        return binsLabelled;
    }

    public void setBinsLabelled(Boolean binsLabelled) {
        this.binsLabelled = binsLabelled;
    }

    public Boolean getNoMixedWaste() {
        return noMixedWaste;
    }

    public void setNoMixedWaste(Boolean noMixedWaste) {
        this.noMixedWaste = noMixedWaste;
    }

    public Integer getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Integer totalScore) {
        this.totalScore = totalScore;
    }

    public ComplianceClassification getClassification() {
        return classification;
    }

    public void setClassification(ComplianceClassification classification) {
        this.classification = classification;
    }

    public InspectionStatus getStatus() {
        return status;
    }

    public void setStatus(InspectionStatus status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getEvidenceImageUrl() {
        return evidenceImageUrl;
    }

    public void setEvidenceImageUrl(String evidenceImageUrl) {
        this.evidenceImageUrl = evidenceImageUrl;
    }

    public Boolean getViolationCreated() {
        return violationCreated;
    }

    public void setViolationCreated(Boolean violationCreated) {
        this.violationCreated = violationCreated;
    }

    public Long getViolationId() {
        return violationId;
    }

    public void setViolationId(Long violationId) {
        this.violationId = violationId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
