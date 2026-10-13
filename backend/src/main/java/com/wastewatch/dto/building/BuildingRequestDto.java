package com.wastewatch.dto.building;

import com.wastewatch.entity.enums.BuildingType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class BuildingRequestDto {

    @NotNull(message = "Area ID is required")
    private Long areaId;

    @NotBlank(message = "Building code is required")
    @Size(max = 30, message = "Building code must not exceed 30 characters")
    private String buildingCode;

    @NotBlank(message = "Building name is required")
    @Size(max = 120, message = "Building name must not exceed 120 characters")
    private String name;

    @NotBlank(message = "Address is required")
    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;

    @DecimalMin(value = "-90.0", message = "Latitude must be >= -90.0")
    @DecimalMax(value = "90.0", message = "Latitude must be <= 90.0")
    private BigDecimal latitude;

    @DecimalMin(value = "-180.0", message = "Longitude must be >= -180.0")
    @DecimalMax(value = "180.0", message = "Longitude must be <= 180.0")
    private BigDecimal longitude;

    private BuildingType buildingType = BuildingType.RESIDENTIAL;

    @NotNull(message = "Total floors count is required")
    @Min(value = 1, message = "Total floors must be at least 1")
    private Integer totalFloors = 1;

    @Size(max = 20, message = "Status must not exceed 20 characters")
    private String status;

    public BuildingRequestDto() {
    }

    public Long getAreaId() {
        return areaId;
    }

    public void setAreaId(Long areaId) {
        this.areaId = areaId;
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
}
