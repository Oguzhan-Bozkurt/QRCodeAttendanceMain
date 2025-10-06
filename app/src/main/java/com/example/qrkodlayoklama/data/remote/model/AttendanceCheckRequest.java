package com.example.qrkodlayoklama.data.remote.model;

public class AttendanceCheckRequest {
    private final String secret;

    public AttendanceCheckRequest(String secret) {
        this.secret = secret;
    }

    public String getSecret() {
        return secret;
    }
}