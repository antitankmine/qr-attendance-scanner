package com.example.schoolthing.security;

import java.nio.charset.StandardCharsets;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Utility for generating and verifying dynamic time-sensitive QR code payloads.
 * Prevents screenshot/replay attacks by enforcing short expiration windows and HMAC signatures.
 */
public class QrCryptoUtils {

    private static final String DEFAULT_SECRET = "SchoolThingSecretKey2026_SecureKey";
    private static final int VALID_WINDOW_SECONDS = 30;

    public static String generatePayload(String studentId, String studentName) {
        long timestamp = System.currentTimeMillis();
        String rawData = studentId + "|" + studentName + "|" + timestamp + "|" + VALID_WINDOW_SECONDS;
        String signature = computeHmac(rawData, DEFAULT_SECRET);
        return rawData + "|" + signature;
    }

    public static DynamicQrPass parseAndVerify(String qrContent) {
        if (qrContent == null || qrContent.trim().isEmpty()) {
            return null;
        }

        String[] parts = qrContent.split("\\|");
        if (parts.length != 5) {
            return null; // Invalid payload format
        }

        String studentId = parts[0];
        String studentName = parts[1];
        long timestamp;
        int validSeconds;

        try {
            timestamp = Long.parseLong(parts[2]);
            validSeconds = Integer.parseInt(parts[3]);
        } catch (NumberFormatException e) {
            return null;
        }

        String providedSignature = parts[4];
        String rawData = studentId + "|" + studentName + "|" + timestamp + "|" + validSeconds;
        String expectedSignature = computeHmac(rawData, DEFAULT_SECRET);

        // Verify cryptographic signature
        if (!expectedSignature.equals(providedSignature)) {
            return null; // Signature mismatch / tampered QR code
        }

        // Verify time window (expiration check)
        long currentTime = System.currentTimeMillis();
        long ageInMillis = currentTime - timestamp;
        long maxAllowedAge = (validSeconds + 15) * 1000L; // 15-second grace buffer for clock skew

        if (ageInMillis < -5000 || ageInMillis > maxAllowedAge) {
            return null; // Code has expired or clock skewed significantly
        }

        return new DynamicQrPass(studentId, studentName, timestamp, validSeconds, providedSignature);
    }

    private static String computeHmac(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(rawHmac);
        } catch (Exception e) {
            return "";
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
