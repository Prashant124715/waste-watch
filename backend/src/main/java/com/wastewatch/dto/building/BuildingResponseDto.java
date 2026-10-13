package com.wastewatch.dto.building;

import com.wastewatch.entity.Building;
import com.wastewatch.entity.enums.BuildingType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BuildingResponseDto {

    private Long id;
    private Long areaId;
    private String areaName;
    private String buildingCode;
    private String name;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private BuildingType buildingType;
    private Integer totalFloors;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public BuildingResponseDto() {
    }

    public static BuildingResponseDto fromEntity(Building building) {
        BuildingResponseDto dto = new BuildingResponseDto();
        dto.setId(building.getId());
        if (building.getArea() != null) {
            dto.setAreaId(building.getArea().getId());
            dto.setAreaName(building.getArea().getName());
        }
        dto.setBuildingCode(building.getBuildingCode());
        dto.setName(building.getName());
        dto.setAddress(building.getAddress());
        dto.setLatitude(building.getLatitude());
        dto.setLongitude(building.getLongitude());
        dto.setBuildingType(building.getBuildingType());
        dto.setTotalFloors(building.getTotalFloors());
        dto.setStatus(building.getStatus());
        dto.setCreatedAt(building.getCreatedAt());
        dto.setUpdatedAt(building.getUpdatedAt());
        return dto;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getBuildingCode() {
        return buildingCode;
    }

    public void setBuildingCode(String buildingCode) {
        this.buildingCode = buildingCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public BuildingType getBuildingType() {
        return buildingType;
    }

    public void setBuildingType(BuildingType buildingType) {
        this.buildingType = buildingType;
    }

    public Integer getTotalFloors() {
        return totalFloors;
    }

    public void setTotalFloors(Integer totalFloors) {
        this.totalFloors = totalFloors;
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
