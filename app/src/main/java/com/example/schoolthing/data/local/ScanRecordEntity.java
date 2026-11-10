package com.example.schoolthing.data.local;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Room Entity for storing scan records locally.
 * Enables offline-first scan queuing.
 */
@Entity(tableName = "scan_records")
public class ScanRecordEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    private String studentId;

    private String studentName;

    private long timestamp;

    @NonNull
    private String attendanceType; // CAMPUS_ENTRY, CAMPUS_EXIT, CLASS_CHECKIN

    @NonNull
    private String attendanceStatus; // PRESENT, LATE, INVALID

    private boolean isSynced;

    public ScanRecordEntity(@NonNull String studentId, String studentName, long timestamp,
                            @NonNull String attendanceType, @NonNull String attendanceStatus, boolean isSynced) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.timestamp = timestamp;
        this.attendanceType = attendanceType;
        this.attendanceStatus = attendanceStatus;
        this.isSynced = isSynced;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(@NonNull String studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @NonNull
    public String getAttendanceType() {
        return attendanceType;
    }

    public void setAttendanceType(@NonNull String attendanceType) {
        this.attendanceType = attendanceType;
    }

    @NonNull
    public String getAttendanceStatus() {
        return attendanceStatus;
    }

    public void setAttendanceStatus(@NonNull String attendanceStatus) {
        this.attendanceStatus = attendanceStatus;
    }

    public boolean isSynced() {
        return isSynced;
    }

    public void setSynced(boolean synced) {
        isSynced = synced;
    }
}
