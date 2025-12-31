package com.myhometutor.controller;

import com.myhometutor.database.DatabaseManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class ReportDialogController {

    @FXML private TextArea reasonArea;
    @FXML private Button cancelButton;

    private int reporterId;
    private String reporterType;
    private int reportedId;
    private String reportedType;

    public void setReportDetails(int reporterId, String reporterType, int reportedId, String reportedType) {
        this.reporterId = reporterId;
        this.reporterType = reporterType;
        this.reportedId = reportedId;
        this.reportedType = reportedType;
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    @FXML
    private void handleSubmit() {
        String reason = reasonArea.getText().trim();
        if (reason.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter a reason for the report.");
            return;
        }

        if (DatabaseManager.getInstance().createReport(reporterId, reporterType, reportedId, reportedType, reason)) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Report submitted successfully.");
            closeDialog();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to submit report.");
        }
    }

    private void closeDialog() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
