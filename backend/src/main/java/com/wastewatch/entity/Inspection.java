package com.wastewatch.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wastewatch.entity.enums.ComplianceClassification;
import com.wastewatch.entity.enums.InspectionStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "INSPECTIONS")
public class Inspection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "room_id", nullable = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Room room;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "building_id", nullable = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Building building;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "volunteer_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "passwordHash"})
    private AppUser volunteer;

    @Column(name = "inspection_date", nullable = false)
    private LocalDateTime inspectionDate;

    @Column(name = "wet_waste_separated", nullable = false)
    private Boolean wetWasteSeparated = false;

    @Column(name = "dry_waste_separated", nullable = false)
    private Boolean dryWasteSeparated = false;

    @Column(name = "sanitary_waste_separated", nullable = false)
    private Boolean sanitaryWasteSeparated = false;

    @Column(name = "correct_bins_used", nullable = false)
    private Boolean correctBinsUsed = false;

    @Column(name = "bins_labelled", nullable = false)
    private Boolean binsLabelled = false;

    @Column(name = "no_mixed_waste", nullable = false)
    private Boolean noMixedWaste = false;

    @Column(name = "total_score", nullable = false)
    private Integer totalScore = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "classification", nullable = false, length = 20)
    private ComplianceClassification classification = ComplianceClassification.POOR;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private InspectionStatus status = InspectionStatus.COMPLETED;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "evidence_image_url", length = 255)
    private String evidenceImageUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Inspection() {
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
        if (this.inspectionDate == null) {
            this.inspectionDate = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = InspectionStatus.COMPLETED;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Returns the Building associated with this inspection.
     * For building-level inspections, this returns the direct building reference.
     * For legacy room-level inspections, this navigates through room → building.
     */
    public Building getEffectiveBuilding() {
        if (this.building != null) return this.building;
        if (this.room != null) return this.room.getBuilding();
        return null;
    }

    /**
     * Returns true if this is a building-level inspection (no room reference).
     */
    public boolean isBuildingLevel() {
        return this.room == null && this.building != null;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public Building getBuilding() {
        return building;
    }

    public void setBuilding(Building building) {
        this.building = building;
    }

    public AppUser getVolunteer() {
        return volunteer;
    }

    public void setVolunteer(AppUser volunteer) {
        this.volunteer = volunteer;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
