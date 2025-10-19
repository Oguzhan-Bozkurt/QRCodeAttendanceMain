package com.example.qrkodlayoklama.data.remote.model;

public class SessionHistoryDto {
    private Long id;
    private String createdAt;
    private String expiresAt;
    private boolean active;
    private long count;

    public Long getId() { return id; }
    public String getCreatedAt() { return createdAt; }
    public String getExpiresAt() { return expiresAt; }
    public boolean isActive() { return active; }
    public long getCount() { return count; }
}
