package com.example.qrkodlayoklama.data.remote.model;

public class AttendanceRecordDto {
    private Long id;
    private Long studentId;
    private Long userName;
    private String name;
    private String surname;
    private String checkedAt;

    public Long getId() { return id; }
    public Long getStudentId() { return studentId; }
    public Long getUserName() { return userName; }

    public String getName() { return name; }
    public String getSurname() { return surname; }
    public String getCheckedAt() { return checkedAt; }
}
