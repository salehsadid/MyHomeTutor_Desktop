package com.myhometutor.controller;

import org.json.JSONObject;

public class ConnectionViewModel {
    private final int id;
    private final String tutorName;
    private final String studentName;
    private final String date;
    private final JSONObject jsonData;

    public ConnectionViewModel(int id, String tutorName, String studentName, String date, JSONObject jsonData) {
        this.id = id;
        this.tutorName = tutorName;
        this.studentName = studentName;
        this.date = date;
        this.jsonData = jsonData;
    }

    public int getId() { return id; }
    public String getTutorName() { return tutorName; }
    public String getStudentName() { return studentName; }
    public String getDate() { return date; }
    public JSONObject getJsonData() { return jsonData; }
}