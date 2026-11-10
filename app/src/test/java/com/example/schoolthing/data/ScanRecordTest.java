package com.example.schoolthing.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.schoolthing.data.local.ScanRecordEntity;
import com.example.schoolthing.data.model.AttendanceStatus;
import com.example.schoolthing.data.model.AttendanceType;
import com.example.schoolthing.data.model.UserRole;
import org.junit.Test;

/**
 * Unit test for scan record data models and user role metadata.
 */
public class ScanRecordTest {

    @Test
    public void testScanRecordEntityProperties() {
        long now = System.currentTimeMillis();
        ScanRecordEntity record = new ScanRecordEntity(
                "STU-2026-9812",
                "Alex Rivera",
                now,
                AttendanceType.CAMPUS_ENTRY.name(),
                AttendanceStatus.PRESENT.name(),
                false
        );

        assertEquals("STU-2026-9812", record.getStudentId());
        assertEquals("Alex Rivera", record.getStudentName());
        assertEquals(now, record.getTimestamp());
        assertEquals("CAMPUS_ENTRY", record.getAttendanceType());
        assertEquals("PRESENT", record.getAttendanceStatus());
        assertFalse(record.isSynced());

        record.setSynced(true);
        assertTrue(record.isSynced());
    }

    @Test
    public void testUserRoleDisplayNames() {
        assertEquals("Student Pass", UserRole.STUDENT.getDisplayName());
        assertEquals("Gate Guard", UserRole.GATE_GUARD.getDisplayName());
        assertEquals("Teacher", UserRole.TEACHER.getDisplayName());
        assertEquals("Administrator", UserRole.ADMINISTRATOR.getDisplayName());
    }
}
