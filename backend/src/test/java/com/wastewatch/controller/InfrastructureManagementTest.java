package com.wastewatch.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wastewatch.dto.area.AreaRequestDto;
import com.wastewatch.dto.auth.LoginRequestDto;
import com.wastewatch.dto.building.BuildingRequestDto;
import com.wastewatch.dto.room.RoomRequestDto;
import com.wastewatch.entity.enums.BuildingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class InfrastructureManagementTest {

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
    void testUnauthenticatedAccessReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/areas"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testVolunteerCanReadAreasAndBuildings() throws Exception {
        mockMvc.perform(get("/api/areas")
                        .header("Authorization", "Bearer " + volunteerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))));

        mockMvc.perform(get("/api/buildings")
                        .header("Authorization", "Bearer " + volunteerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))));
    }

    @Test
    void testVolunteerForbiddenFromCreatingArea() throws Exception {
        AreaRequestDto newArea = new AreaRequestDto(
                "ZONE-TEST-01", "Test Ward", "Metro City", "400055", "Test Description"
        );

        mockMvc.perform(post("/api/areas")
                        .header("Authorization", "Bearer " + volunteerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newArea)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testAdminCanCreateAndRetrieveArea() throws Exception {
        AreaRequestDto newArea = new AreaRequestDto(
                "ZONE-EAST-99", "East Ward - Sector 99", "Metro City", "400099", "New development area"
        );

        MvcResult createResult = mockMvc.perform(post("/api/areas")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newArea)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.areaCode").value("ZONE-EAST-99"))
                .andReturn();

        long areaId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/api/areas/" + areaId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.areaCode").value("ZONE-EAST-99"));
    }

    @Test
    void testDuplicateAreaCodeReturnsConflict() throws Exception {
        AreaRequestDto duplicateArea = new AreaRequestDto(
                "ZONE-NORTH-01", "Duplicate North Ward", "Metro City", "400001", "Duplicate"
        );

        mockMvc.perform(post("/api/areas")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateArea)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void testValidationFailureReturnsBadRequest() throws Exception {
        AreaRequestDto invalidArea = new AreaRequestDto("", "", "", "", "");

        mockMvc.perform(post("/api/areas")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidArea)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors", notNullValue()));
    }

    @Test
    void testAdminCanCreateBuildingAndDuplicateHandling() throws Exception {
        BuildingRequestDto buildingDto = new BuildingRequestDto();
        buildingDto.setAreaId(1L);
        buildingDto.setBuildingCode("BLD-TEST-501");
        buildingDto.setName("Test Tower");
        buildingDto.setAddress("501 Test Blvd");
        buildingDto.setLatitude(new BigDecimal("19.1000"));
        buildingDto.setLongitude(new BigDecimal("72.9000"));
        buildingDto.setBuildingType(BuildingType.COMMERCIAL);
        buildingDto.setTotalFloors(4);

        mockMvc.perform(post("/api/buildings")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildingDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.buildingCode").value("BLD-TEST-501"));

        // Duplicate code check
        mockMvc.perform(post("/api/buildings")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildingDto)))
                .andExpect(status().isConflict());
    }

    @Test
    void testDuplicateRoomInSameBuildingReturnsConflict() throws Exception {
        RoomRequestDto duplicateRoom = new RoomRequestDto();
        duplicateRoom.setBuildingId(1L);
        duplicateRoom.setFloorNumber(1);
        duplicateRoom.setRoomNumber("Flat 101"); // Already exists from seed data

        mockMvc.perform(post("/api/rooms")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRoom)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void testGetNonexistentResourceReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/areas/999999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        mockMvc.perform(get("/api/buildings/999999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/rooms/999999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }
}
