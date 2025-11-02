package com.example.qrkodlayoklama.data.remote.model;

public class AttendanceStartRequest {
    private Integer minutes;
    private String description;

    public AttendanceStartRequest(Integer minutes, String description) {
        this.minutes = minutes;
        this.description = description;
    }

    public Integer getMinutes() { return minutes; }
    public String getDescription() { return description; }

}
