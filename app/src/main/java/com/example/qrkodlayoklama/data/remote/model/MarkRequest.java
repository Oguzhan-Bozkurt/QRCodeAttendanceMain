package com.example.qrkodlayoklama.data.remote.model;

public class MarkRequest {
    private String secret;

    public MarkRequest(String secret) { this.secret = secret; }

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
}
