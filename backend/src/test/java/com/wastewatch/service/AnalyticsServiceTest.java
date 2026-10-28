package com.wastewatch.service;

import com.wastewatch.dto.analytics.*;
import com.wastewatch.entity.*;
import com.wastewatch.entity.enums.BuildingType;
import com.wastewatch.entity.enums.ComplianceClassification;
import com.wastewatch.entity.enums.UserRole;
import com.wastewatch.entity.enums.ViolationStatus;
import com.wastewatch.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private InspectionRepository inspectionRepository;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private BuildingRepository buildingRepository;
    @Mock
    private AreaRepository areaRepository;
    @Mock
    private ViolationRepository violationRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AnalyticsServiceImpl analyticsService;

    private Area testArea;
    private Building testBuilding;
    private Room testRoom1;
    private Room testRoom2;
    private AppUser testVolunteer;

    @BeforeEach
    void setUp() {
        testArea = new Area();
        testArea.setId(1L);
        testArea.setName("North Sector");
        testArea.setAreaCode("SEC-N");

        testBuilding = new Building();
        testBuilding.setId(10L);
        testBuilding.setName("Green Towers");
        testBuilding.setBuildingCode("GT-01");
        testBuilding.setBuildingType(BuildingType.RESIDENTIAL);
        testBuilding.setArea(testArea);
        testBuilding.setLatitude(new BigDecimal("19.0760"));
        testBuilding.setLongitude(new BigDecimal("72.8777"));

        testRoom1 = new Room();
        testRoom1.setId(101L);
        testRoom1.setRoomNumber("101");
        testRoom1.setFloorNumber(1);
        testRoom1.setBuilding(testBuilding);

        testRoom2 = new Room();
        testRoom2.setId(102L);
        testRoom2.setRoomNumber("102");
        testRoom2.setFloorNumber(1);
        testRoom2.setBuilding(testBuilding);

        testVolunteer = new AppUser();
        testVolunteer.setId(20L);
        testVolunteer.setUsername("volunteer1");
        testVolunteer.setFullName("Jane Doe");
        testVolunteer.setEmail("jane@example.com");
        testVolunteer.setRole(UserRole.VOLUNTEER);
    }

    @Test
    @DisplayName("Zero State: Clean handling of empty database with 0 inspections")
    void testZeroStateOverview() {
        when(inspectionRepository.count()).thenReturn(0L);
        when(buildingRepository.count()).thenReturn(0L);
        when(inspectionRepository.countDistinctInspectedBuildings()).thenReturn(0L);
        when(areaRepository.count()).thenReturn(0L);
        when(userRepository.findByRole(UserRole.VOLUNTEER)).thenReturn(Collections.emptyList());
        when(inspectionRepository.getOverallAverageScore()).thenReturn(null);
        when(violationRepository.countByStatusIn(anyList())).thenReturn(0L);
        when(violationRepository.countByStatus(ViolationStatus.RESOLVED)).thenReturn(0L);
        when(inspectionRepository.findAll()).thenReturn(Collections.emptyList());

        OverviewAnalyticsDto dto = analyticsService.getOverviewAnalytics();

        assertNotNull(dto);
        assertEquals(0, dto.getTotalInspections());
        assertEquals(0, dto.getTotalBuildings());
        assertEquals(0.0, dto.getOverallComplianceRate());
        assertEquals(0.0, dto.getOverallInspectionCoverage());
        assertEquals(100.0, dto.getOverallViolationResolutionRate());
        // System health = 0.70*0 + 0.20*0 + 0.10*100 = 10.0
        assertEquals(10.0, dto.getSystemHealthScore());
    }

    @Test
    @DisplayName("Perfect State: 100% coverage, 100% compliance, 100% health score")
    void testPerfectComplianceAndCoverage() {
        when(inspectionRepository.count()).thenReturn(10L);
        when(buildingRepository.count()).thenReturn(2L);
        when(inspectionRepository.countDistinctInspectedBuildings()).thenReturn(2L);
        when(areaRepository.count()).thenReturn(1L);
        when(userRepository.findByRole(UserRole.VOLUNTEER)).thenReturn(List.of(testVolunteer));
        when(inspectionRepository.getOverallAverageScore()).thenReturn(100.0);
        when(violationRepository.countByStatusIn(anyList())).thenReturn(0L);
        when(violationRepository.countByStatus(ViolationStatus.RESOLVED)).thenReturn(0L);
        when(inspectionRepository.findAll()).thenReturn(Collections.emptyList());

        OverviewAnalyticsDto dto = analyticsService.getOverviewAnalytics();

        assertNotNull(dto);
        assertEquals(100.0, dto.getOverallComplianceRate());
        assertEquals(100.0, dto.getOverallInspectionCoverage());
        assertEquals(100.0, dto.getOverallViolationResolutionRate());
        // 0.70*100 + 0.20*100 + 0.10*100 = 100.0
        assertEquals(100.0, dto.getSystemHealthScore());
    }

    @Test
    @DisplayName("Partial Coverage Confidence Rule: 94% compliance with 12% coverage gives Low Confidence & 78.2 Health Score")
    void testPartialCoverageConfidenceRule() {
        when(areaRepository.findAll()).thenReturn(List.of(testArea));
        when(buildingRepository.countByAreaId(1L)).thenReturn(50L);
        when(inspectionRepository.countDistinctInspectedBuildingsByAreaId(1L)).thenReturn(6L); // 6/50 = 12%
        when(inspectionRepository.countByAreaId(1L)).thenReturn(6L);
        when(inspectionRepository.getAverageScoreByAreaId(1L)).thenReturn(94.0); // 94% compliance
        when(violationRepository.countByAreaId(1L)).thenReturn(0L);
        when(violationRepository.countByAreaIdAndStatus(1L, ViolationStatus.RESOLVED)).thenReturn(0L);
        when(buildingRepository.findByAreaId(1L)).thenReturn(Collections.emptyList());

        List<AreaAnalyticsDto> result = analyticsService.getAreaAnalytics();

        assertEquals(1, result.size());
        AreaAnalyticsDto areaDto = result.get(0);

        assertEquals(94.0, areaDto.getAverageCompliance());
        assertEquals(12.0, areaDto.getInspectionCoverage());
        assertEquals(100.0, areaDto.getViolationResolutionRate());
        assertEquals("LOW", areaDto.getConfidenceRating()); // < 40% is LOW confidence

        // Formula: (0.70 * 94) + (0.20 * 12) + (0.10 * 100) = 65.8 + 2.4 + 10.0 = 78.2
        assertEquals(78.2, areaDto.getAreaHealthScore());
    }

    @Test
    @DisplayName("Poor Compliance Area: 30% compliance, 50% coverage, 0% resolution rate")
    void testPoorComplianceArea() {
        when(areaRepository.findAll()).thenReturn(List.of(testArea));
        when(buildingRepository.countByAreaId(1L)).thenReturn(20L);
        when(inspectionRepository.countDistinctInspectedBuildingsByAreaId(1L)).thenReturn(10L); // 50% coverage
        when(inspectionRepository.countByAreaId(1L)).thenReturn(10L);
        when(inspectionRepository.getAverageScoreByAreaId(1L)).thenReturn(30.0); // 30% POOR
        when(violationRepository.countByAreaId(1L)).thenReturn(4L);
        when(violationRepository.countByAreaIdAndStatus(1L, ViolationStatus.RESOLVED)).thenReturn(0L); // 0% resolved
        when(buildingRepository.findByAreaId(1L)).thenReturn(Collections.emptyList());

        List<AreaAnalyticsDto> result = analyticsService.getAreaAnalytics();

        assertEquals(1, result.size());
        AreaAnalyticsDto areaDto = result.get(0);

        assertEquals(30.0, areaDto.getAverageCompliance());
        assertEquals("POOR", areaDto.getComplianceStatus());
        assertEquals(50.0, areaDto.getInspectionCoverage());
        assertEquals("MEDIUM", areaDto.getConfidenceRating());
        assertEquals(0.0, areaDto.getViolationResolutionRate());

        // Formula: (0.70 * 30) + (0.20 * 50) + (0.10 * 0) = 21.0 + 10.0 + 0.0 = 31.0
        assertEquals(31.0, areaDto.getAreaHealthScore());
    }

    @Test
    @DisplayName("Hotspot Detection: Identifies repeat offender room with multiple poor inspections")
    void testHotspotRepeatOffenderRoom() {
        when(roomRepository.findAll()).thenReturn(List.of(testRoom1));

        Inspection insp1 = new Inspection();
        insp1.setId(501L);
        insp1.setRoom(testRoom1);
        insp1.setTotalScore(25);
        insp1.setClassification(ComplianceClassification.POOR);
        insp1.setInspectionDate(LocalDateTime.now().minusDays(2));

        Inspection insp2 = new Inspection();
        insp2.setId(502L);
        insp2.setRoom(testRoom1);
        insp2.setTotalScore(35);
        insp2.setClassification(ComplianceClassification.POOR);
        insp2.setInspectionDate(LocalDateTime.now().minusDays(5));

        when(inspectionRepository.findByRoomIdOrderByInspectionDateDesc(101L)).thenReturn(List.of(insp1, insp2));
        when(violationRepository.countByRoomIdAndStatusNot(101L, ViolationStatus.RESOLVED)).thenReturn(1L);

        // Building analytics mocks for hotspot problem building check
        when(buildingRepository.findAll()).thenReturn(Collections.emptyList());
        when(areaRepository.findAll()).thenReturn(Collections.emptyList());

        HotspotAnalyticsDto hotspots = analyticsService.getHotspots();

        assertNotNull(hotspots);
        assertEquals(1, hotspots.getRepeatOffenderRooms().size());

        RepeatOffenderRoomDto roomDto = hotspots.getRepeatOffenderRooms().get(0);
        assertEquals(101L, roomDto.getRoomId());
        assertEquals(2, roomDto.getFailedInspectionsCount());
        assertEquals(25, roomDto.getLatestScore());
        assertEquals("CRITICAL", roomDto.getRiskLevel()); // 2 failed + open violation = CRITICAL
    }

    @Test
    @DisplayName("Area Improvement: Calculates positive score delta for improved areas")
    void testAreaImprovements() {
        when(areaRepository.findAll()).thenReturn(List.of(testArea));
        when(buildingRepository.countByAreaId(1L)).thenReturn(10L);

        Inspection recent = new Inspection();
        recent.setTotalScore(90);
        recent.setBuilding(testBuilding);

        Inspection previous = new Inspection();
        previous.setTotalScore(60);
        previous.setBuilding(testBuilding);

        when(inspectionRepository.findByEffectiveAreaIdAndInspectionDateBetween(eq(1L), any(), any()))
                .thenReturn(List.of(recent))
                .thenReturn(List.of(previous));

        List<AreaImprovementDto> list = analyticsService.getAreaImprovements(14);

        assertEquals(1, list.size());
        AreaImprovementDto dto = list.get(0);

        assertEquals(90.0, dto.getRecentAverageScore());
        assertEquals(60.0, dto.getPreviousAverageScore());
        assertEquals(30.0, dto.getScoreDelta());
        assertEquals("IMPROVED", dto.getTrendStatus());
    }

    @Test
    @DisplayName("Volunteer Analytics: Accurately evaluates audits count and ACTIVE status")
    void testVolunteerProductivity() {
        when(userRepository.findByRole(UserRole.VOLUNTEER)).thenReturn(List.of(testVolunteer));
        when(inspectionRepository.countByVolunteerId(20L)).thenReturn(15L);
        when(inspectionRepository.getAverageScoreByVolunteerId(20L)).thenReturn(82.5);
        when(inspectionRepository.countDistinctInspectedRoomsByVolunteerId(20L)).thenReturn(12L);
        when(inspectionRepository.countDistinctInspectedBuildingsByVolunteerId(20L)).thenReturn(3L);
        when(inspectionRepository.countViolationsFlaggedByVolunteerId(20L)).thenReturn(2L);

        Inspection latest = new Inspection();
        latest.setInspectionDate(LocalDateTime.now().minusDays(1)); // within 7 days -> ACTIVE

        when(inspectionRepository.findTop20ByVolunteerIdOrderByInspectionDateDesc(20L)).thenReturn(List.of(latest));
        when(inspectionRepository.findByVolunteerIdOrderByInspectionDateDesc(20L)).thenReturn(List.of(latest));

        List<VolunteerAnalyticsDto> result = analyticsService.getVolunteerAnalytics();

        assertEquals(1, result.size());
        VolunteerAnalyticsDto vDto = result.get(0);

        assertEquals(15L, vDto.getInspectionsCount());
        assertEquals(82.5, vDto.getAverageScoreAwarded());
        assertEquals(3L, vDto.getDistinctRoomsInspected());
        assertEquals(3L, vDto.getDistinctBuildingsCovered());
        assertEquals("ACTIVE", vDto.getActivityStatus());
    }
}
