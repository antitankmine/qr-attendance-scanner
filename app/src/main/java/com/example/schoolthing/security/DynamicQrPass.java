package com.example.schoolthing.security;

/**
 * Model representing a time-sensitive Dynamic QR Pass payload.
 */
public class DynamicQrPass {
    private final String studentId;
    private final String studentName;
    private final long timestamp;
    private final int validWindowSeconds;
    private final String signature;

    public DynamicQrPass(String studentId, String studentName, long timestamp, int validWindowSeconds, String signature) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.timestamp = timestamp;
        this.validWindowSeconds = validWindowSeconds;
        this.signature = signature;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getValidWindowSeconds() {
        return validWindowSeconds;
    }

    public String getSignature() {
        return signature;
    }
}
