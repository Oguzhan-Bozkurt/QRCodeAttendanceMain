package com.example.qrkodlayoklama.data.remote.model;

public class LoginRequest {
    private Long userName;
    private String password;

    public LoginRequest(Long userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    public Long getUserName() { return userName; }
    public String getPassword() { return password; }
}
