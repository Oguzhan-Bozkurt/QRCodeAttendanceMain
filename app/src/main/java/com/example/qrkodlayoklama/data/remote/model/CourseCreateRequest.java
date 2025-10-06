package com.example.qrkodlayoklama.data.remote.model;

public class CourseCreateRequest {
    private final String courseName;
    private final String courseCode;

    public CourseCreateRequest(String courseName, String courseCode) {
        this.courseName = courseName;
        this.courseCode = courseCode;
    }

    public String getCourseName() { return courseName; }
    public String getCourseCode() { return courseCode; }
}
