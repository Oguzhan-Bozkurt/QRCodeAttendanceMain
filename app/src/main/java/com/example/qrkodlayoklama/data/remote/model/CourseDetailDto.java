package com.example.qrkodlayoklama.data.remote.model;

import java.util.List;

public class CourseDetailDto {
    private Long id;
    private String courseName;
    private String courseCode;
    private List<UserDto> students;

    public Long getId() { return id; }
    public String getCourseName() { return courseName; }
    public String getCourseCode() { return courseCode; }
    public List<UserDto> getStudents() { return students; }
}
