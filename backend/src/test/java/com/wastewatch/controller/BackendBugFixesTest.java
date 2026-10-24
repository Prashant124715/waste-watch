package com.wastewatch.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wastewatch.dto.auth.LoginRequestDto;
import com.wastewatch.dto.volunteer.VolunteerStatusUpdateDto;
import com.wastewatch.entity.*;
import com.wastewatch.entity.enums.ComplianceClassification;
import com.wastewatch.entity.enums.InspectionStatus;
import com.wastewatch.entity.enums.UserStatus;
import com.wastewatch.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BackendBugFixesTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AreaRepository areaRepository;

    @Autowired
    private BuildingRepository buildingRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private InspectionRepository inspectionRepository;

    private String adminToken;
    private String volunteerToken;
    private Long adminUserId;
    private Long volunteerUserId;

    @BeforeEach
    void setUp() throws Exception {
        // Authenticate admin
        MvcResult adminResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequestDto("admin", "admin123"))))
                .andExpect(status().isOk())
                .andReturn();
        adminToken = objectMapper.readTree(adminResult.getResponse().getContentAsString()).get("token").asText();

        // Authenticate volunteer
        MvcResult volResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequestDto("volunteer", "volunteer123"))))
                .andExpect(status().isOk())
                .andReturn();
        volunteerToken = objectMapper.readTree(volResult.getResponse().getContentAsString()).get("token").asText();

        AppUser admin = userRepository.findByUsername("admin").orElseThrow();
        adminUserId = admin.getId();

        AppUser vol = userRepository.findByUsername("volunteer").orElseThrow();
        volunteerUserId = vol.getId();
    }

    @Test
    @DisplayName("Fix 1: Building-level inspections in ReportService execute without NPE and aggregate properly")
    void testBuildingLevelInspectionReportsDoNotThrowNpe() throws Exception {
        Building building = buildingRepository.findAll().stream().findFirst().orElseThrow();
        AppUser volunteer = userRepository.findByUsername("volunteer").orElseThrow();

        // Create a direct building-level inspection (room == null)
        Inspection buildingInspection = new Inspection();
        buildingInspection.setRoom(null);
        buildingInspection.setBuilding(building);
        buildingInspection.setVolunteer(volunteer);
        buildingInspection.setInspectionDate(LocalDateTime.now());
        buildingInspection.setTotalScore(85);
        buildingInspection.setClassification(ComplianceClassification.EXCELLENT);
        buildingInspection.setStatus(InspectionStatus.COMPLETED);
        buildingInspection.setWetWasteSeparated(true);
        buildingInspection.setDryWasteSeparated(true);
        buildingInspection.setSanitaryWasteSeparated(true);
        buildingInspection.setCorrectBinsUsed(true);
        buildingInspection.setBinsLabelled(true);
        buildingInspection.setNoMixedWaste(true);
        inspectionRepository.save(buildingInspection);

        // 1. Overview report
        mockMvc.perform(get("/api/reports/overview")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalInspections", greaterThanOrEqualTo(1)));

        // 2. Area performance report
        mockMvc.perform(get("/api/reports/areas")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(not(empty()))));

        // 3. Building performance report
        mockMvc.perform(get("/api/reports/buildings")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(not(empty()))));

        // 4. Coverage report
        mockMvc.perform(get("/api/reports/coverage")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRooms", greaterThan(0)));

        // 5. Volunteer activity report
        mockMvc.perform(get("/api/reports/volunteers")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(not(empty()))));

        // 6. CSV Export of Inspections
        MvcResult csvResult = mockMvc.perform(get("/api/reports/export/csv?type=inspections")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("wastewatch-inspections-report.csv")))
                .andReturn();

        String csv = csvResult.getResponse().getContentAsString();
        assertTrue(csv.contains("Inspection ID,Date,Room,Building"));
        assertTrue(csv.contains("BUILDING"));
    }

    @Test
    @DisplayName("Fix 2: Deactivated/suspended user JWT is rejected by JwtAuthenticationFilter")
    void testDeactivatedUserJwtIsRejected() throws Exception {
        // Volunteer initially has valid token and can access profile/self
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + volunteerToken))
                .andExpect(status().isOk());

        // Admin deactivates volunteer
        mockMvc.perform(put("/api/volunteers/" + volunteerUserId + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new VolunteerStatusUpdateDto(UserStatus.INACTIVE))))
                .andExpect(status().isOk());

        // Previously issued JWT must now be rejected
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + volunteerToken))
                .andExpect(status().isUnauthorized());

        // Restore volunteer status back to ACTIVE for cleanup
        AppUser vol = userRepository.findById(volunteerUserId).orElseThrow();
        vol.setStatus(UserStatus.ACTIVE);
        userRepository.save(vol);
    }

    @Test
    @DisplayName("Fix 3: Invalid parameter value returns 400 Bad Request instead of 500")
    void testInvalidQueryParamReturns400() throws Exception {
        mockMvc.perform(get("/api/violations?status=INVALID_STATUS_VALUE")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", containsString("Parameter 'status' has an invalid value")));
    }

    @Test
    @DisplayName("Fix 4: Malformed JSON payload returns 400 Bad Request instead of 500")
    void testMalformedJsonReturns400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ invalid_json: true "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", containsString("Malformed JSON request payload")));
    }

    @Test
    @DisplayName("Fix 5: Referential integrity guards on Area, Building, and Room deletion")
    void testReferentialIntegrityDeletions() throws Exception {
        Area areaWithBuildings = areaRepository.findAll().stream()
                .filter(a -> buildingRepository.countByAreaId(a.getId()) > 0)
                .findFirst().orElseThrow();

        // Attempting to delete Area with buildings must return 400 InvalidRequestException
        mockMvc.perform(delete("/api/areas/" + areaWithBuildings.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Cannot delete area: Area has associated buildings")));

        Building buildingWithRooms = buildingRepository.findAll().stream()
                .filter(b -> roomRepository.countByBuildingId(b.getId()) > 0)
                .findFirst().orElseThrow();

        // Attempting to delete Building with rooms must return 400 InvalidRequestException
        mockMvc.perform(delete("/api/buildings/" + buildingWithRooms.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Cannot delete building: Building has associated rooms")));

        Room roomWithInspections = roomRepository.findAll().stream()
                .filter(r -> inspectionRepository.countByRoomId(r.getId()) > 0)
                .findFirst().orElseThrow();

        // Attempting to delete Room with inspections must return 400 InvalidRequestException
        mockMvc.perform(delete("/api/rooms/" + roomWithInspections.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Cannot delete room: Room has existing inspection records")));
    }

    @Test
    @DisplayName("Fix 6: VolunteerService rejects retrieving performance for admin users")
    void testVolunteerPerformanceRejectsAdminRole() throws Exception {
        mockMvc.perform(get("/api/volunteers/" + adminUserId + "/performance")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Cannot retrieve volunteer performance for non-volunteer user")));
    }
}
