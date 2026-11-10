package com.example.schoolthing.data.model;

/**
 * Roles for access control in the QR Attendance system.
 */
public enum UserRole {
    STUDENT("Student Pass"),
    GATE_GUARD("Gate Guard"),
    TEACHER("Teacher"),
    ADMINISTRATOR("Administrator");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
