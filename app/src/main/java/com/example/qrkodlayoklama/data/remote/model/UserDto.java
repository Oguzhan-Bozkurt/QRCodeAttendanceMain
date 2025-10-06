package com.example.qrkodlayoklama.data.remote.model;

public class UserDto {
    private Long id;
    private Long userName;
    private String name;
    private String surname;
    private Boolean userIsStudent;
    private String title;

    public Long getId() { return id; }
    public Long getUserName() { return userName; }
    public String getName() { return name; }
    public String getSurname() { return surname; }
    public Boolean getUserIsStudent() { return userIsStudent; }
    public String getTitle() { return title; }
}
