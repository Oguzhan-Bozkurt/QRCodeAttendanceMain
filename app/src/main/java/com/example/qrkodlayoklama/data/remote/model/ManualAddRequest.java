package com.example.qrkodlayoklama.data.remote.model;

public class ManualAddRequest {
    private Long studentId;

    public ManualAddRequest(Long studentId) {
        this.studentId = studentId;
    }

    public Long getStudentId() {
        return studentId;
    }
}
