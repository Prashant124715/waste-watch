package com.wastewatch.service;

import com.wastewatch.dto.map.BuildingMapMarkerDto;
import com.wastewatch.entity.Area;
import com.wastewatch.entity.Building;
import com.wastewatch.entity.Inspection;
import com.wastewatch.entity.Room;
import com.wastewatch.entity.enums.BuildingType;
import com.wastewatch.entity.enums.ComplianceClassification;
import com.wastewatch.repository.BuildingRepository;
import com.wastewatch.repository.InspectionRepository;
import com.wastewatch.repository.RoomRepository;
import com.wastewatch.repository.ViolationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MapServiceTest {

    @Mock
    private BuildingRepository buildingRepository;
    @Mock
    private InspectionRepository inspectionRepository;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private ViolationRepository violationRepository;

    @InjectMocks
    private MapService mapService;

    private Area area;
    private Building buildingWithCoords;
    private Building buildingWithoutCoords;

    @BeforeEach
    void setUp() {
        area = new Area();
        area.setId(1L);
        area.setName("Downtown Zone");
        area.setAreaCode("ZONE-DT");

        buildingWithCoords = new Building();
        buildingWithCoords.setId(101L);
        buildingWithCoords.setName("Horizon Heights");
        buildingWithCoords.setBuildingCode("HH-01");
        buildingWithCoords.setBuildingType(BuildingType.RESIDENTIAL);
        buildingWithCoords.setArea(area);
        buildingWithCoords.setLatitude(new BigDecimal("19.0760"));
        buildingWithCoords.setLongitude(new BigDecimal("72.8777"));
        buildingWithCoords.setTotalFloors(10);

        buildingWithoutCoords = new Building();
        buildingWithoutCoords.setId(102L);
        buildingWithoutCoords.setName("Unmapped Society");
        buildingWithoutCoords.setBuildingCode("US-02");
        buildingWithoutCoords.setBuildingType(BuildingType.COMMERCIAL);
        buildingWithoutCoords.setArea(area);
        buildingWithoutCoords.setLatitude(null);
        buildingWithoutCoords.setLongitude(null);
        buildingWithoutCoords.setTotalFloors(4);
    }

    @Test
    @DisplayName("Valid Coordinates: Correctly flags hasValidCoordinates=true with compliance score & coverage")
    void testBuildingWithValidCoordinates() {
        when(buildingRepository.findAll()).thenReturn(List.of(buildingWithCoords));
        when(roomRepository.countByBuildingId(101L)).thenReturn(10L);
        when(inspectionRepository.countDistinctInspectedRoomsByBuildingId(101L)).thenReturn(8L);
        when(inspectionRepository.countByBuildingId(101L)).thenReturn(12L);
        when(inspectionRepository.getAverageScoreByBuildingId(101L)).thenReturn(85.0);

        Inspection latest = new Inspection();
        latest.setInspectionDate(LocalDateTime.now().minusHours(3));
        when(inspectionRepository.findFirstByRoomBuildingIdOrderByInspectionDateDesc(101L)).thenReturn(Optional.of(latest));
        when(roomRepository.findByBuildingId(101L)).thenReturn(Collections.emptyList());
        when(violationRepository.countByBuildingIdAndStatusNot(anyLong(), any())).thenReturn(0L);

        List<BuildingMapMarkerDto> markers = mapService.getBuildingMapMarkers();

        assertEquals(1, markers.size());
        BuildingMapMarkerDto marker = markers.get(0);

        assertEquals(101L, marker.getId());
        assertEquals("Horizon Heights", marker.getName());
        assertTrue(marker.isHasValidCoordinates());
        assertEquals(new BigDecimal("19.0760"), marker.getLatitude());
        assertEquals(new BigDecimal("72.8777"), marker.getLongitude());
        assertEquals(85.0, marker.getComplianceScore());
        assertEquals("EXCELLENT", marker.getComplianceStatus());
        assertEquals(80.0, marker.getInspectionCoverage()); // 8/10 * 100
        assertEquals(8, marker.getInspectedRooms());
        assertEquals(10, marker.getTotalRooms());
        assertNotNull(marker.getLastInspectionDate());
    }

    @Test
    @DisplayName("Missing Coordinates: Gracefully flags hasValidCoordinates=false without throwing exceptions")
    void testBuildingWithMissingCoordinates() {
        when(buildingRepository.findAll()).thenReturn(List.of(buildingWithoutCoords));
        when(roomRepository.countByBuildingId(102L)).thenReturn(5L);
        when(inspectionRepository.countDistinctInspectedRoomsByBuildingId(102L)).thenReturn(0L);
        when(inspectionRepository.countByBuildingId(102L)).thenReturn(0L);
        when(inspectionRepository.getAverageScoreByBuildingId(102L)).thenReturn(null);
        when(inspectionRepository.findFirstByRoomBuildingIdOrderByInspectionDateDesc(102L)).thenReturn(Optional.empty());
        when(roomRepository.findByBuildingId(102L)).thenReturn(Collections.emptyList());
        when(violationRepository.countByBuildingIdAndStatusNot(anyLong(), any())).thenReturn(0L);

        List<BuildingMapMarkerDto> markers = mapService.getBuildingMapMarkers();

        assertEquals(1, markers.size());
        BuildingMapMarkerDto marker = markers.get(0);

        assertEquals(102L, marker.getId());
        assertFalse(marker.isHasValidCoordinates());
        assertNull(marker.getLatitude());
        assertNull(marker.getLongitude());
        assertEquals("NO_DATA", marker.getComplianceStatus());
        assertNull(marker.getComplianceScore());
        assertEquals(0.0, marker.getInspectionCoverage());
    }

    @Test
    @DisplayName("Multiple Buildings: Processes mixed list with various health statuses and open violations")
    void testMultipleBuildingsMixedList() {
        when(buildingRepository.findAll()).thenReturn(List.of(buildingWithCoords, buildingWithoutCoords));
        when(roomRepository.countByBuildingId(anyLong())).thenReturn(4L);
        when(inspectionRepository.countDistinctInspectedRoomsByBuildingId(anyLong())).thenReturn(2L);
        when(inspectionRepository.countByBuildingId(101L)).thenReturn(5L);
        when(inspectionRepository.countByBuildingId(102L)).thenReturn(0L);
        when(inspectionRepository.getAverageScoreByBuildingId(101L)).thenReturn(35.0); // POOR
        when(inspectionRepository.getAverageScoreByBuildingId(102L)).thenReturn(null);
        when(inspectionRepository.findFirstByRoomBuildingIdOrderByInspectionDateDesc(anyLong())).thenReturn(Optional.empty());
        when(violationRepository.countByBuildingIdAndStatusNot(eq(101L), any())).thenReturn(2L);
        when(violationRepository.countByBuildingIdAndStatusNot(eq(102L), any())).thenReturn(0L);

        List<BuildingMapMarkerDto> markers = mapService.getBuildingMapMarkers();

        assertEquals(2, markers.size());

        BuildingMapMarkerDto m1 = markers.get(0);
        assertEquals("POOR", m1.getComplianceStatus());
        assertEquals(35.0, m1.getComplianceScore());
        assertEquals(2, m1.getOpenViolations());

        BuildingMapMarkerDto m2 = markers.get(1);
        assertEquals("NO_DATA", m2.getComplianceStatus());
        assertFalse(m2.isHasValidCoordinates());
    }
}
