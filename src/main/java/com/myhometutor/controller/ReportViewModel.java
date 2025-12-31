package com.myhometutor.controller;

public class ReportViewModel {
    private int id;
    private int reporterId;
    private String reporterName;
    private String reporterType;
    private int reportedId;
    private String reportedName;
    private String reportedType;
    private String reason;
    private String status;
    private String createdAt;

    public ReportViewModel(int id, int reporterId, String reporterName, String reporterType, int reportedId, String reportedName, String reportedType, String reason, String status, String createdAt) {
        this.id = id;
        this.reporterId = reporterId;
        this.reporterName = reporterName;
        this.reporterType = reporterType;
        this.reportedId = reportedId;
        this.reportedName = reportedName;
        this.reportedType = reportedType;
        this.reason = reason;
        this.status = status;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public int getReporterId() { return reporterId; }
    public String getReporterName() { return reporterName; }
    public String getReporterType() { return reporterType; }
    public int getReportedId() { return reportedId; }
    public String getReportedName() { return reportedName; }
    public String getReportedType() { return reportedType; }
    public String getReason() { return reason; }
    public String getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }
}
