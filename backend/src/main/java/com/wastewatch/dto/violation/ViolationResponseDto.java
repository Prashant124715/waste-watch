package com.wastewatch.dto.violation;

import com.wastewatch.entity.Violation;
import com.wastewatch.entity.enums.ViolationSeverity;
import com.wastewatch.entity.enums.ViolationStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ViolationResponseDto {

    private Long id;
    private Long inspectionId;
    private Integer inspectionScore;
    private Long roomId;
    private String roomNumber;
    private Integer floorNumber;
    private Long buildingId;
    private String buildingName;
    private String areaName;
    private ViolationSeverity severity;
    private ViolationStatus status;
    private String description;
    private BigDecimal fineAmount;
    private String resolutionNotes;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    private Boolean roomPersistentNonCompliance;

    public ViolationResponseDto() {
    }

    public static ViolationResponseDto fromEntity(Violation violation) {
        ViolationResponseDto dto = new ViolationResponseDto();
        dto.setId(violation.getId());

        if (violation.getInspection() != null) {
            dto.setInspectionId(violation.getInspection().getId());
            dto.setInspectionScore(violation.getInspection().getTotalScore());
        }

        if (violation.getRoom() != null) {
            dto.setRoomId(violation.getRoom().getId());
            dto.setRoomNumber(violation.getRoom().getRoomNumber());
            dto.setFloorNumber(violation.getRoom().getFloorNumber());
            dto.setRoomPersistentNonCompliance(Boolean.TRUE.equals(violation.getRoom().getPersistentNonCompliance()));

            if (violation.getRoom().getBuilding() != null) {
                dto.setBuildingId(violation.getRoom().getBuilding().getId());
                dto.setBuildingName(violation.getRoom().getBuilding().getName());

                if (violation.getRoom().getBuilding().getArea() != null) {
                    dto.setAreaName(violation.getRoom().getBuilding().getArea().getName());
                }
            }
        }

        dto.setSeverity(violation.getSeverity());
        dto.setStatus(violation.getStatus());
        dto.setDescription(violation.getDescription());
        dto.setFineAmount(violation.getFineAmount());
        dto.setResolutionNotes(violation.getResolutionNotes());
        dto.setCreatedAt(violation.getCreatedAt());
        dto.setResolvedAt(violation.getResolvedAt());

        return dto;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getInspectionId() {
        return inspectionId;
    }

    public void setInspectionId(Long inspectionId) {
        this.inspectionId = inspectionId;
    }

    public Integer getInspectionScore() {
        return inspectionScore;
    }

    public void setInspectionScore(Integer inspectionScore) {
        this.inspectionScore = inspectionScore;
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

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public ViolationSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(ViolationSeverity severity) {
        this.severity = severity;
    }

    public ViolationStatus getStatus() {
        return status;
    }

    public void setStatus(ViolationStatus status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getFineAmount() {
        return fineAmount;
    }

    public void setFineAmount(BigDecimal fineAmount) {
        this.fineAmount = fineAmount;
    }

    public String getResolutionNotes() {
        return resolutionNotes;
    }

    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public Boolean getRoomPersistentNonCompliance() {
        return roomPersistentNonCompliance;
    }

    public void setRoomPersistentNonCompliance(Boolean roomPersistentNonCompliance) {
        this.roomPersistentNonCompliance = roomPersistentNonCompliance;
    }
}
