package com.wastewatch.dto.room;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.wastewatch.entity.enums.ComplianceClassification;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoomDetailDto {

    private Long id;
    private Long buildingId;
    private String buildingName;
    private String buildingCode;
    private Long areaId;
    private String areaName;
    private String areaCode;
    private Integer floorNumber;
    private String roomNumber;
    private String qrCodeToken;
    private String qrCodeDataUrl;
    private String occupantName;
    private String contactPhone;
    private String status;
    private LocalDateTime lastInspectedAt;
    private Integer lastScore;
    private ComplianceClassification lastClassification;
    private long totalInspections;

    // Nested summary models matching the Prompt 12 API response format
    private BuildingSummary building;
    private AreaSummary area;

    public static class BuildingSummary {
        private Long id;
        private String name;
        private String buildingCode;

        public BuildingSummary() {
        }

        public BuildingSummary(Long id, String name, String buildingCode) {
            this.id = id;
            this.name = name;
            this.buildingCode = buildingCode;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getBuildingCode() {
            return buildingCode;
        }

        public void setBuildingCode(String buildingCode) {
            this.buildingCode = buildingCode;
        }
    }

    public static class AreaSummary {
        private Long id;
        private String name;
        private String areaCode;

        public AreaSummary() {
        }

        public AreaSummary(Long id, String name, String areaCode) {
            this.id = id;
            this.name = name;
            this.areaCode = areaCode;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAreaCode() {
            return areaCode;
        }

        public void setAreaCode(String areaCode) {
            this.areaCode = areaCode;
        }
    }

    public RoomDetailDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // Alias for Prompt 12: "roomId": 42
    public Long getRoomId() {
        return id;
    }

    public void setRoomId(Long roomId) {
        this.id = roomId;
    }

    public Long getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(Long buildingId) {
        this.buildingId = buildingId;
        updateBuildingSummary();
    }

    public String getBuildingName() {
        return buildingName;
    }

    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
        updateBuildingSummary();
    }

    public String getBuildingCode() {
        return buildingCode;
    }

    public void setBuildingCode(String buildingCode) {
        this.buildingCode = buildingCode;
        updateBuildingSummary();
    }

    public Long getAreaId() {
        return areaId;
    }

    public void setAreaId(Long areaId) {
        this.areaId = areaId;
        updateAreaSummary();
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
        updateAreaSummary();
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
        updateAreaSummary();
    }

    public Integer getFloorNumber() {
        return floorNumber;
    }

    public void setFloorNumber(Integer floorNumber) {
        this.floorNumber = floorNumber;
    }

    // Alias for Prompt 12: "floor": 2
    public Integer getFloor() {
        return floorNumber;
    }

    public void setFloor(Integer floor) {
        this.floorNumber = floor;
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

    public String getQrCodeDataUrl() {
        return qrCodeDataUrl;
    }

    public void setQrCodeDataUrl(String qrCodeDataUrl) {
        this.qrCodeDataUrl = qrCodeDataUrl;
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

    public LocalDateTime getLastInspectedAt() {
        return lastInspectedAt;
    }

    public void setLastInspectedAt(LocalDateTime lastInspectedAt) {
        this.lastInspectedAt = lastInspectedAt;
    }

    public Integer getLastScore() {
        return lastScore;
    }

    public void setLastScore(Integer lastScore) {
        this.lastScore = lastScore;
    }

    public ComplianceClassification getLastClassification() {
        return lastClassification;
    }

    public void setLastClassification(ComplianceClassification lastClassification) {
        this.lastClassification = lastClassification;
    }

    public long getTotalInspections() {
        return totalInspections;
    }

    public void setTotalInspections(long totalInspections) {
        this.totalInspections = totalInspections;
    }

    public BuildingSummary getBuilding() {
        if (building == null && (buildingId != null || buildingName != null || buildingCode != null)) {
            building = new BuildingSummary(buildingId, buildingName, buildingCode);
        }
        return building;
    }

    public void setBuilding(BuildingSummary building) {
        this.building = building;
        if (building != null) {
            this.buildingId = building.getId();
            this.buildingName = building.getName();
            this.buildingCode = building.getBuildingCode();
        }
    }

    public AreaSummary getArea() {
        if (area == null && (areaId != null || areaName != null || areaCode != null)) {
            area = new AreaSummary(areaId, areaName, areaCode);
        }
        return area;
    }

    public void setArea(AreaSummary area) {
        this.area = area;
        if (area != null) {
            this.areaId = area.getId();
            this.areaName = area.getName();
            this.areaCode = area.getAreaCode();
        }
    }

    private void updateBuildingSummary() {
        if (buildingId != null || buildingName != null || buildingCode != null) {
            this.building = new BuildingSummary(buildingId, buildingName, buildingCode);
        }
    }

    private void updateAreaSummary() {
        if (areaId != null || areaName != null || areaCode != null) {
            this.area = new AreaSummary(areaId, areaName, areaCode);
        }
    }
}
