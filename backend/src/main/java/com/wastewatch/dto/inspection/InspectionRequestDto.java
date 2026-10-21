package com.wastewatch.dto.inspection;

import jakarta.validation.constraints.NotNull;

public class InspectionRequestDto {

    /**
     * Room ID — required for legacy room-level inspections.
     * Optional when buildingId is provided (building-level inspection).
     */
    private Long roomId;

    /**
     * Building ID — required for building-level inspections.
     * Optional when roomId is provided (room-level inspection).
     */
    private Long buildingId;

    @NotNull(message = "Wet waste separation status is required")
    private Boolean wetWasteSeparated;

    @NotNull(message = "Dry waste separation status is required")
    private Boolean dryWasteSeparated;

    @NotNull(message = "Sanitary waste separation status is required")
    private Boolean sanitaryWasteSeparated;

    @NotNull(message = "Correct bins used status is required")
    private Boolean correctBinsUsed;

    @NotNull(message = "Bins labelled status is required")
    private Boolean binsLabelled;

    @NotNull(message = "No mixed waste status is required")
    private Boolean noMixedWaste;

    private String remarks;

    private String evidenceImageUrl;

    public InspectionRequestDto() {
    }

    /**
     * Returns true if this is a building-level inspection request.
     */
    public boolean isBuildingLevelInspection() {
        return buildingId != null && roomId == null;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public Long getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(Long buildingId) {
        this.buildingId = buildingId;
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
}
