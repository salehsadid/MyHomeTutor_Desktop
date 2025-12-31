package com.myhometutor.controller;

import org.json.JSONObject;

public class UserViewModel {
    private final int id;
    private final String username;
    private final String type;
    private final String status;
    private final JSONObject jsonData;

    public UserViewModel(int id, String username, String type, String status, JSONObject jsonData) {
        this.id = id;
        this.username = username;
        this.type = type;
        this.status = status;
        this.jsonData = jsonData;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getType() { return type; }
    public String getStatus() { return status; }
    public JSONObject getJsonData() { return jsonData; }
}
