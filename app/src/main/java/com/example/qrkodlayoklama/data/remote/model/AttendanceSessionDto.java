package com.example.qrkodlayoklama.data.remote.model;

public class AttendanceSessionDto {
    private Long id;
    private Long courseId;
    private String secret;
    private String expiresAt;
    private boolean active;
    private String createdAt;

    public Long getId() { return id; }
    public Long getCourseId() { return courseId; }
    public String getSecret() { return secret; }
    public String getExpiresAt() { return expiresAt; }
    public boolean isActive() { return active; }
    public String getCreatedAt() { return createdAt; }
}
