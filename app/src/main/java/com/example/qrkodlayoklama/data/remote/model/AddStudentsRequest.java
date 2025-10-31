package com.example.qrkodlayoklama.data.remote.model;

import java.util.List;

public class AddStudentsRequest {
    private List<Long> userNames;

    public AddStudentsRequest(List<Long> userNames) {
        this.userNames = userNames;
    }

    public List<Long> getUserNames() {
        return userNames;
    }

    public void setUserNames(List<Long> userNames) {
        this.userNames = userNames;
    }
}
