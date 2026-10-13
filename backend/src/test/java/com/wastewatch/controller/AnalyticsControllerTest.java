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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AnalyticsControllerTest {

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
    @DisplayName("GET /api/analytics/overview: Returns 200 with system overview metrics")
    void testGetOverviewAnalytics() throws Exception {
        mockMvc.perform(get("/api/analytics/overview")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.totalInspections", notNullValue()))
                .andExpect(jsonPath("$.data.systemHealthScore", notNullValue()))
                .andExpect(jsonPath("$.data.overallComplianceRate", notNullValue()))
                .andExpect(jsonPath("$.data.overallInspectionCoverage", notNullValue()));
    }

    @Test
    @DisplayName("GET /api/analytics/areas: Returns 200 with area health scores")
    void testGetAreaAnalytics() throws Exception {
        mockMvc.perform(get("/api/analytics/areas")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", isA(java.util.List.class)))
                .andExpect(jsonPath("$.data[0].areaHealthScore", notNullValue()))
                .andExpect(jsonPath("$.data[0].confidenceRating", notNullValue()));
    }

    @Test
    @DisplayName("GET /api/analytics/buildings: Returns 200 with building matrix")
    void testGetBuildingAnalytics() throws Exception {
        mockMvc.perform(get("/api/analytics/buildings")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", isA(java.util.List.class)))
                .andExpect(jsonPath("$.data[0].buildingName", notNullValue()))
                .andExpect(jsonPath("$.data[0].inspectionCoverage", notNullValue()));
    }

    @Test
    @DisplayName("GET /api/analytics/trends: Returns 200 with time-series buckets")
    void testGetTrendAnalytics() throws Exception {
        mockMvc.perform(get("/api/analytics/trends?period=daily&days=7")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.period", is("daily")))
                .andExpect(jsonPath("$.data.points", isA(java.util.List.class)));
    }

    @Test
    @DisplayName("GET /api/analytics/improvements: Returns 200 with period comparison")
    void testGetAreaImprovements() throws Exception {
        mockMvc.perform(get("/api/analytics/improvements?days=14")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", isA(java.util.List.class)));
    }

    @Test
    @DisplayName("GET /api/analytics/hotspots: Returns 200 with repeat offenders and problem buildings")
    void testGetHotspots() throws Exception {
        mockMvc.perform(get("/api/analytics/hotspots")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.repeatOffenderRooms", notNullValue()))
                .andExpect(jsonPath("$.data.problemBuildings", notNullValue()));
    }

    @Test
    @DisplayName("GET /api/analytics/volunteers: Returns 200 with volunteer productivity")
    void testGetVolunteerAnalytics() throws Exception {
        mockMvc.perform(get("/api/analytics/volunteers")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", isA(java.util.List.class)))
                .andExpect(jsonPath("$.data[0].inspectionsCount", notNullValue()));
    }

    @Test
    @DisplayName("Security RBAC: Volunteer receives 403 Forbidden on analytics endpoints")
    void testVolunteerForbiddenOnAnalytics() throws Exception {
        mockMvc.perform(get("/api/analytics/overview")
                        .header("Authorization", "Bearer " + volunteerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Security RBAC: Unauthenticated user receives 401 Unauthorized")
    void testUnauthenticatedOnAnalytics() throws Exception {
        mockMvc.perform(get("/api/analytics/overview"))
                .andExpect(status().isUnauthorized());
    }
}
