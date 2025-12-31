package com.myhometutor.controller;

import org.json.JSONObject;

public class TuitionPostViewModel {
    private final int id;
    private final String studentName;
    private final String subject;
    private final String className;
    private final String status;
    private final JSONObject jsonData;

    public TuitionPostViewModel(int id, String studentName, String subject, String className, String status, JSONObject jsonData) {
        this.id = id;
        this.studentName = studentName;
        this.subject = subject;
        this.className = className;
        this.status = status;
        this.jsonData = jsonData;
    }

    public int getId() { return id; }
    public String getStudentName() { return studentName; }
    public String getSubject() { return subject; }
    public String getClassName() { return className; }
    public String getStatus() { return status; }
    public JSONObject getJsonData() { return jsonData; }
}