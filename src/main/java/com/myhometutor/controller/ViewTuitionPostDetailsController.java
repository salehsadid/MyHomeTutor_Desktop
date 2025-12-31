package com.myhometutor.controller;

import com.myhometutor.database.DatabaseManager;
import com.myhometutor.util.EmailService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.util.Optional;

public class ViewTuitionPostDetailsController {

    @FXML private Label studentNameLabel;
    @FXML private Label subjectLabel;
    @FXML private Label classLabel;
    @FXML private Label statusLabel;
    @FXML private Label salaryLabel;
    @FXML private Label daysLabel;
    @FXML private Label locationLabel;
    @FXML private Label descriptionLabel;
    
    @FXML private Button closeButton;
    @FXML private VBox adminActionBox;

    private int postId;
    private int studentId;
    private String studentEmail;
    private String studentName;

    public void setPostData(JSONObject postData) {
        if (postData != null) {
            this.postId = postData.getInt("id");
            this.studentId = postData.getInt("student_id");
            this.studentName = postData.optString("student_name", "Unknown");
            this.studentEmail = postData.optString("student_email", "");
            
            studentNameLabel.setText(studentName);
            subjectLabel.setText(postData.optString("subject", "N/A"));
            classLabel.setText(postData.optString("class", "N/A"));
            statusLabel.setText(postData.optString("status", "pending"));
            
            // Additional details from JSON data blob
            salaryLabel.setText(postData.optString("salary", "Negotiable"));
            daysLabel.setText(postData.optString("days", "N/A"));
            
            // Construct location string
            String address = postData.optString("address", "");
            if ("Online".equalsIgnoreCase(address)) {
                locationLabel.setText("Online");
            } else {
                StringBuilder loc = new StringBuilder();
                if (!address.isEmpty()) loc.append(address);
                
                String area = postData.optString("area", "");
                if (!area.isEmpty()) {
                    if (loc.length() > 0) loc.append(", ");
                    loc.append(area);
                }
                
                String thana = postData.optString("thana", "");
                if (!thana.isEmpty()) {
                    if (loc.length() > 0) loc.append(", ");
                    loc.append(thana);
                }
                
                String district = postData.optString("district", "");
                if (!district.isEmpty()) {
                    if (loc.length() > 0) loc.append(", ");
                    loc.append(district);
                }
                
                if (loc.length() == 0) locationLabel.setText("N/A");
                else locationLabel.setText(loc.toString());
            }
            
            descriptionLabel.setText(postData.optString("description", "No description provided."));
        }
    }

    public void setAdminMode(boolean isAdmin) {
        if (isAdmin) {
            adminActionBox.setVisible(true);
            adminActionBox.setManaged(true);
        } else {
            adminActionBox.setVisible(false);
            adminActionBox.setManaged(false);
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleApprove() {
        if (DatabaseManager.getInstance().updateTuitionPostStatus(postId, "active")) {
            statusLabel.setText("active");
            sendApprovalEmail();
            handleClose();
        }
    }

    @FXML
    private void handleReject() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Reject Post");
        dialog.setHeaderText("Reason for Rejection");
        dialog.setContentText("Please enter the reason:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(reason -> {
            if (DatabaseManager.getInstance().updateTuitionPostStatus(postId, "rejected")) {
                statusLabel.setText("rejected");
                sendRejectionEmail(reason);
                handleClose();
            }
        });
    }

    private void sendApprovalEmail() {
        if (studentEmail != null && !studentEmail.isEmpty()) {
            String subject = "Tuition Post Approved - MyHomeTutor";
            String body = "<html><body style='font-family: Arial, sans-serif;'>" +
                          "<p>Dear " + studentName + ",</p>" +
                          "<p>Your tuition post for <strong>" + subjectLabel.getText() + "</strong> has been approved by the admin.</p>" +
                          "<p>It is now visible to tutors.</p>" +
                          "<br><p>Best regards,<br>MyHomeTutor Team</p>" +
                          "</body></html>";
            
            new Thread(() -> {
                EmailService.sendEmail(studentEmail, subject, body);
            }).start();
        }
    }

    private void sendRejectionEmail(String reason) {
        if (studentEmail != null && !studentEmail.isEmpty()) {
            String subject = "Tuition Post Rejected - MyHomeTutor";
            String body = "<html><body style='font-family: Arial, sans-serif;'>" +
                          "<p>Dear " + studentName + ",</p>" +
                          "<p>We regret to inform you that your tuition post for <strong>" + subjectLabel.getText() + "</strong> has been rejected by the admin.</p>" +
                          "<p><strong>Reason:</strong> " + reason + "</p>" +
                          "<p>Please review our guidelines and post again.</p>" +
                          "<br><p>Best regards,<br>MyHomeTutor Team</p>" +
                          "</body></html>";
            
            new Thread(() -> {
                EmailService.sendEmail(studentEmail, subject, body);
            }).start();
        }
    }
}
