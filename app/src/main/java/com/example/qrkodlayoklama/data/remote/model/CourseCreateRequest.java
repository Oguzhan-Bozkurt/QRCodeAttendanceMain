package com.example.qrkodlayoklama.data.remote.model;

import java.util.List;

public class CourseCreateRequest {
    private String courseName;
    private String courseCode;
    private List<Long> studentIds;

    public CourseCreateRequest(String courseName, String courseCode, List<Long> studentIds) {
        this.courseName = courseName;
        this.courseCode = courseCode;
        this.studentIds = studentIds;
    }

    public String getCourseName() { return courseName; }
    public String getCourseCode() { return courseCode; }
    public List<Long> getStudentIds() { return studentIds; }
}
