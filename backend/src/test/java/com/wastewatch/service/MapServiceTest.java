package com.wastewatch.service;

import com.wastewatch.dto.map.AreaHotspotDto;
import com.wastewatch.dto.map.BuildingMapMarkerDto;
import com.wastewatch.entity.Area;
import com.wastewatch.entity.Building;
import com.wastewatch.entity.Inspection;
import com.wastewatch.entity.Room;
import com.wastewatch.entity.enums.BuildingType;
import com.wastewatch.repository.AreaRepository;
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
    @Mock
    private AreaRepository areaRepository;

    @InjectMocks
    private MapService mapService;

    private Area area;
    private Area area2;
    private Building buildingWithCoords;
    private Building buildingWithoutCoords;
    private Building buildingWithZeroCoords;

    @BeforeEach
    void setUp() {
        area = new Area();
        area.setId(1L);
        area.setName("Downtown Zone");
        area.setAreaCode("ZONE-DT");

        area2 = new Area();
        area2.setId(2L);
        area2.setName("Green Valley");
        area2.setAreaCode("ZONE-GV");

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

        buildingWithZeroCoords = new Building();
        buildingWithZeroCoords.setId(103L);
        buildingWithZeroCoords.setName("Null Island Complex");
        buildingWithZeroCoords.setBuildingCode("NIC-03");
        buildingWithZeroCoords.setBuildingType(BuildingType.COMMERCIAL);
        buildingWithZeroCoords.setArea(area2);
        buildingWithZeroCoords.setLatitude(BigDecimal.ZERO);
        buildingWithZeroCoords.setLongitude(BigDecimal.ZERO);
        buildingWithZeroCoords.setTotalFloors(2);
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
        assertEquals(101L, marker.getBuildingId());
        assertEquals("Horizon Heights", marker.getName());
        assertEquals("Horizon Heights", marker.getBuildingName());
        assertEquals("Downtown Zone", marker.getArea());
        assertEquals("Downtown Zone", marker.getAreaName());
        assertTrue(marker.isHasValidCoordinates());
        assertEquals(new BigDecimal("19.0760"), marker.getLatitude());
        assertEquals(new BigDecimal("72.8777"), marker.getLongitude());
        assertEquals(85.0, marker.getComplianceScore());
        assertEquals(85.0, marker.getAverageCompliance());
        assertEquals("EXCELLENT", marker.getComplianceStatus());
        assertEquals(80.0, marker.getInspectionCoverage()); // 8/10 * 100
        assertEquals(8, marker.getInspectedRooms());
        assertEquals(10, marker.getTotalRooms());
        assertNotNull(marker.getLastInspectionDate());
        assertNotNull(marker.getLastInspection());
    }

    @Test
    @DisplayName("Missing Coordinates: Gracefully flags hasValidCoordinates=false without throwing exceptions")
    void testBuildingWithMissingCoordinates() {
        when(buildingRepository.findAll()).thenReturn(List.of(buildingWithoutCoords, buildingWithZeroCoords));
        when(roomRepository.countByBuildingId(anyLong())).thenReturn(5L);
        when(inspectionRepository.countDistinctInspectedRoomsByBuildingId(anyLong())).thenReturn(0L);
        when(inspectionRepository.countByBuildingId(anyLong())).thenReturn(0L);
        when(inspectionRepository.getAverageScoreByBuildingId(anyLong())).thenReturn(null);
        when(inspectionRepository.findFirstByRoomBuildingIdOrderByInspectionDateDesc(anyLong())).thenReturn(Optional.empty());
        when(roomRepository.findByBuildingId(anyLong())).thenReturn(Collections.emptyList());
        when(violationRepository.countByBuildingIdAndStatusNot(anyLong(), any())).thenReturn(0L);

        List<BuildingMapMarkerDto> markers = mapService.getBuildingMapMarkers();

        assertEquals(2, markers.size());
        BuildingMapMarkerDto m1 = markers.get(0);
        assertEquals(102L, m1.getId());
        assertFalse(m1.isHasValidCoordinates());
        assertNull(m1.getLatitude());
        assertNull(m1.getLongitude());
        assertEquals("NO_DATA", m1.getComplianceStatus());

        BuildingMapMarkerDto m2 = markers.get(1);
        assertEquals(103L, m2.getId());
        assertFalse(m2.isHasValidCoordinates(), "Coordinates (0,0) must be flagged as invalid");
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

    @Test
    @DisplayName("Filter Behavior: Successfully filters by Area, ComplianceStatus, and BuildingId")
    void testFilterBehavior() {
        when(buildingRepository.findAll()).thenReturn(List.of(buildingWithCoords, buildingWithoutCoords));
        when(roomRepository.countByBuildingId(anyLong())).thenReturn(6L);
        when(inspectionRepository.countDistinctInspectedRoomsByBuildingId(anyLong())).thenReturn(3L);
        when(inspectionRepository.countByBuildingId(101L)).thenReturn(5L);
        when(inspectionRepository.countByBuildingId(102L)).thenReturn(0L);
        when(inspectionRepository.getAverageScoreByBuildingId(101L)).thenReturn(88.0); // EXCELLENT
        when(inspectionRepository.getAverageScoreByBuildingId(102L)).thenReturn(null); // NO_DATA
        when(inspectionRepository.findFirstByRoomBuildingIdOrderByInspectionDateDesc(anyLong())).thenReturn(Optional.empty());
        when(violationRepository.countByBuildingIdAndStatusNot(anyLong(), any())).thenReturn(0L);

        // Filter by building ID 101
        List<BuildingMapMarkerDto> byBuilding = mapService.getBuildingMapMarkers(null, null, 101L, null);
        assertEquals(1, byBuilding.size());
        assertEquals(101L, byBuilding.get(0).getId());

        // Filter by compliance status EXCELLENT
        List<BuildingMapMarkerDto> byStatus = mapService.getBuildingMapMarkers(null, "EXCELLENT", null, null);
        assertEquals(1, byStatus.size());
        assertEquals("EXCELLENT", byStatus.get(0).getComplianceStatus());

        // Filter by inspected status
        List<BuildingMapMarkerDto> byInspected = mapService.getBuildingMapMarkers(null, null, null, "INSPECTED");
        assertEquals(1, byInspected.size());
        assertEquals(101L, byInspected.get(0).getId());

        // Filter by uninspected status
        List<BuildingMapMarkerDto> byUninspected = mapService.getBuildingMapMarkers(null, null, null, "UNINSPECTED");
        assertEquals(1, byUninspected.size());
        assertEquals(102L, byUninspected.get(0).getId());
    }

    @Test
    @DisplayName("Hotspot Analysis: Computes area aggregates, center coordinates, and severity ratings")
    void testGetAreaHotspots() {
        when(areaRepository.findAll()).thenReturn(List.of(area));
        when(buildingRepository.findAll()).thenReturn(List.of(buildingWithCoords));
        when(roomRepository.countByBuildingId(101L)).thenReturn(10L);
        when(inspectionRepository.countDistinctInspectedRoomsByBuildingId(101L)).thenReturn(5L);
        when(inspectionRepository.countByBuildingId(101L)).thenReturn(5L);
        when(inspectionRepository.getAverageScoreByBuildingId(101L)).thenReturn(32.0); // POOR
        when(inspectionRepository.findFirstByRoomBuildingIdOrderByInspectionDateDesc(101L)).thenReturn(Optional.empty());
        when(violationRepository.countByBuildingIdAndStatusNot(anyLong(), any())).thenReturn(3L);

        List<AreaHotspotDto> hotspots = mapService.getAreaHotspots();

        assertNotNull(hotspots);
        assertEquals(1, hotspots.size());
        AreaHotspotDto h = hotspots.get(0);
        assertEquals("Downtown Zone", h.getAreaName());
        assertEquals(1, h.getTotalBuildings());
        assertEquals(1, h.getMappedBuildings());
        assertEquals(32.0, h.getAverageScore());
        assertEquals("POOR", h.getComplianceStatus());
        assertTrue(h.isHotspot());
        assertEquals("CRITICAL", h.getHotspotSeverity());
        assertEquals(3, h.getOpenViolations());
    }
}
