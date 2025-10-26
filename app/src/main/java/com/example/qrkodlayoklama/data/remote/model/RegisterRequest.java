package com.example.qrkodlayoklama.data.remote.model;

public class RegisterRequest {
    private String username;
    private String password;
    private String name;
    private String surname;
    private boolean role;
    private String title;

    public RegisterRequest(String username, String password, String name, String surname, boolean role, String title) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.surname = surname;
        this.role = role;
        this.title = title;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getFullName() { return (name + " " + surname); }
    public boolean getRole() { return role; }
    public String getTitle() {return title; }
}
