package com.example.qrkodlayoklama.data.remote.model;

public class RegisterRequest {
    private Long userName;
    private String password;
    private String name;
    private String surName;
    private boolean userIsStudent;
    private String title;

    public RegisterRequest(Long userName, String password, String name, String surName, boolean userIsStudent, String title) {
        this.userName = userName;
        this.password = password;
        this.name = name;
        this.surName = surName;
        this.userIsStudent = userIsStudent;
        this.title = title;
    }

    public Long getUserName() { return userName; }
    public String getPassword() { return password; }
    public String getFullName() { return (name + " " + surName); }
    public boolean getRole() { return userIsStudent; }
    public String getTitle() {return title; }
}
