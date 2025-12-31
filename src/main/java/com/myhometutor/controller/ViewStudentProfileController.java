package com.myhometutor.controller;

import com.myhometutor.util.EmailService;
import com.myhometutor.util.ThemeManager;
import com.myhometutor.database.DatabaseManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Base64;
import java.util.Optional;

public class ViewStudentProfileController {

    @FXML private ImageView profileImageView;
    @FXML private Label userNameLabel;
    @FXML private Label userTypeLabel;
    
    @FXML private Label instituteLabel;
    @FXML private Label classLabel;
    @FXML private Label groupLabel;
    
    @FXML private Label divisionLabel;
    @FXML private Label districtLabel;
    @FXML private Label areaLabel;
    
    @FXML private Label genderLabel;
    
    @FXML private javafx.scene.layout.VBox contactDetailsBox;
    @FXML private Label emailLabel;
    @FXML private Label phoneLabel;
    
    @FXML private Label additionalInfoLabel;
    
    @FXML private VBox verificationDocContainer;
    @FXML private ImageView verificationDocImage;
    @FXML private Label noDocLabel;
    
    @FXML private Button closeButton;
    @FXML private HBox adminActionBox;
    @FXML private Button reportButton;

    private int studentId;
    private String userEmail;
    private String userName;

    @FXML
    private void initialize() {
        // Make profile image circular
        profileImageView.setPreserveRatio(false);
        Circle clip = new Circle(
            75, // radius (150/2)
            75, // centerX
            75  // centerY
        );
        profileImageView.setClip(clip);
    }

    public void setStudentData(JSONObject studentData) {
        if (studentData != null) {
            this.studentId = studentData.getInt("id");
            this.userEmail = studentData.getString("email");
            this.userName = studentData.getString("name");

            // Set user name
            if (studentData.has("name")) {
                userNameLabel.setText(studentData.getString("name").toUpperCase());
            }
            
            // Set educational details
            if (studentData.has("institute")) {
                instituteLabel.setText(studentData.getString("institute"));
            }
            if (studentData.has("class")) {
                classLabel.setText(studentData.getString("class"));
            }
            if (studentData.has("group")) {
                groupLabel.setText(studentData.getString("group"));
            } else if (studentData.has("subject")) {
                groupLabel.setText(studentData.getString("subject"));
            }
            
            // Set location details
            if (studentData.has("division")) {
                divisionLabel.setText(studentData.getString("division"));
            }
            if (studentData.has("district")) {
                districtLabel.setText(studentData.getString("district"));
            }
            if (studentData.has("area")) {
                areaLabel.setText(studentData.getString("area"));
            }
            
            // Set personal details
            if (studentData.has("gender")) {
                genderLabel.setText(studentData.getString("gender"));
            }
            
            if (studentData.has("email")) {
                emailLabel.setText(studentData.getString("email"));
            }
            if (studentData.has("phone")) {
                phoneLabel.setText(studentData.getString("phone"));
            }
            
            // Load profile image
            String imagePath = studentData.optString("profilePicture", "");
            if (!imagePath.isEmpty()) {
                try {
                    File file = new File(imagePath);
                    if (file.exists()) {
                        Image image = new Image(file.toURI().toString());
                        profileImageView.setImage(image);
                    }
                } catch (Exception e) {
                    System.err.println("Error loading profile image: " + e.getMessage());
                }
            } else {
                // Set default placeholder if needed, or keep the one from FXML/CSS
                // Assuming FXML has a default image or style
            }
            
            // Load Verification Document
            String base64Doc = studentData.optString("verification_document_image", "");
            if (!base64Doc.isEmpty()) {
                try {
                    byte[] imageBytes = Base64.getDecoder().decode(base64Doc);
                    Image image = new Image(new ByteArrayInputStream(imageBytes));
                    verificationDocImage.setImage(image);
                    
                    if (isAdmin) {
                        verificationDocContainer.setVisible(true);
                        verificationDocContainer.setManaged(true);
                        verificationDocImage.setVisible(true);
                        verificationDocImage.setManaged(true);
                        noDocLabel.setVisible(false);
                        noDocLabel.setManaged(false);
                    } else {
                        verificationDocContainer.setVisible(false);
                        verificationDocContainer.setManaged(false);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    noDocLabel.setText("Error loading document");
                    if (isAdmin) {
                        verificationDocContainer.setVisible(true);
                        verificationDocContainer.setManaged(true);
                        noDocLabel.setVisible(true);
                        noDocLabel.setManaged(true);
                    } else {
                        verificationDocContainer.setVisible(false);
                        verificationDocContainer.setManaged(false);
                    }
                }
            } else {
                verificationDocImage.setImage(null);
                if (isAdmin) {
                    verificationDocContainer.setVisible(true);
                    verificationDocContainer.setManaged(true);
                    verificationDocImage.setVisible(false);
                    verificationDocImage.setManaged(false);
                    noDocLabel.setVisible(true);
                    noDocLabel.setManaged(true);
                } else {
                    verificationDocContainer.setVisible(false);
                    verificationDocContainer.setManaged(false);
                }
            }

            // Show/Hide Admin Actions based on status
            String status = studentData.optString("status", "active");
            if ("pending".equalsIgnoreCase(status)) {
                adminActionBox.setVisible(true);
                adminActionBox.setManaged(true);
            } else {
                adminActionBox.setVisible(false);
                adminActionBox.setManaged(false);
            }
        }
    }

    @FXML
    private void handleApprove() {
        if (DatabaseManager.updateUserStatus("students", studentId, "active")) {
            sendApprovalEmail();
            handleClose();
        }
    }

    @FXML
    private void handleReject() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Reject Application");
        dialog.setHeaderText("Reason for Rejection");
        dialog.setContentText("Please enter the reason:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(reason -> {
            if (DatabaseManager.updateUserStatus("students", studentId, "rejected")) {
                sendRejectionEmail(reason);
                handleClose();
            }
        });
    }

    private void sendRejectionEmail(String reason) {
        String subject = "Application Rejected - MyHomeTutor";
        String body = "<html>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<div style='background-color: #f4f4f4; padding: 20px;'>" +
                "<div style='background-color: #ffffff; padding: 20px; border-radius: 5px; box-shadow: 0 2px 5px rgba(0,0,0,0.1);'>" +
                "<h2 style='color: #c0392b;'>Application Rejected</h2>" +
                "<p>Dear " + userName + ",</p>" +
                "<p>We regret to inform you that your application has been rejected by the admin.</p>" +
                "<p><strong>Reason:</strong> " + reason + "</p>" +
                "<p>Please contact support or re-apply with correct information.</p>" +
                "<br>" +
                "<p style='font-size: 14px; color: #888;'>Best regards,<br>The MyHomeTutor Team</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";

        new Thread(() -> {
            EmailService.sendEmail(userEmail, subject, body);
        }).start();
    }

    private void sendApprovalEmail() {
        String subject = "Profile Approved - MyHomeTutor";
        String body = "<html>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<div style='background-color: #f4f4f4; padding: 20px;'>" +
                "<div style='background-color: #ffffff; padding: 20px; border-radius: 5px; box-shadow: 0 2px 5px rgba(0,0,0,0.1);'>" +
                "<h2 style='color: #2c3e50;'>Congratulations, " + userName + "!</h2>" +
                "<p style='font-size: 16px; color: #555;'>Your profile has been approved by the admin.</p>" +
                "<p style='font-size: 16px; color: #555;'>You can now log in to your account and start exploring tuitions.</p>" +
                "<br>" +
                "<p style='font-size: 14px; color: #888;'>Best regards,<br>The MyHomeTutor Team</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";

        new Thread(() -> {
            EmailService.sendEmail(userEmail, subject, body);
        }).start();
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    public void setShowContactDetails(boolean show) {
        contactDetailsBox.setVisible(show);
        contactDetailsBox.setManaged(show);
    }

    public void setStudentId(int id) {
        this.studentId = id;
    }

    private boolean isAdmin = false;

    public void setAdminMode(boolean isAdmin) {
        this.isAdmin = isAdmin;
        if (isAdmin) {
            adminActionBox.setVisible(true);
            adminActionBox.setManaged(true);
            setShowContactDetails(true);
            if (reportButton != null) {
                reportButton.setVisible(false);
                reportButton.setManaged(false);
            }
            
            // Show verification doc if available
            verificationDocContainer.setVisible(true);
            verificationDocContainer.setManaged(true);
            
            if (verificationDocImage.getImage() != null) {
                verificationDocImage.setVisible(true);
                verificationDocImage.setManaged(true);
                noDocLabel.setVisible(false);
                noDocLabel.setManaged(false);
            } else {
                verificationDocImage.setVisible(false);
                verificationDocImage.setManaged(false);
                noDocLabel.setVisible(true);
                noDocLabel.setManaged(true);
            }
        } else {
            adminActionBox.setVisible(false);
            adminActionBox.setManaged(false);
            if (reportButton != null) {
                reportButton.setVisible(true);
                reportButton.setManaged(true);
            }
            
            // Hide verification doc details
            verificationDocContainer.setVisible(false);
            verificationDocContainer.setManaged(false);
        }
    }

    @FXML
    private void handleReport() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/ReportDialog.fxml"));
            javafx.scene.Parent root = loader.load();
            
            ReportDialogController controller = loader.getController();
            int reporterId;
            String reporterType;
            
            if (isAdmin) {
                 reporterId = 0; 
                 reporterType = "Admin";
            } else {
                 reporterId = com.myhometutor.model.SessionManager.getInstance().getUserId();
                 reporterType = com.myhometutor.model.SessionManager.getInstance().getUserType();
            }

            controller.setReportDetails(reporterId, reporterType, studentId, "Student");
            
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Report User");
            stage.setScene(new javafx.scene.Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
}
