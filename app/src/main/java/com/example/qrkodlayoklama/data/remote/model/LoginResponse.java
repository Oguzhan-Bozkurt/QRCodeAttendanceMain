package com.example.qrkodlayoklama.data.remote.model;

public class LoginResponse {
    private String status;
    private String user;
    private String accessToken;
    private String ts;

    public String getStatus() { return status; }
    public String getUser() { return user; }
    public String getAccessToken() { return accessToken; }
    public String getTs() { return ts; }
}