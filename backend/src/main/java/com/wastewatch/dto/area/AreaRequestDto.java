package com.wastewatch.dto.area;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AreaRequestDto {

    @NotBlank(message = "Area code is required")
    @Size(max = 20, message = "Area code must not exceed 20 characters")
    private String areaCode;

    @NotBlank(message = "Area name is required")
    @Size(max = 100, message = "Area name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @NotBlank(message = "Pincode is required")
    @Size(max = 10, message = "Pincode must not exceed 10 characters")
    private String pincode;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    @Size(max = 20, message = "Status must not exceed 20 characters")
    private String status;

    public AreaRequestDto() {
    }

    public AreaRequestDto(String areaCode, String name, String city, String pincode, String description) {
        this.areaCode = areaCode;
        this.name = name;
        this.city = city;
        this.pincode = pincode;
        this.description = description;
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
}
