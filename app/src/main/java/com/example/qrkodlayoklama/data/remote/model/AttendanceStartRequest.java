package com.example.qrkodlayoklama.data.remote.model;

public class AttendanceStartRequest {
    private Integer minutes;
    public AttendanceStartRequest(Integer minutes) { this.minutes = minutes; }
    public Integer getMinutes() { return minutes; }
}
