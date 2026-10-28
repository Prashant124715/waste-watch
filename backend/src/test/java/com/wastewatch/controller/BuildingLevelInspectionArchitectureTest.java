package com.wastewatch.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wastewatch.dto.auth.LoginRequestDto;
import com.wastewatch.dto.auth.RegisterRequestDto;
import com.wastewatch.dto.inspection.InspectionRequestDto;
import com.wastewatch.entity.Building;
import com.wastewatch.entity.enums.UserRole;
import com.wastewatch.repository.BuildingRepository;
import com.wastewatch.repository.InspectionRepository;
import com.wastewatch.repository.ViolationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Architecture Verification Suite — Scope Reduction to Building Level.
 *
 * Verifies all 12 target architectural requirements:
 * 1. Volunteer can select/retrieve a Building.
 * 2. Volunteer can submit an inspection using ONLY buildingId.
 * 3. Inspection is saved successfully.
 * 4. Inspection has the correct Building.
 * 5. Inspection does not require a Room.
 * 6. Low score (<40) creates the appropriate violation tied to Building.
 * 7. Inspection appears in volunteer history.
 * 8. Inspection appears in admin inspection list.
 * 9. Analytics still work at building level.
 * 10. Building map still works.
 * 11. Existing registration/login still works.
 * 12. Static assets & loading resources remain available.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class BuildingLevelInspectionArchitectureTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BuildingRepository buildingRepository;

    @Autowired
    private InspectionRepository inspectionRepository;

    @Autowired
    private ViolationRepository violationRepository;

    private String volunteerToken;
    private String adminToken;
    private Building testBuilding;

    @BeforeEach
    void setUp() throws Exception {
        // Authenticate volunteer
        LoginRequestDto volLogin = new LoginRequestDto("volunteer", "volunteer123");
        MvcResult volRes = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(volLogin)))
                .andExpect(status().isOk())
                .andReturn();
        volunteerToken = objectMapper.readTree(volRes.getResponse().getContentAsString()).get("token").asText();

        // Authenticate admin
        LoginRequestDto adminLogin = new LoginRequestDto("admin", "admin123");
        MvcResult adminRes = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminLogin)))
                .andExpect(status().isOk())
                .andReturn();
        adminToken = objectMapper.readTree(adminRes.getResponse().getContentAsString()).get("token").asText();

        List<Building> buildings = buildingRepository.findAll();
        assertFalse(buildings.isEmpty(), "Seed buildings must exist");
        testBuilding = buildings.get(0);
    }

    @Test
    @DisplayName("Test 1: Volunteer can select/query available Buildings")
    void test1_volunteerCanSelectBuilding() throws Exception {
        mockMvc.perform(get("/api/buildings")
                        .header("Authorization", "Bearer " + volunteerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[0].buildingCode").exists());
    }

    @Test
    @DisplayName("Test 2, 3, 4, 5: Submit inspection with ONLY buildingId — saved, correct building, room is null")
    void test2to5_submitInspectionWithOnlyBuildingId() throws Exception {
        Building seedBuilding = buildingRepository.findAll().get(0);
        Building targetBuilding = buildingRepository.save(new Building(
                seedBuilding.getArea(),
                "BLD-ARCH-TEST-A-" + System.currentTimeMillis(),
                "Arch Test Building A",
                "100 Arch Way",
                seedBuilding.getLatitude(),
                seedBuilding.getLongitude(),
                com.wastewatch.entity.enums.BuildingType.RESIDENTIAL,
                10
        ));

        InspectionRequestDto dto = new InspectionRequestDto();
        dto.setBuildingId(targetBuilding.getId());
        dto.setRoomId(null); // Explicitly NO room
        dto.setWetWasteSeparated(true);
        dto.setDryWasteSeparated(true);
        dto.setSanitaryWasteSeparated(true);
        dto.setCorrectBinsUsed(true);
        dto.setBinsLabelled(true);
        dto.setNoMixedWaste(true);
        dto.setRemarks("Pure building audit without any room object.");

        MvcResult result = mockMvc.perform(post("/api/inspections")
                        .header("Authorization", "Bearer " + volunteerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated()) // Test 3: saved successfully
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.buildingId").value(targetBuilding.getId())) // Test 4: correct building
                .andExpect(jsonPath("$.data.roomNumber").doesNotExist()) // Test 5: room not present / not required
                .andExpect(jsonPath("$.data.totalScore").value(100))
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString()).get("data");
        long inspectionId = json.get("id").asLong();

        // Verify entity in repository
        com.wastewatch.entity.Inspection savedEntity = inspectionRepository.findById(inspectionId).orElseThrow();
        assertNotNull(savedEntity.getBuilding(), "Building must be set");
        assertEquals(targetBuilding.getId(), savedEntity.getBuilding().getId());
        assertNull(savedEntity.getRoom(), "Room MUST be null for building-level inspection");
    }

    @Test
    @DisplayName("Test 6: Low score on building inspection creates appropriate violation")
    void test6_lowScoreCreatesViolation() throws Exception {
        Building seedBuilding = buildingRepository.findAll().get(0);
        Building targetBuilding = buildingRepository.save(new Building(
                seedBuilding.getArea(),
                "BLD-ARCH-TEST-B-" + System.currentTimeMillis(),
                "Arch Test Building B",
                "200 Arch Way",
                seedBuilding.getLatitude(),
                seedBuilding.getLongitude(),
                com.wastewatch.entity.enums.BuildingType.COMMERCIAL,
                5
        ));

        InspectionRequestDto dto = new InspectionRequestDto();
        dto.setBuildingId(targetBuilding.getId());
        dto.setWetWasteSeparated(false);
        dto.setDryWasteSeparated(false);
        dto.setSanitaryWasteSeparated(false);
        dto.setCorrectBinsUsed(true); // 15 pts -> POOR score
        dto.setBinsLabelled(false);
        dto.setNoMixedWaste(false);
        dto.setRemarks("Severe building contamination.");

        MvcResult result = mockMvc.perform(post("/api/inspections")
                        .header("Authorization", "Bearer " + volunteerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.totalScore").value(15))
                .andExpect(jsonPath("$.data.classification").value("POOR"))
                .andExpect(jsonPath("$.data.violationCreated").value(true))
                .andExpect(jsonPath("$.data.violationId", notNullValue()))
                .andReturn();

        long violationId = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("data").get("violationId").asLong();

        com.wastewatch.entity.Violation violation = violationRepository.findById(violationId).orElseThrow();
        assertEquals(targetBuilding.getId(), violation.getBuilding().getId());
        assertNull(violation.getRoom(), "Violation must not require a room");
    }

    @Test
    @DisplayName("Test 7: Inspection appears in volunteer history")
    void test7_appearsInVolunteerHistory() throws Exception {
        mockMvc.perform(get("/api/volunteers/me/inspections")
                        .header("Authorization", "Bearer " + volunteerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].buildingName", notNullValue()));
    }

    @Test
    @DisplayName("Test 8: Inspection appears in admin inspection list and building filter")
    void test8_appearsInAdminInspectionList() throws Exception {
        mockMvc.perform(get("/api/inspections")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));

        mockMvc.perform(get("/api/buildings/" + testBuilding.getId() + "/inspections")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Test 9: Analytics still work at building level")
    void test9_analyticsStillWork() throws Exception {
        mockMvc.perform(get("/api/analytics/overview")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.systemHealthScore", notNullValue()))
                .andExpect(jsonPath("$.data.totalBuildings", greaterThan(0)));

        mockMvc.perform(get("/api/analytics/areas")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].areaHealthScore", notNullValue()));

        mockMvc.perform(get("/api/analytics/buildings")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].buildingId", notNullValue()));
    }

    @Test
    @DisplayName("Test 10: Building map still works")
    void test10_buildingMapStillWorks() throws Exception {
        mockMvc.perform(get("/api/map/buildings")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data[0].latitude", notNullValue()))
                .andExpect(jsonPath("$.data[0].longitude", notNullValue()));
    }

    @Test
    @DisplayName("Test 11: Registration and login still work")
    void test11_registrationAndLoginWork() throws Exception {
        String uniqueUser = "vol_" + System.currentTimeMillis();
        RegisterRequestDto reg = new RegisterRequestDto();
        reg.setUsername(uniqueUser);
        reg.setEmail(uniqueUser + "@example.com");
        reg.setPassword("Pass@12345");
        reg.setConfirmPassword("Pass@12345");
        reg.setFullName("Automated Scope Volunteer");
        reg.setPhoneNumber("9876543210");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.username").value(uniqueUser));

        LoginRequestDto login = new LoginRequestDto("volunteer", "volunteer123");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()));
    }

    @Test
    @DisplayName("Test 12: Loading animation & public static resources are preserved")
    void test12_loadingAnimationPreserved() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));

        java.io.File indexHtml = new java.io.File("../frontend/index.html");
        assertTrue(indexHtml.exists(), "frontend/index.html must exist");
        String content = java.nio.file.Files.readString(indexHtml.toPath());
        assertTrue(content.contains("loading-overlay") || content.contains("spinner"), "Loading animation must be preserved");
    }
}
