package com.wastewatch.dto.area;

import com.wastewatch.entity.Area;
import java.time.LocalDateTime;

public class AreaResponseDto {

    private Long id;
    private String areaCode;
    private String name;
    private String city;
    private String pincode;
    private String description;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AreaResponseDto() {
    }

    public static AreaResponseDto fromEntity(Area area) {
        AreaResponseDto dto = new AreaResponseDto();
        dto.setId(area.getId());
        dto.setAreaCode(area.getAreaCode());
        dto.setName(area.getName());
        dto.setCity(area.getCity());
        dto.setPincode(area.getPincode());
        dto.setDescription(area.getDescription());
        dto.setStatus(area.getStatus());
        dto.setCreatedAt(area.getCreatedAt());
        dto.setUpdatedAt(area.getUpdatedAt());
        return dto;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
}
