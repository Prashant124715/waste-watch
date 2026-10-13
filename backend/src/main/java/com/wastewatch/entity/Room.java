package com.wastewatch.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "ROOMS",
    uniqueConstraints = {
        @UniqueConstraint(name = "UK_ROOMS_BUILDING_ROOM_NUM", columnNames = {"building_id", "room_number"})
    }
)
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "building_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Building building;

    @Column(name = "floor_number", nullable = false)
    private Integer floorNumber;

    @Column(name = "room_number", nullable = false, length = 30)
    private String roomNumber;

    @Column(name = "qr_code_token", nullable = false, unique = true, length = 64)
    private String qrCodeToken;

    @Column(name = "occupant_name", length = 100)
    private String occupantName;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "ACTIVE";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "persistent_non_compliance", nullable = false)
    private Boolean persistentNonCompliance = false;

    public Room() {
    }

    public Room(Building building, Integer floorNumber, String roomNumber, String qrCodeToken, String occupantName, String contactPhone) {
        this.building = building;
        this.floorNumber = floorNumber;
        this.roomNumber = roomNumber;
        this.qrCodeToken = qrCodeToken;
        this.occupantName = occupantName;
        this.contactPhone = contactPhone;
        this.status = "ACTIVE";
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = "ACTIVE";
        }
        if (this.qrCodeToken == null || this.qrCodeToken.isBlank()) {
            this.qrCodeToken = "QR-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Building getBuilding() {
        return building;
    }

    public void setBuilding(Building building) {
        this.building = building;
    }

    public Integer getFloorNumber() {
        return floorNumber;
    }

    public void setFloorNumber(Integer floorNumber) {
        this.floorNumber = floorNumber;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getQrCodeToken() {
        return qrCodeToken;
    }

    public void setQrCodeToken(String qrCodeToken) {
        this.qrCodeToken = qrCodeToken;
    }

    public String getOccupantName() {
        return occupantName;
    }

    public void setOccupantName(String occupantName) {
        this.occupantName = occupantName;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public Boolean getPersistentNonCompliance() {
        return persistentNonCompliance;
    }

    public void setPersistentNonCompliance(Boolean persistentNonCompliance) {
        this.persistentNonCompliance = persistentNonCompliance != null ? persistentNonCompliance : false;
    }
}
