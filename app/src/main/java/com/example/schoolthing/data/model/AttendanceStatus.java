package com.example.schoolthing.data.model;

/**
 * Status of an attendance scan evaluation.
 */
public enum AttendanceStatus {
    PRESENT("On Time", "#2E7D32"), // Green
    LATE("Late Arrival", "#F57F17"),    // Yellow / Amber
    INVALID("Invalid Code", "#C62828"), // Red
    UNEXCUSED("Unexcused", "#C62828"); // Red

    private final String label;
    private final String hexColor;

    AttendanceStatus(String label, String hexColor) {
        this.label = label;
        this.hexColor = hexColor;
    }

    public String getLabel() {
        return label;
    }

    public String getHexColor() {
        return hexColor;
    }
}
