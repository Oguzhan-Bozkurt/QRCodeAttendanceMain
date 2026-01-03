package com.example.qrkodlayoklama.data.remote.model;

public class MyAttendanceSummaryDto {
    private long courseId;
    private String courseName;
    private String courseCode;
    private int totalSessions;
    private int attendedSessions;

    public long getCourseId() {
        return courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public int getTotalSessions() {
        return totalSessions;
    }

    public int getAttendedSessions() {
        return attendedSessions;
    }
}
