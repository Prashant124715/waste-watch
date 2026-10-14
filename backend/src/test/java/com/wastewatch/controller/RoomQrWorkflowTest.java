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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class RoomQrWorkflowTest {

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
    @DisplayName("QR Lookup: Volunteer successfully identifies room by valid QR code token")
    void testVolunteerCanLookupRoomByQrCode() throws Exception {
        // Seeded room: "QR-GVH-101"
        mockMvc.perform(get("/api/rooms/qr/QR-GVH-101")
                        .header("Authorization", "Bearer " + volunteerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomNumber", is("Flat 101")))
                .andExpect(jsonPath("$.floorNumber", is(1)))
                .andExpect(jsonPath("$.floor", is(1)))
                .andExpect(jsonPath("$.roomId", notNullValue()))
                .andExpect(jsonPath("$.building.name", is("Green Valley Heights")))
                .andExpect(jsonPath("$.building.id", notNullValue()))
                .andExpect(jsonPath("$.area.name", is("North Ward - Sector 14")))
                .andExpect(jsonPath("$.area.id", notNullValue()))
                .andExpect(jsonPath("$.qrCodeToken", is("QR-GVH-101")))
                .andExpect(jsonPath("$.qrCodeDataUrl", startsWith("data:image/png;base64,")));
    }

    @Test
    @DisplayName("QR Lookup: Admin successfully identifies room by valid QR code token")
    void testAdminCanLookupRoomByQrCode() throws Exception {
        mockMvc.perform(get("/api/rooms/qr/QR-GVH-102")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomNumber", is("Flat 102")))
                .andExpect(jsonPath("$.building.name", is("Green Valley Heights")))
                .andExpect(jsonPath("$.qrCodeToken", is("QR-GVH-102")));
    }

    @Test
    @DisplayName("QR Lookup: Returns 404 when QR code does not exist")
    void testQrLookupNonExistentReturns404() throws Exception {
        mockMvc.perform(get("/api/rooms/qr/QR-NONEXISTENT-999")
                        .header("Authorization", "Bearer " + volunteerToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", containsString("Room not found for the provided QR code")));
    }

    @Test
    @DisplayName("QR Lookup: Unauthenticated request returns 401 Unauthorized")
    void testQrLookupUnauthenticatedReturns401() throws Exception {
        mockMvc.perform(get("/api/rooms/qr/QR-GVH-101"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("QR Lookup Security: Response does NOT leak password, passwordHash or internal credentials")
    void testQrLookupDoesNotExposeSensitiveInformation() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/rooms/qr/QR-GVH-101")
                        .header("Authorization", "Bearer " + volunteerToken))
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertFalse(body.contains("password"));
        assertFalse(body.contains("passwordHash"));
        assertFalse(body.contains("secret"));
    }

    @Test
    @DisplayName("QR Image: Returns valid PNG image bytes with Content-Type image/png by room ID")
    void testGetRoomQrImageById() throws Exception {
        // Room 1 seeded
        MvcResult result = mockMvc.perform(get("/api/rooms/1/qr-image")
                        .header("Authorization", "Bearer " + volunteerToken))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", is("image/png")))
                .andReturn();

        byte[] bytes = result.getResponse().getContentAsByteArray();
        assertTrue(bytes.length > 50);
        // Verify PNG magic numbers
        assertEquals((byte) 0x89, bytes[0]);
        assertEquals((byte) 'P', bytes[1]);
        assertEquals((byte) 'N', bytes[2]);
        assertEquals((byte) 'G', bytes[3]);
    }

    @Test
    @DisplayName("QR Image: Returns valid PNG image bytes by QR token")
    void testGetRoomQrImageByToken() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/rooms/qr/QR-GVH-101/image")
                        .header("Authorization", "Bearer " + volunteerToken))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", is("image/png")))
                .andReturn();

        byte[] bytes = result.getResponse().getContentAsByteArray();
        assertTrue(bytes.length > 50);
        assertEquals((byte) 0x89, bytes[0]);
    }

    @Test
    @DisplayName("QR Image: Returns 404 when requesting image for non-existent room ID")
    void testGetRoomQrImageNonExistentRoomReturns404() throws Exception {
        mockMvc.perform(get("/api/rooms/999999/qr-image")
                        .header("Authorization", "Bearer " + volunteerToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Admin QR Management: Admin can regenerate QR code token; old token expires; new token works")
    void testAdminCanRegenerateRoomQrCode() throws Exception {
        // 1. Fetch current room
        MvcResult initialResult = mockMvc.perform(get("/api/rooms/3")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();
        String oldQrToken = objectMapper.readTree(initialResult.getResponse().getContentAsString()).get("qrCodeToken").asText();

        // 2. Admin regenerates QR token
        MvcResult regenResult = mockMvc.perform(post("/api/rooms/3/regenerate-qr")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();

        String newQrToken = objectMapper.readTree(regenResult.getResponse().getContentAsString()).get("qrCodeToken").asText();
        assertNotEquals(oldQrToken, newQrToken, "Regenerated token must differ from previous token");
        assertTrue(newQrToken.startsWith("QR-"));

        // 3. New token resolves successfully
        mockMvc.perform(get("/api/rooms/qr/" + newQrToken)
                        .header("Authorization", "Bearer " + volunteerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(3)));

        // 4. Old token now returns 404 Not Found
        mockMvc.perform(get("/api/rooms/qr/" + oldQrToken)
                        .header("Authorization", "Bearer " + volunteerToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Admin QR Management: Volunteer is forbidden from regenerating QR code (403 Forbidden)")
    void testVolunteerCannotRegenerateQrCode() throws Exception {
        mockMvc.perform(post("/api/rooms/1/regenerate-qr")
                        .header("Authorization", "Bearer " + volunteerToken))
                .andExpect(status().isForbidden());
    }
}
