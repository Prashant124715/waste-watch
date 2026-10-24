package com.wastewatch.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wastewatch.dto.auth.LoginRequestDto;
import com.wastewatch.dto.auth.RegisterRequestDto;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class VolunteerRegistrationWorkflowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private com.wastewatch.security.JwtTokenProvider tokenProvider;

    private String adminToken;
    private String volunteerToken;

    @BeforeEach
    void setUp() throws Exception {
        // Log in as Admin
        LoginRequestDto adminLogin = new LoginRequestDto("admin", "admin123");
        MvcResult adminRes = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminLogin)))
                .andExpect(status().isOk())
                .andReturn();
        adminToken = objectMapper.readTree(adminRes.getResponse().getContentAsString()).get("token").asText();

        // Log in as existing Active Volunteer
        LoginRequestDto volLogin = new LoginRequestDto("volunteer", "volunteer123");
        MvcResult volRes = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(volLogin)))
                .andExpect(status().isOk())
                .andReturn();
        volunteerToken = objectMapper.readTree(volRes.getResponse().getContentAsString()).get("token").asText();
    }

    @Test
    @DisplayName("Public volunteer registration succeeds and sets PENDING status and VOLUNTEER role")
    void testPublicRegistrationSuccess() throws Exception {
        RegisterRequestDto request = new RegisterRequestDto();
        request.setFullName("Aarav Sharma");
        request.setUsername("aarav_sharma");
        request.setEmail("aarav.sharma@example.in");
        request.setPhoneNumber("+91 98201 12345");
        request.setPassword("Secret@123");
        request.setConfirmPassword("Secret@123");
        request.setCity("Mumbai");
        request.setState("Maharashtra");
        request.setAddress("Flat 402, Shree Sai Residency, Andheri West");
        request.setReason("Passionate about wet/dry waste segregation in housing societies");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message", containsString("pending administrator verification")))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.role").value("VOLUNTEER"))
                .andExpect(jsonPath("$.data.username").value("aarav_sharma"));

        // Verify entity in repository
        AppUser savedUser = userRepository.findByUsername("aarav_sharma").orElse(null);
        assertNotNull(savedUser);
        assertEquals(UserRole.VOLUNTEER, savedUser.getRole());
        assertEquals(UserStatus.PENDING, savedUser.getStatus());
        assertEquals("Mumbai", savedUser.getCity());
        assertEquals("Maharashtra", savedUser.getState());
        assertNotEquals("Secret@123", savedUser.getPasswordHash(), "Password must be BCrypt hashed, never plaintext");
    }

    @Test
    @DisplayName("Registration rejects invalid Indian phone number formats")
    void testInvalidIndianPhoneNumberFormat() throws Exception {
        RegisterRequestDto request = new RegisterRequestDto();
        request.setFullName("Invalid Phone Applicant");
        request.setUsername("invalid_phone_user");
        request.setEmail("invalid.phone@example.in");
        request.setPhoneNumber("12345"); // Invalid phone format
        request.setPassword("Secret@123");
        request.setConfirmPassword("Secret@123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Registration rejects duplicate username and duplicate email")
    void testDuplicateRegistrationRejection() throws Exception {
        // admin username already exists
        RegisterRequestDto duplicateUser = new RegisterRequestDto();
        duplicateUser.setFullName("Admin Impersonator");
        duplicateUser.setUsername("admin");
        duplicateUser.setEmail("impostor@example.in");
        duplicateUser.setPhoneNumber("+919811122233");
        duplicateUser.setPassword("Secret@123");
        duplicateUser.setConfirmPassword("Secret@123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateUser)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("already taken")));

        // admin email already exists
        RegisterRequestDto duplicateEmail = new RegisterRequestDto();
        duplicateEmail.setFullName("Duplicate Email");
        duplicateEmail.setUsername("unique_username_99");
        duplicateEmail.setEmail("admin@wastewatch.org");
        duplicateEmail.setPhoneNumber("+919811122234");
        duplicateEmail.setPassword("Secret@123");
        duplicateEmail.setConfirmPassword("Secret@123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateEmail)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("already exists")));
    }

    @Test
    @DisplayName("Registration rejects mismatched password confirmation")
    void testPasswordConfirmationMismatch() throws Exception {
        RegisterRequestDto mismatch = new RegisterRequestDto();
        mismatch.setFullName("Mismatch User");
        mismatch.setUsername("mismatch_user");
        mismatch.setEmail("mismatch@example.in");
        mismatch.setPhoneNumber("+919876543210");
        mismatch.setPassword("Secret@123");
        mismatch.setConfirmPassword("DifferentSecret@123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mismatch)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Passwords do not match")));
    }

    @Test
    @DisplayName("Pending volunteer cannot login until approved")
    void testPendingVolunteerCannotLogin() throws Exception {
        RegisterRequestDto request = new RegisterRequestDto();
        request.setFullName("Pooja Nair");
        request.setUsername("pooja_nair");
        request.setEmail("pooja.nair@example.in");
        request.setPhoneNumber("+91 97654 32109");
        request.setPassword("Pooja@123");
        request.setConfirmPassword("Pooja@123");
        request.setCity("Pune");
        request.setState("Maharashtra");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Attempt login while PENDING
        LoginRequestDto login = new LoginRequestDto("pooja_nair", "Pooja@123");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("pending administrator verification")));
    }

    @Test
    @DisplayName("Admin can filter pending volunteers, approve application, and volunteer can then log in")
    void testAdminApprovalWorkflow() throws Exception {
        // Register applicant
        RegisterRequestDto request = new RegisterRequestDto();
        request.setFullName("Karan Mehta");
        request.setUsername("karan_mehta");
        request.setEmail("karan.mehta@example.in");
        request.setPhoneNumber("+91 98333 44556");
        request.setPassword("Karan@1234");
        request.setConfirmPassword("Karan@1234");
        request.setCity("Bengaluru");
        request.setState("Karnataka");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        AppUser applicant = userRepository.findByUsername("karan_mehta").orElseThrow();

        // Admin filters volunteers by PENDING status
        mockMvc.perform(get("/api/volunteers?status=PENDING")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data[*].username", hasItem("karan_mehta")));

        // Admin approves the volunteer
        mockMvc.perform(post("/api/volunteers/" + applicant.getId() + "/approve")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        // Now volunteer can log in successfully
        LoginRequestDto login = new LoginRequestDto("karan_mehta", "Karan@1234");
        MvcResult res = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.role").value("VOLUNTEER"))
                .andReturn();

        String newVolToken = objectMapper.readTree(res.getResponse().getContentAsString()).get("token").asText();
        assertNotNull(newVolToken);

        // Volunteer can access /api/auth/me
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + newVolToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("karan_mehta"))
                .andExpect(jsonPath("$.role").value("VOLUNTEER"))
                .andExpect(jsonPath("$.city").value("Bengaluru"));
    }

    @Test
    @DisplayName("Admin can reject volunteer application and rejected user cannot login")
    void testAdminRejectionWorkflow() throws Exception {
        RegisterRequestDto request = new RegisterRequestDto();
        request.setFullName("Suresh Patil");
        request.setUsername("suresh_patil");
        request.setEmail("suresh.patil@example.in");
        request.setPhoneNumber("+91 91234 56789");
        request.setPassword("Suresh@1234");
        request.setConfirmPassword("Suresh@1234");
        request.setCity("Nashik");
        request.setState("Maharashtra");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        AppUser applicant = userRepository.findByUsername("suresh_patil").orElseThrow();

        // Admin rejects volunteer
        mockMvc.perform(post("/api/volunteers/" + applicant.getId() + "/reject")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REJECTED"));

        // Rejected volunteer cannot log in
        LoginRequestDto login = new LoginRequestDto("suresh_patil", "Suresh@1234");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("not approved")));
    }

    @Test
    @DisplayName("Unauthorized users and volunteers cannot approve applications")
    void testUnauthorizedApprovalFails() throws Exception {
        // Existing active volunteer attempts to approve someone
        mockMvc.perform(post("/api/volunteers/2/approve")
                        .header("Authorization", "Bearer " + volunteerToken))
                .andExpect(status().isForbidden());

        // Anonymous user attempts to approve someone
        mockMvc.perform(post("/api/volunteers/2/approve"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Pending volunteer cannot submit inspection reports")
    void testPendingVolunteerCannotSubmitInspection() throws Exception {
        AppUser pendingUser = new AppUser("pending_insp_tester", "pending_insp@tester.org", "Pending Tester", "passwordHash", UserRole.VOLUNTEER);
        pendingUser.setStatus(UserStatus.PENDING);
        pendingUser = userRepository.save(pendingUser);

        org.springframework.security.core.Authentication auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                com.wastewatch.security.UserPrincipal.create(pendingUser), null,
                java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_VOLUNTEER")));
        String pendingToken = tokenProvider.generateToken(auth);

        com.wastewatch.dto.inspection.InspectionRequestDto req = new com.wastewatch.dto.inspection.InspectionRequestDto();
        req.setBuildingId(1L);
        req.setWetWasteSeparated(true);
        req.setDryWasteSeparated(true);
        req.setSanitaryWasteSeparated(true);
        req.setCorrectBinsUsed(true);
        req.setBinsLabelled(true);
        req.setNoMixedWaste(true);

        mockMvc.perform(post("/api/inspections")
                        .header("Authorization", "Bearer " + pendingToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }
}
