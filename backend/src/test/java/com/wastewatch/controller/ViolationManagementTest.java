package com.wastewatch.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wastewatch.dto.auth.LoginRequestDto;
import com.wastewatch.dto.violation.ViolationResolveDto;
import com.wastewatch.dto.violation.ViolationStatusUpdateDto;
import com.wastewatch.entity.enums.ViolationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ViolationManagementTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        MvcResult adminResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequestDto("admin", "admin123"))))
                .andExpect(status().isOk())
                .andReturn();
        adminToken = objectMapper.readTree(adminResult.getResponse().getContentAsString()).get("token").asText();
    }

    @Test
    @DisplayName("GET /api/violations: Returns all existing violations without dropping records")
    void testGetAllViolations() throws Exception {
        mockMvc.perform(get("/api/violations")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))))
                .andExpect(jsonPath("$[0].id", notNullValue()))
                .andExpect(jsonPath("$[0].severity", notNullValue()))
                .andExpect(jsonPath("$[0].status", notNullValue()));
    }

    @Test
    @DisplayName("GET /api/violations?status=OPEN: Filters correctly by status")
    void testGetViolationsByStatus() throws Exception {
        mockMvc.perform(get("/api/violations?status=OPEN")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].status", is("OPEN")));
    }

    @Test
    @DisplayName("GET /api/violations?severity=CRITICAL: Filters correctly by severity")
    void testGetViolationsBySeverity() throws Exception {
        mockMvc.perform(get("/api/violations?severity=CRITICAL")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].severity", is("CRITICAL")));
    }

    @Test
    @DisplayName("GET /api/violations/{id}: Returns detail for existing violation")
    void testGetViolationById() throws Exception {
        mockMvc.perform(get("/api/violations/1")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.roomNumber", notNullValue()))
                .andExpect(jsonPath("$.buildingName", notNullValue()));
    }

    @Test
    @DisplayName("PUT /api/violations/{id}/status: Updates violation status")
    void testUpdateViolationStatus() throws Exception {
        ViolationStatusUpdateDto dto = new ViolationStatusUpdateDto();
        dto.setStatus(ViolationStatus.ACTION_REQUIRED);
        dto.setResolutionNotes("Action requested from facility supervisor");
        dto.setFineAmount(new BigDecimal("300.00"));

        mockMvc.perform(put("/api/violations/2/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.status", is("ACTION_REQUIRED")));
    }
}
