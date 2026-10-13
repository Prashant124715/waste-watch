package com.wastewatch.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wastewatch.dto.auth.LoginRequestDto;
import com.wastewatch.dto.volunteer.VolunteerStatusUpdateDto;
import com.wastewatch.entity.AppUser;
import com.wastewatch.entity.enums.UserRole;
import com.wastewatch.entity.enums.UserStatus;
import com.wastewatch.repository.AuditLogRepository;
import com.wastewatch.repository.UserRepository;
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
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ReportingAndGovernanceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    private String adminToken;
    private String volunteerToken;
    private Long volunteerUserId;
    private Long adminUserId;

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

        AppUser vol = userRepository.findByUsername("volunteer").orElseThrow();
        volunteerUserId = vol.getId();

        AppUser admin = userRepository.findByUsername("admin").orElseThrow();
        adminUserId = admin.getId();
    }

    // =========================================================================
    // 1. ADMIN USER MANAGEMENT & PRIVILEGE ESCALATION PREVENTION
    // =========================================================================

    @Test
    @DisplayName("Admin can view all volunteers with comprehensive activity statistics")
    void testAdminViewVolunteers() throws Exception {
        mockMvc.perform(get("/api/volunteers")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", is(not(empty()))))
                .andExpect(jsonPath("$.data[0].username", is("volunteer")))
                .andExpect(jsonPath("$.data[0].status", is("ACTIVE")))
                .andExpect(jsonPath("$.data[0].distinctRoomsInspected", notNullValue()))
                .andExpect(jsonPath("$.data[0].distinctBuildingsInspected", notNullValue()));
    }

    @Test
    @DisplayName("Admin can deactivate and reactivate volunteer with audit trail")
    void testAdminActivateDeactivateVolunteer() throws Exception {
        // 1. Deactivate
        VolunteerStatusUpdateDto deactivateDto = new VolunteerStatusUpdateDto(UserStatus.INACTIVE);
        mockMvc.perform(put("/api/volunteers/" + volunteerUserId + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deactivateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.status", is("INACTIVE")));

        AppUser updatedUser = userRepository.findById(volunteerUserId).orElseThrow();
        assertEquals(UserStatus.INACTIVE, updatedUser.getStatus());

        // 2. Reactivate
        VolunteerStatusUpdateDto activateDto = new VolunteerStatusUpdateDto(UserStatus.ACTIVE);
        mockMvc.perform(put("/api/volunteers/" + volunteerUserId + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(activateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.status", is("ACTIVE")));

        updatedUser = userRepository.findById(volunteerUserId).orElseThrow();
        assertEquals(UserStatus.ACTIVE, updatedUser.getStatus());
    }

    @Test
    @DisplayName("Privilege Escalation Prevention: Cannot modify admin status via volunteer endpoint")
    void testPrivilegeEscalationPrevented() throws Exception {
        VolunteerStatusUpdateDto dto = new VolunteerStatusUpdateDto(UserStatus.INACTIVE);
        mockMvc.perform(put("/api/volunteers/" + adminUserId + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Volunteer cannot change volunteer status (403 Forbidden)")
    void testVolunteerCannotChangeStatus() throws Exception {
        VolunteerStatusUpdateDto dto = new VolunteerStatusUpdateDto(UserStatus.INACTIVE);
        mockMvc.perform(put("/api/volunteers/" + volunteerUserId + "/status")
                        .header("Authorization", "Bearer " + volunteerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    // =========================================================================
    // 2. REPORTING APIS
    // =========================================================================

    @Test
    @DisplayName("Admin can query overall compliance report")
    void testOverviewReport() throws Exception {
        mockMvc.perform(get("/api/reports/overview")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalInspections", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.totalRooms", greaterThan(0)))
                .andExpect(jsonPath("$.coveragePercent", greaterThanOrEqualTo(0.0)))
                .andExpect(jsonPath("$.resolutionRate", greaterThanOrEqualTo(0.0)));
    }

    @Test
    @DisplayName("Admin can query area performance report")
    void testAreaPerformanceReport() throws Exception {
        mockMvc.perform(get("/api/reports/areas")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(not(empty()))))
                .andExpect(jsonPath("$[0].areaCode", notNullValue()))
                .andExpect(jsonPath("$[0].healthScore", greaterThanOrEqualTo(0.0)));
    }

    @Test
    @DisplayName("Admin can query building performance report")
    void testBuildingPerformanceReport() throws Exception {
        mockMvc.perform(get("/api/reports/buildings")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(not(empty()))))
                .andExpect(jsonPath("$[0].buildingCode", notNullValue()))
                .andExpect(jsonPath("$[0].totalRooms", greaterThan(0)));
    }

    @Test
    @DisplayName("Admin can query coverage report")
    void testCoverageReport() throws Exception {
        mockMvc.perform(get("/api/reports/coverage")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRooms", greaterThan(0)))
                .andExpect(jsonPath("$.areaCoverage", is(not(empty()))))
                .andExpect(jsonPath("$.buildingCoverage", is(not(empty()))));
    }

    @Test
    @DisplayName("Admin can query violations report")
    void testViolationReport() throws Exception {
        mockMvc.perform(get("/api/reports/violations")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalViolations", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.totalFinesImposed", notNullValue()))
                .andExpect(jsonPath("$.resolutionRate", greaterThanOrEqualTo(0.0)));
    }

    @Test
    @DisplayName("Admin can query volunteer activity report")
    void testVolunteerActivityReport() throws Exception {
        mockMvc.perform(get("/api/reports/volunteers")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(not(empty()))))
                .andExpect(jsonPath("$[0].username", is("volunteer")));
    }

    @Test
    @DisplayName("CSV Export delivers authentic downloadable CSV files with headers")
    void testCsvExports() throws Exception {
        // 1. Overview CSV
        MvcResult overviewResult = mockMvc.perform(get("/api/reports/export/csv?type=overview")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("wastewatch-overview-report.csv")))
                .andExpect(content().contentType("text/csv;charset=UTF-8"))
                .andReturn();
        String csvContent = overviewResult.getResponse().getContentAsString();
        assertTrue(csvContent.contains("Metric,Value"));
        assertTrue(csvContent.contains("Total Inspections"));

        // 2. Areas CSV
        MvcResult areasResult = mockMvc.perform(get("/api/reports/export/csv?type=areas")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("wastewatch-areas-report.csv")))
                .andReturn();
        assertTrue(areasResult.getResponse().getContentAsString().contains("Area Code,Area Name"));

        // 3. Buildings CSV
        MvcResult buildingsResult = mockMvc.perform(get("/api/reports/export/csv?type=buildings")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("wastewatch-buildings-report.csv")))
                .andReturn();
        assertTrue(buildingsResult.getResponse().getContentAsString().contains("Building Code,Building Name"));

        // 4. Violations CSV
        MvcResult violationsResult = mockMvc.perform(get("/api/reports/export/csv?type=violations")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("wastewatch-violations-report.csv")))
                .andReturn();
        assertTrue(violationsResult.getResponse().getContentAsString().contains("Violation ID,Room,Building"));
    }

    // =========================================================================
    // 3. AUDIT LOGS & GOVERNANCE
    // =========================================================================

    @Test
    @DisplayName("Admin can query audit logs and apply filters")
    void testAuditLogsQueryAndFilters() throws Exception {
        // Query all logs
        mockMvc.perform(get("/api/audit-logs")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(not(empty()))))
                .andExpect(jsonPath("$[0].action", notNullValue()))
                .andExpect(jsonPath("$[0].createdAt", notNullValue()));

        // Query by specific action: LOGIN
        mockMvc.perform(get("/api/audit-logs?action=LOGIN")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(not(empty()))))
                .andExpect(jsonPath("$[0].action", is("LOGIN")));

        // Verify no sensitive tokens or passwords stored in details
        MvcResult result = mockMvc.perform(get("/api/audit-logs")
                        .header("Authorization", "Bearer " + adminToken))
                .andReturn();
        String json = result.getResponse().getContentAsString();
        assertFalse(json.contains("admin123"));
        assertFalse(json.contains("volunteer123"));
        assertFalse(json.contains("passwordHash"));
    }

    // =========================================================================
    // 4. ROLE-BASED ACCESS CONTROL (RBAC) FOR GOVERNANCE & REPORTS
    // =========================================================================

    @Test
    @DisplayName("Volunteer cannot access Reports APIs (403 Forbidden)")
    void testVolunteerCannotAccessReports() throws Exception {
        mockMvc.perform(get("/api/reports/overview")
                        .header("Authorization", "Bearer " + volunteerToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/reports/export/csv")
                        .header("Authorization", "Bearer " + volunteerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Volunteer cannot access Audit Logs API (403 Forbidden)")
    void testVolunteerCannotAccessAuditLogs() throws Exception {
        mockMvc.perform(get("/api/audit-logs")
                        .header("Authorization", "Bearer " + volunteerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Unauthenticated request to reports/audit is rejected (401 Unauthorized)")
    void testUnauthenticatedAccessRejected() throws Exception {
        mockMvc.perform(get("/api/reports/overview"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/audit-logs"))
                .andExpect(status().isUnauthorized());
    }
}
