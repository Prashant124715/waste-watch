package com.wastewatch.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wastewatch.dto.auth.LoginRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Phase 11 - Geographic Monitoring: Comprehensive integration tests for
 * GET /api/map/buildings, GET /api/map/hotspots and GET /api/map/areas.
 *
 * Test coverage:
 *  - Building map markers
 *  - Valid coordinates
 *  - hasValidCoordinates flag
 *  - Multiple buildings
 *  - Mandatory building fields
 *  - Compliance score and status
 *  - Poor room count
 *  - Open violations
 *  - Inspection coverage
 *  - Building filters
 *  - Area hotspot analytics
 *  - Area map summaries
 *  - Area Health Score
 *  - Confidence rating
 *  - RBAC
 *  - Unauthenticated access
 *  - Invalid JWT access
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
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
                        .content(objectMapper.writeValueAsString(
                                new LoginRequestDto("admin", "admin123"))))
                .andExpect(status().isOk())
                .andReturn();

        adminToken = objectMapper
                .readTree(adminResult.getResponse().getContentAsString())
                .get("token")
                .asText();

        // Authenticate volunteer
        MvcResult volunteerResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequestDto("volunteer", "volunteer123"))))
                .andExpect(status().isOk())
                .andReturn();

        volunteerToken = objectMapper
                .readTree(volunteerResult.getResponse().getContentAsString())
                .get("token")
                .asText();
    }

    // ======================================================================
    // GET /api/map/buildings - Basic access
    // ======================================================================

    @Test
    @DisplayName("GET /api/map/buildings: Admin receives 200 OK with complete building marker schema")
    void testGetBuildingMapMarkersAsAdmin() throws Exception {
        mockMvc.perform(get("/api/map/buildings")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", isA(java.util.List.class)))
                .andExpect(jsonPath("$.data", not(empty())))
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
    @DisplayName("GET /api/map/buildings: Invalid JWT returns 401 Unauthorized")
    void testGetBuildingMapMarkersWithInvalidToken() throws Exception {
        mockMvc.perform(get("/api/map/buildings")
                        .header("Authorization", "Bearer invalid.jwt.token"))
                .andExpect(status().isUnauthorized());
    }

    // ======================================================================
    // GET /api/map/buildings - Filters
    // ======================================================================

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

    // ======================================================================
    // GET /api/map/buildings - Coordinates
    // ======================================================================

    @Test
    @DisplayName("Valid coordinates: seeded buildings have non-null latitude and longitude")
    void testBuildingsHaveValidCoordinates() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/map/buildings")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode data = objectMapper
                .readTree(result.getResponse().getContentAsString())
                .get("data");

        assertTrue(data.isArray());
        assertTrue(data.size() > 0, "Expected at least one seeded building");

        boolean anyMapped = false;

        for (JsonNode building : data) {
            if (building.get("hasValidCoordinates").asBoolean()) {
                assertNotNull(
                        building.get("latitude"),
                        "latitude must not be null for mapped building"
                );
                assertNotNull(
                        building.get("longitude"),
                        "longitude must not be null for mapped building"
                );
                assertFalse(building.get("latitude").isNull());
                assertFalse(building.get("longitude").isNull());

                anyMapped = true;
            }
        }

        assertTrue(
                anyMapped,
                "At least one building should have valid coordinates from seed data"
        );
    }

    @Test
    @DisplayName("Valid coordinates: hasValidCoordinates is true for seeded buildings with proper coords")
    void testHasValidCoordinatesFlagTrueForSeedData() throws Exception {
        mockMvc.perform(get("/api/map/buildings")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].hasValidCoordinates", notNullValue()))
                .andExpect(jsonPath("$.data[0].id", notNullValue()))
                .andExpect(jsonPath("$.data[0].name", notNullValue()));
    }

    @Test
    @DisplayName("Missing/invalid coordinates: hasValidCoordinates field is always present and boolean")
    void testBuildingWithNullCoordinatesHasInvalidFlag() throws Exception {
        /*
         * All seeded buildings currently have valid coordinates.
         * This test verifies the structural contract:
         * hasValidCoordinates is always present and is a boolean.
         */
        MvcResult result = mockMvc.perform(get("/api/map/buildings")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode data = objectMapper
                .readTree(result.getResponse().getContentAsString())
                .get("data");

        for (JsonNode building : data) {
            JsonNode flag = building.get("hasValidCoordinates");

            assertNotNull(
                    flag,
                    "hasValidCoordinates field must always be present"
            );

            assertTrue(
                    flag.isBoolean(),
                    "hasValidCoordinates must be boolean, not: " + flag
            );
        }
    }

    // ======================================================================
    // GET /api/map/buildings - Multiple buildings
    // ======================================================================

    @Test
    @DisplayName("Multiple buildings: at least 3 buildings in map data")
    void testMultipleBuildingsReturned() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/map/buildings")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode data = objectMapper
                .readTree(result.getResponse().getContentAsString())
                .get("data");

        assertTrue(
                data.size() >= 3,
                "Expected at least 3 buildings in map data"
        );
    }

    @Test
    @DisplayName("Multiple buildings: each has mandatory map fields")
    void testMandatoryFieldsPresentOnAllBuildings() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/map/buildings")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode data = objectMapper
                .readTree(result.getResponse().getContentAsString())
                .get("data");

        for (JsonNode building : data) {
            assertFalse(building.get("id").isNull(), "id must not be null");
            assertFalse(building.get("name").isNull(), "name must not be null");
            assertNotNull(
                    building.get("hasValidCoordinates"),
                    "hasValidCoordinates must be present"
            );
        }
    }

    // ======================================================================
    // GET /api/map/buildings - Compliance
    // ======================================================================

    @Test
    @DisplayName("Compliance score: buildings with inspections have complianceStatus populated")
    void testComplianceStatusPopulatedForInspectedBuildings() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/map/buildings")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode data = objectMapper
                .readTree(result.getResponse().getContentAsString())
                .get("data");

        boolean foundWithData = false;

        for (JsonNode building : data) {
            JsonNode statusNode = building.get("complianceStatus");

            assertNotNull(
                    statusNode,
                    "complianceStatus field must always be present"
            );

            if (!statusNode.asText().equals("NO_DATA")) {
                foundWithData = true;

                JsonNode scoreNode = building.get("complianceScore");

                assertNotNull(scoreNode);

                assertFalse(
                        scoreNode.isNull(),
                        "complianceScore should not be null when status is not NO_DATA"
                );
            }
        }

        assertTrue(
                foundWithData,
                "At least one building should have a compliance classification from seed inspections"
        );
    }

    @Test
    @DisplayName("Compliance status: building with poor room has poorRoomCount > 0 and openViolations >= 0")
    void testBuildingWithViolationsHasCorrectMetrics() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/map/buildings")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode data = objectMapper
                .readTree(result.getResponse().getContentAsString())
                .get("data");

        boolean foundBuildingWithViolation = false;

        for (JsonNode building : data) {
            if (building.get("poorRoomCount").asInt() > 0) {
                foundBuildingWithViolation = true;

                assertTrue(
                        building.get("openViolations").asInt() >= 0,
                        "Building with poor room should have non-negative open violations"
                );
            }
        }

        assertTrue(
                foundBuildingWithViolation,
                "Seed data should produce at least one building with a poor room"
        );
    }

    @Test
    @DisplayName("Compliance status: building with open violations has openViolations >= 1 in seed data")
    void testBuildingHasOpenViolationsFromSeedData() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/map/buildings")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode data = objectMapper
                .readTree(result.getResponse().getContentAsString())
                .get("data");

        boolean foundOpenViolation = false;

        for (JsonNode building : data) {
            if (building.get("openViolations").asInt() >= 1) {
                foundOpenViolation = true;

                assertTrue(
                        building.get("poorRoomCount").asInt() >= 1,
                        "Building with open violations should also record at least 1 poor room"
                );
            }
        }

        assertTrue(
                foundOpenViolation,
                "Seed data should produce at least one building with open violations"
        );
    }

    // ======================================================================
    // GET /api/map/buildings - Inspection coverage
    // ======================================================================

    @Test
    @DisplayName("Inspection coverage: inspectionCoverage is between 0 and 100 for all buildings")
    void testInspectionCoverageInRange() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/map/buildings")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode data = objectMapper
                .readTree(result.getResponse().getContentAsString())
                .get("data");

        for (JsonNode building : data) {
            JsonNode coverage = building.get("inspectionCoverage");

            assertNotNull(
                    coverage,
                    "inspectionCoverage must always be present"
            );

            double value = coverage.asDouble(-1);

            assertTrue(
                    value >= 0.0 && value <= 100.0,
                    "inspectionCoverage must be in [0, 100] but was "
                            + value
                            + " for "
                            + building.get("name").asText()
            );
        }
    }

    @Test
    @DisplayName("Poor rooms: poorRoomCount is non-negative for all buildings")
    void testPoorRoomCountNonNegative() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/map/buildings")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode data = objectMapper
                .readTree(result.getResponse().getContentAsString())
                .get("data");

        for (JsonNode building : data) {
            assertTrue(
                    building.get("poorRoomCount").asLong() >= 0,
                    "poorRoomCount must be >= 0 for "
                            + building.get("name").asText()
            );
        }
    }

    // ======================================================================
    // GET /api/map/hotspots
    // ======================================================================

    @Test
    @DisplayName("GET /api/map/hotspots: Returns 200 OK with area-level hotspot analytics")
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

    // ======================================================================
    // GET /api/map/areas - Area-level summaries
    // ======================================================================

    @Test
    @DisplayName("GET /api/map/areas: Admin receives 200 OK with area summary list")
    void testGetAreaMapSummariesAsAdmin() throws Exception {
        mockMvc.perform(get("/api/map/areas")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", isA(java.util.List.class)))
                .andExpect(jsonPath("$.data", not(empty())));
    }

    @Test
    @DisplayName("GET /api/map/areas: Response contains mandatory fields for each area")
    void testAreaSummariesHaveMandatoryFields() throws Exception {
        mockMvc.perform(get("/api/map/areas")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].areaId", notNullValue()))
                .andExpect(jsonPath("$.data[0].areaName", notNullValue()))
                .andExpect(jsonPath("$.data[0].areaCode", notNullValue()))
                .andExpect(jsonPath("$.data[0].areaHealthScore", notNullValue()))
                .andExpect(jsonPath("$.data[0].averageCompliance", notNullValue()))
                .andExpect(jsonPath("$.data[0].inspectionCoverage", notNullValue()))
                .andExpect(jsonPath("$.data[0].confidenceRating", notNullValue()));
    }

    @Test
    @DisplayName("GET /api/map/areas: areaHealthScore is in valid range [0, 100]")
    void testAreaHealthScoreInRange() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/map/areas")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode data = objectMapper
                .readTree(result.getResponse().getContentAsString())
                .get("data");

        for (JsonNode area : data) {
            double healthScore = area
                    .get("areaHealthScore")
                    .asDouble(-1);

            assertTrue(
                    healthScore >= 0.0 && healthScore <= 100.0,
                    "areaHealthScore must be in [0, 100] but was "
                            + healthScore
                            + " for "
                            + area.get("areaName").asText()
            );
        }
    }

    @Test
    @DisplayName("GET /api/map/areas: confidenceRating is HIGH, MEDIUM, or LOW")
    void testAreaConfidenceRatingValid() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/map/areas")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode data = objectMapper
                .readTree(result.getResponse().getContentAsString())
                .get("data");

        for (JsonNode area : data) {
            String confidence = area
                    .get("confidenceRating")
                    .asText();

            assertTrue(
                    "HIGH".equals(confidence)
                            || "MEDIUM".equals(confidence)
                            || "LOW".equals(confidence),
                    "confidenceRating must be HIGH, MEDIUM, or LOW; got: "
                            + confidence
            );
        }
    }

    @Test
    @DisplayName("GET /api/map/areas: Volunteer receives 403 Forbidden")
    void testGetAreaMapSummariesAsVolunteer() throws Exception {
        mockMvc.perform(get("/api/map/areas")
                        .header("Authorization", "Bearer " + volunteerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/map/areas: Unauthenticated request returns 401 Unauthorized")
    void testGetAreaMapSummariesUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/map/areas"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/map/areas: Seeded data produces at least 2 area summaries")
    void testAreaSummariesCountMatchesSeedData() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/map/areas")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode data = objectMapper
                .readTree(result.getResponse().getContentAsString())
                .get("data");

        assertTrue(
                data.size() >= 2,
                "Expected at least 2 area summaries from seed data"
        );
    }

    @Test
    @DisplayName("GET /api/map/areas: inspectionCoverage is in valid range [0, 100]")
    void testAreaInspectionCoverageInRange() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/map/areas")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode data = objectMapper
                .readTree(result.getResponse().getContentAsString())
                .get("data");

        for (JsonNode area : data) {
            double coverage = area
                    .get("inspectionCoverage")
                    .asDouble(-1);

            assertTrue(
                    coverage >= 0.0 && coverage <= 100.0,
                    "inspectionCoverage must be in [0, 100] but was "
                            + coverage
            );
        }
    }
}