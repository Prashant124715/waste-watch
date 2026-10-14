package com.wastewatch.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QrCodeServiceTest {

    private QrCodeService qrCodeService;

    @BeforeEach
    void setUp() {
        qrCodeService = new QrCodeService();
    }

    @Test
    @DisplayName("generateQrCodePng: produces valid non-empty PNG byte array with PNG magic bytes")
    void testGenerateQrCodePng() {
        byte[] pngBytes = qrCodeService.generateQrCodePng("QR-GVH-101", 300, 300);

        assertNotNull(pngBytes);
        assertTrue(pngBytes.length > 50, "PNG image bytes should be non-trivial size");

        // Verify PNG magic numbers: 0x89, 'P', 'N', 'G'
        assertEquals((byte) 0x89, pngBytes[0]);
        assertEquals((byte) 'P', pngBytes[1]);
        assertEquals((byte) 'N', pngBytes[2]);
        assertEquals((byte) 'G', pngBytes[3]);
    }

    @Test
    @DisplayName("generateQrCodeDataUrl: produces valid data:image/png;base64 URI")
    void testGenerateQrCodeDataUrl() {
        String dataUrl = qrCodeService.generateQrCodeDataUrl("QR-TEST-202", 200, 200);

        assertNotNull(dataUrl);
        assertTrue(dataUrl.startsWith("data:image/png;base64,"), "Data URL must start with standard prefix");
        assertTrue(dataUrl.length() > 50, "Data URL should contain base64 payload");
    }

    @Test
    @DisplayName("generateQrCodePng: throws IllegalArgumentException when text is null or blank")
    void testGenerateQrCodePngNullOrBlankThrows() {
        assertThrows(IllegalArgumentException.class, () -> qrCodeService.generateQrCodePng(null, 200, 200));
        assertThrows(IllegalArgumentException.class, () -> qrCodeService.generateQrCodePng("   ", 200, 200));
    }

    @Test
    @DisplayName("formatRoomQrIdentifier: sanitizes building and room number into stable uppercase token")
    void testFormatRoomQrIdentifier() {
        String token = qrCodeService.formatRoomQrIdentifier("BLD-NW-101", "Flat 101");
        assertEquals("QR-BLD-NW-101-FLAT-101", token);

        String token2 = qrCodeService.formatRoomQrIdentifier("Tower B", "Unit #42");
        assertEquals("QR-TOWER-B-UNIT-42", token2);
    }

    @Test
    @DisplayName("generateRandomQrToken: produces non-null QR token with QR- prefix")
    void testGenerateRandomQrToken() {
        String token = qrCodeService.generateRandomQrToken();
        assertNotNull(token);
        assertTrue(token.startsWith("QR-"));
        assertEquals(11, token.length()); // "QR-" + 8 chars
    }
}
