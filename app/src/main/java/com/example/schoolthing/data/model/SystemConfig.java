package com.example.schoolthing.data.model;

/**
 * Configurable clock-time thresholds for evaluating late and absent attendance cutoff times.
 */
public class SystemConfig {

    private String lateThresholdTime;
    private String absentThresholdTime;

    public SystemConfig() {
        this.lateThresholdTime = "08:15 AM";
        this.absentThresholdTime = "08:30 AM";
    }

    public SystemConfig(String lateThresholdTime, String absentThresholdTime) {
        this.lateThresholdTime = lateThresholdTime;
        this.absentThresholdTime = absentThresholdTime;
    }

    public String getLateThresholdTime() {
        return lateThresholdTime;
    }

    public void setLateThresholdTime(String lateThresholdTime) {
        this.lateThresholdTime = lateThresholdTime;
    }

    public String getAbsentThresholdTime() {
        return absentThresholdTime;
    }

    public void setAbsentThresholdTime(String absentThresholdTime) {
        this.absentThresholdTime = absentThresholdTime;
    }
}
