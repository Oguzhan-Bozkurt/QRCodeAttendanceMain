package com.example.qrkodlayoklama.data.remote.model;

public class MyAttendanceDto {
    private long sessionId;
    private long courseId;
    private String courseName;
    private String courseCode;
    private String checkedAt;
    private String description;
    private long totalSessions; // Bu alanı ekledim

    public long getSessionId() { return sessionId; }
    public long getCourseId() { return courseId; }
    public String getCourseName() { return courseName; }
    public String getCourseCode() { return courseCode; }
    public String getCheckedAt() { return checkedAt; }
    public String getDescription() { return description; }
    public long getTotalSessions() { return totalSessions; } // ve getter'ını ekledim
}
