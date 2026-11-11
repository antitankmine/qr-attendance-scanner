package com.example.schoolthing.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * Unit test for Dynamic QR Code payload generation, signature verification, and time expiration.
 */
public class DynamicQrTest {

    @Test
    public void generateAndVerifyValidQrPass() {
        String studentId = "STU-1001";
        String studentName = "User One";

        String payload = QrCryptoUtils.generatePayload(studentId, studentName);
        assertNotNull(payload);

        DynamicQrPass pass = QrCryptoUtils.parseAndVerify(payload);
        assertNotNull("Dynamic QR Pass should be verified successfully", pass);
        assertEquals(studentId, pass.getStudentId());
        assertEquals(studentName, pass.getStudentName());
        assertEquals(30, pass.getValidWindowSeconds());
    }

    @Test
    public void rejectTamperedSignature() {
        String payload = QrCryptoUtils.generatePayload("STU-1001", "User One");
        // Tamper with payload student ID
        String tamperedPayload = payload.replace("STU-1001", "STU-FAKE-9999");

        DynamicQrPass pass = QrCryptoUtils.parseAndVerify(tamperedPayload);
        assertNull("Tampered payload signature should be rejected", pass);
    }

    @Test
    public void rejectInvalidPayloadFormat() {
        String invalidPayload = "random_invalid_string_without_delimiters";
        DynamicQrPass pass = QrCryptoUtils.parseAndVerify(invalidPayload);
        assertNull("Invalid payload format should return null", pass);
    }
}
