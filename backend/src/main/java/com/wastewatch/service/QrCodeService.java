package com.wastewatch.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

@Service
public class QrCodeService {

    private static final Logger log = LoggerFactory.getLogger(QrCodeService.class);
    private static final int DEFAULT_SIZE = 300;

    /**
     * Generate raw PNG image bytes for the given QR code payload text.
     *
     * @param text   payload string embedded in the QR code (e.g. room QR token)
     * @param width  image width in pixels
     * @param height image height in pixels
     * @return byte array of the generated PNG image
     */
    public byte[] generateQrCodePng(String text, int width, int height) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("QR code payload text cannot be null or empty");
        }
        int w = width > 0 ? width : DEFAULT_SIZE;
        int h = height > 0 ? height : DEFAULT_SIZE;

        try {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.MARGIN, 2);

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, w, h, hints);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            return outputStream.toByteArray();
        } catch (WriterException | IOException e) {
            log.error("Failed to generate QR code image for payload: {}", text, e);
            throw new RuntimeException("Could not generate QR code image", e);
        }
    }

    /**
     * Generate a Base64 data URI (data:image/png;base64,...) for the given QR code text.
     *
     * @param text   payload string
     * @param width  image width in pixels
     * @param height image height in pixels
     * @return Base64 data URI string suitable for HTML <img> src
     */
    public String generateQrCodeDataUrl(String text, int width, int height) {
        byte[] imageBytes = generateQrCodePng(text, width, height);
        String base64 = Base64.getEncoder().encodeToString(imageBytes);
        return "data:image/png;base64," + base64;
    }

    /**
     * Generate a standardized, human-readable, and stable QR identifier
     * based on building code and room number.
     *
     * Example: Building 'BLD-NW-101', Room 'Flat 101' -> 'QR-BLD-NW-101-101'
     *
     * @param buildingCode system code of the building
     * @param roomNumber   room number or unit identifier
     * @return formatted QR code token
     */
    public String formatRoomQrIdentifier(String buildingCode, String roomNumber) {
        String cleanBuilding = buildingCode != null ? buildingCode.replaceAll("[^a-zA-Z0-9]", "-").toUpperCase() : "BLD";
        String cleanRoom = roomNumber != null ? roomNumber.replaceAll("[^a-zA-Z0-9]", "-").toUpperCase() : "RM";
        cleanBuilding = cleanBuilding.replaceAll("-+", "-").replaceAll("^-|-$", "");
        cleanRoom = cleanRoom.replaceAll("-+", "-").replaceAll("^-|-$", "");
        return String.format("QR-%s-%s", cleanBuilding, cleanRoom);
    }

    /**
     * Generate a randomized unique fallback QR token.
     *
     * @return unique token string (e.g. QR-A1B2C3D4)
     */
    public String generateRandomQrToken() {
        return "QR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
