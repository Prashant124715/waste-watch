package com.wastewatch.dto.analytics;

import java.util.List;

public class HotspotAnalyticsDto {
    private List<RepeatOffenderRoomDto> repeatOffenderRooms;
    private List<ProblemBuildingDto> problemBuildings;
    private List<AreaAnalyticsDto> criticalAreas;

    public HotspotAnalyticsDto() {
    }

    public HotspotAnalyticsDto(List<RepeatOffenderRoomDto> repeatOffenderRooms,
                               List<ProblemBuildingDto> problemBuildings,
                               List<AreaAnalyticsDto> criticalAreas) {
        this.repeatOffenderRooms = repeatOffenderRooms;
        this.problemBuildings = problemBuildings;
        this.criticalAreas = criticalAreas;
    }

    public List<RepeatOffenderRoomDto> getRepeatOffenderRooms() {
        return repeatOffenderRooms;
    }

    public void setRepeatOffenderRooms(List<RepeatOffenderRoomDto> repeatOffenderRooms) {
        this.repeatOffenderRooms = repeatOffenderRooms;
    }

    public List<ProblemBuildingDto> getProblemBuildings() {
        return problemBuildings;
    }

    public void setProblemBuildings(List<ProblemBuildingDto> problemBuildings) {
        this.problemBuildings = problemBuildings;
    }

    public List<AreaAnalyticsDto> getCriticalAreas() {
        return criticalAreas;
    }

    public void setCriticalAreas(List<AreaAnalyticsDto> criticalAreas) {
        this.criticalAreas = criticalAreas;
    }
}
