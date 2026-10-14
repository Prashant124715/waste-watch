package com.wastewatch.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wastewatch.dto.auth.LoginRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MapControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String volunteerToken;

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
    }

    @Test
    @DisplayName("GET /api/map/buildings: Admin receives 200 OK with complete building marker schema")
    void testGetBuildingMapMarkersAsAdmin() throws Exception {
        mockMvc.perform(get("/api/map/buildings")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", isA(java.util.List.class)))
                .andExpect(jsonPath("$.data[0].id", notNullValue()))
                .andExpect(jsonPath("$.data[0].buildingId", notNullValue()))
                .andExpect(jsonPath("$.data[0].name", notNullValue()))
                .andExpect(jsonPath("$.data[0].buildingName", notNullValue()))
                .andExpect(jsonPath("$.data[0].area", notNullValue()))
                .andExpect(jsonPath("$.data[0].complianceStatus", notNullValue()))
                .andExpect(jsonPath("$.data[0].inspectionCoverage", notNullValue()))
                .andExpect(jsonPath("$.data[0].hasValidCoordinates", notNullValue()));
    }

    @Test
    @DisplayName("GET /api/map/buildings: Volunteer receives 200 OK for field navigation")
    void testGetBuildingMapMarkersAsVolunteer() throws Exception {
        mockMvc.perform(get("/api/map/buildings")
                        .header("Authorization", "Bearer " + volunteerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", isA(java.util.List.class)));
    }

    @Test
    @DisplayName("GET /api/map/buildings: Unauthenticated request returns 401 Unauthorized")
    void testGetBuildingMapMarkersUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/map/buildings"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/map/buildings: Filter by areaId returns filtered markers")
    void testFilterByAreaId() throws Exception {
        mockMvc.perform(get("/api/map/buildings")
                        .param("areaId", "1")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", isA(java.util.List.class)));
    }

    @Test
    @DisplayName("GET /api/map/buildings: Filter by complianceStatus returns matching buildings")
    void testFilterByComplianceStatus() throws Exception {
        mockMvc.perform(get("/api/map/buildings")
                        .param("complianceStatus", "EXCELLENT")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", isA(java.util.List.class)));
    }

    @Test
    @DisplayName("GET /api/map/hotspots: Returns 200 OK with list of area-level hotspot analytics")
    void testGetAreaHotspots() throws Exception {
        mockMvc.perform(get("/api/map/hotspots")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", isA(java.util.List.class)))
                .andExpect(jsonPath("$.data[0].areaId", notNullValue()))
                .andExpect(jsonPath("$.data[0].areaName", notNullValue()))
                .andExpect(jsonPath("$.data[0].hotspotSeverity", notNullValue()));
    }
}
