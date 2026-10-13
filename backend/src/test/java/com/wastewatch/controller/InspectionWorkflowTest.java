package com.wastewatch.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wastewatch.dto.auth.LoginRequestDto;
import com.wastewatch.dto.inspection.InspectionRequestDto;
import com.wastewatch.dto.violation.ViolationStatusUpdateDto;
import com.wastewatch.entity.Room;
import com.wastewatch.entity.enums.ViolationStatus;
import com.wastewatch.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class InspectionWorkflowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RoomRepository roomRepository;

    private String adminToken;
    private String volunteerToken;
    private Long testRoomId;
    private String testRoomQrToken;

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

        List<Room> rooms = roomRepository.findAll();
        assertFalse(rooms.isEmpty(), "Seed rooms must be present");
        Room room = rooms.get(0);
        testRoomId = room.getId();
        testRoomQrToken = room.getQrCodeToken();
    }

    @Test
    void testVolunteerSubmitsPerfectInspection() throws Exception {
        List<Room> rooms = roomRepository.findAll();
        Room room = rooms.get(2); // Room 201

        InspectionRequestDto requestDto = new InspectionRequestDto();
        requestDto.setRoomId(room.getId());
        requestDto.setWetWasteSeparated(true);
        requestDto.setDryWasteSeparated(true);
        requestDto.setSanitaryWasteSeparated(true);
        requestDto.setCorrectBinsUsed(true);
        requestDto.setBinsLabelled(true);
        requestDto.setNoMixedWaste(true);
        requestDto.setRemarks("Perfect segregation standards maintained.");

        MvcResult result = mockMvc.perform(post("/api/inspections")
                        .header("Authorization", "Bearer " + volunteerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalScore").value(100))
                .andExpect(jsonPath("$.data.classification").value("EXCELLENT"))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.violationCreated").value(false))
                .andExpect(jsonPath("$.data.volunteerName").value("Field Inspector Volunteer"))
                .andReturn();

        JsonNode responseNode = objectMapper.readTree(result.getResponse().getContentAsString());
        long inspectionId = responseNode.get("data").get("id").asLong();

        // Verify retrieval by ID
        mockMvc.perform(get("/api/inspections/" + inspectionId)
                        .header("Authorization", "Bearer " + volunteerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalScore").value(100));
    }

    @Test
    void testVolunteerSubmitsFailingInspectionTriggersAutoViolation() throws Exception {
        List<Room> rooms = roomRepository.findAll();
        Room room = rooms.get(3); // Suite 301

        InspectionRequestDto requestDto = new InspectionRequestDto();
        requestDto.setRoomId(room.getId());
        requestDto.setWetWasteSeparated(false);
        requestDto.setDryWasteSeparated(false);
        requestDto.setSanitaryWasteSeparated(false);
        requestDto.setCorrectBinsUsed(true); // 15 pts
        requestDto.setBinsLabelled(false);
        requestDto.setNoMixedWaste(false);
        requestDto.setRemarks("Gross cross-contamination across bins.");

        MvcResult result = mockMvc.perform(post("/api/inspections")
                        .header("Authorization", "Bearer " + volunteerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalScore").value(15))
                .andExpect(jsonPath("$.data.classification").value("POOR"))
                .andExpect(jsonPath("$.data.status").value("FLAGGED"))
                .andExpect(jsonPath("$.data.violationCreated").value(true))
                .andExpect(jsonPath("$.data.violationId", notNullValue()))
                .andReturn();

        JsonNode responseNode = objectMapper.readTree(result.getResponse().getContentAsString());
        long violationId = responseNode.get("data").get("violationId").asLong();

        // Admin checks violation
        mockMvc.perform(get("/api/violations/" + violationId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.severity").value("CRITICAL"));

        // Admin updates violation status to RESOLVED
        ViolationStatusUpdateDto updateDto = new ViolationStatusUpdateDto();
        updateDto.setStatus(ViolationStatus.RESOLVED);
        updateDto.setResolutionNotes("Re-inspected following morning; tenant rectified segregation.");
        updateDto.setFineAmount(new BigDecimal("100.00"));

        mockMvc.perform(put("/api/violations/" + violationId + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("RESOLVED"))
                .andExpect(jsonPath("$.data.resolvedAt", notNullValue()));
    }

    @Test
    void testVolunteerCannotAccessViolationsOrDashboard() throws Exception {
        mockMvc.perform(get("/api/violations")
                        .header("Authorization", "Bearer " + volunteerToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/dashboard/summary")
                        .header("Authorization", "Bearer " + volunteerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testAdminRetrievesDashboardAndMap() throws Exception {
        // Dashboard summary
        mockMvc.perform(get("/api/dashboard/summary")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalInspections", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.scoreDistribution.excellent", notNullValue()));

        // Dashboard trends
        mockMvc.perform(get("/api/dashboard/trends")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Map buildings
        mockMvc.perform(get("/api/map/buildings")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data[0].latitude", notNullValue()))
                .andExpect(jsonPath("$.data[0].longitude", notNullValue()));
    }

    @Test
    void testQrCodeLookupEndpoint() throws Exception {
        mockMvc.perform(get("/api/rooms/qr/" + testRoomQrToken)
                        .header("Authorization", "Bearer " + volunteerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qrCodeToken").value(testRoomQrToken))
                .andExpect(jsonPath("$.roomNumber", notNullValue()))
                .andExpect(jsonPath("$.buildingName", notNullValue()));
    }

    @Test
    void testVolunteerListAndPerformance() throws Exception {
        mockMvc.perform(get("/api/volunteers")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void testInvalidRoomReturns404NotFound() throws Exception {
        InspectionRequestDto requestDto = new InspectionRequestDto();
        requestDto.setRoomId(999999L);
        requestDto.setWetWasteSeparated(true);
        requestDto.setDryWasteSeparated(true);
        requestDto.setSanitaryWasteSeparated(true);
        requestDto.setCorrectBinsUsed(true);
        requestDto.setBinsLabelled(true);
        requestDto.setNoMixedWaste(true);

        mockMvc.perform(post("/api/inspections")
                        .header("Authorization", "Bearer " + volunteerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void testUnauthorizedRequestRejected() throws Exception {
        InspectionRequestDto requestDto = new InspectionRequestDto();
        requestDto.setRoomId(testRoomId);
        requestDto.setWetWasteSeparated(true);
        requestDto.setDryWasteSeparated(true);
        requestDto.setSanitaryWasteSeparated(true);
        requestDto.setCorrectBinsUsed(true);
        requestDto.setBinsLabelled(true);
        requestDto.setNoMixedWaste(true);

        mockMvc.perform(post("/api/inspections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testMissingRequiredFieldsReturns400BadRequest() throws Exception {
        InspectionRequestDto requestDto = new InspectionRequestDto();
        // roomId is missing

        mockMvc.perform(post("/api/inspections")
                        .header("Authorization", "Bearer " + volunteerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.validationErrors.roomId").exists());
    }

    @Test
    void testAccidentalDuplicateSubmissionPrevention() throws Exception {
        List<Room> rooms = roomRepository.findAll();
        Room room = rooms.get(0); // Flat 101

        InspectionRequestDto requestDto = new InspectionRequestDto();
        requestDto.setRoomId(room.getId());
        requestDto.setWetWasteSeparated(true);
        requestDto.setDryWasteSeparated(true);
        requestDto.setSanitaryWasteSeparated(true);
        requestDto.setCorrectBinsUsed(true);
        requestDto.setBinsLabelled(true);
        requestDto.setNoMixedWaste(true);
        requestDto.setRemarks("First immediate submission.");

        // First submission succeeds
        mockMvc.perform(post("/api/inspections")
                        .header("Authorization", "Bearer " + volunteerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated());

        // Immediate second submission for the same room by the same volunteer triggers 409 Conflict
        mockMvc.perform(post("/api/inspections")
                        .header("Authorization", "Bearer " + volunteerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message", containsString("Duplicate submission detected")));
    }

    @Test
    void testVolunteerMyInspectionsAndRecentEndpoints() throws Exception {
        mockMvc.perform(get("/api/volunteers/me/inspections")
                        .header("Authorization", "Bearer " + volunteerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mockMvc.perform(get("/api/inspections/recent")
                        .header("Authorization", "Bearer " + volunteerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testAdminCanViewAllInspections() throws Exception {
        mockMvc.perform(get("/api/inspections")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
