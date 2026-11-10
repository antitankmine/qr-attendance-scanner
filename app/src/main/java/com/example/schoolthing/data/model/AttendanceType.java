package com.example.schoolthing.data.model;

/**
 * Type of scan activity.
 */
public enum AttendanceType {
    CAMPUS_ENTRY("Campus Entry"),
    CAMPUS_EXIT("Campus Exit"),
    CLASS_CHECKIN("Classroom Check-In");

    private final String description;

    AttendanceType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
