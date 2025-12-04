package com.example.qrkodlayoklama.data.remote.model;

public class AttendanceUpdateRequest {
    private String description;

    public AttendanceUpdateRequest(String description) {
        this.description = description;
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
