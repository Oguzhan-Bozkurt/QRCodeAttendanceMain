package com.example.qrkodlayoklama.data.remote.model;

public class ActiveSummaryDto {
    private Long sessionId;
    private String expiresAt;
    private long count;
    private boolean active;

    public Long getSessionId() { return sessionId; }
    public String getExpiresAt() { return expiresAt; }
    public long getCount() { return count; }
    public boolean isActive() { return active; }
}
