package com.myhometutor.model;

import org.json.JSONObject;

public class SessionManager {
    
    private static SessionManager instance;
    private JSONObject currentUser;
    private String userType; // "Student" or "Tutor"
    
    private SessionManager() {
    }
    
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    public void setCurrentUser(JSONObject userData, String userType) {
        this.currentUser = userData;
        this.userType = userType;
    }
    
    public JSONObject getCurrentUser() {
        return currentUser;
    }
    
    public String getUserType() {
        return userType;
    }
    
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    public void logout() {
        currentUser = null;
        userType = null;
    }
    
    public int getUserId() {
        if (currentUser != null && currentUser.has("id")) {
            return currentUser.getInt("id");
        }
        return -1;
    }
    
    public String getUsername() {
        if (currentUser != null && currentUser.has("username")) {
            return currentUser.getString("username");
        }
        return "";
    }
}
