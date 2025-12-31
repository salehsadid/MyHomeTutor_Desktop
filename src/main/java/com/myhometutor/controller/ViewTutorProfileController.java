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

public class ViewTutorProfileController {

    @FXML private ImageView profileImageView;
    @FXML private Label userNameLabel;
    @FXML private Label userTypeLabel;
    
    // College Info
    @FXML private Label collegeNameLabel;
    @FXML private Label collegeGroupLabel;
    @FXML private Label hscYearLabel;
    
    // University Info
    @FXML private Label universityNameLabel;
    @FXML private Label universityDeptLabel;
    @FXML private Label universityYearLabel;
    @FXML private Label universitySessionLabel;
    
    @FXML private Label experienceLabel;
    
    @FXML private Label divisionLabel;
    @FXML private Label districtLabel;
    @FXML private Label areaLabel;
    
    @FXML private Label genderLabel;
    
    @FXML private javafx.scene.layout.VBox contactDetailsBox;
    @FXML private Label emailLabel;
    @FXML private Label phoneLabel;
    
    @FXML private Label preferredFeeLabel;
    @FXML private Label preferredDayLabel;
    @FXML private Label preferredTimeLabel;
    @FXML private Label preferredLocationLabel;
    
    @FXML private VBox verificationDocContainer;
    @FXML private ImageView verificationDocImage;
    @FXML private Label noDocLabel;
    
    @FXML private Button closeButton;
    @FXML private HBox adminActionBox;
    @FXML private Button reportButton;

    private int tutorId;
    private String userEmail;
    private String userName;
    
    @FXML private Label additionalInfoLabel;

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

    public void setTutorData(JSONObject tutorData) {
        if (tutorData != null) {
            if (tutorData.has("id")) {
                this.tutorId = tutorData.getInt("id");
            }
            if (tutorData.has("email")) {
                this.userEmail = tutorData.getString("email");
            }
            
            if (tutorData.has("name")) {
                userName = tutorData.getString("name");
                userNameLabel.setText(userName.toUpperCase());
            }
            
            // College Info
            if (tutorData.has("collegeName")) {
                collegeNameLabel.setText(tutorData.getString("collegeName"));
            }
            if (tutorData.has("collegeGroup")) {
                collegeGroupLabel.setText(tutorData.getString("collegeGroup"));
            }
            if (tutorData.has("hscYear")) {
                hscYearLabel.setText(tutorData.getString("hscYear"));
            }
            
            // University Info
            if (tutorData.has("universityName")) {
                universityNameLabel.setText(tutorData.getString("universityName"));
            }
            if (tutorData.has("universityDept")) {
                universityDeptLabel.setText(tutorData.getString("universityDept"));
            }
            if (tutorData.has("universityYear")) {
                universityYearLabel.setText(tutorData.getString("universityYear"));
            }
            if (tutorData.has("universitySession")) {
                universitySessionLabel.setText(tutorData.getString("universitySession"));
            }
            
            if (tutorData.has("experience")) {
                experienceLabel.setText(tutorData.getString("experience"));
            }
            
            if (tutorData.has("division")) {
                divisionLabel.setText(tutorData.getString("division"));
            }
            if (tutorData.has("district")) {
                districtLabel.setText(tutorData.getString("district"));
            }
            if (tutorData.has("area")) {
                areaLabel.setText(tutorData.getString("area"));
            }
            
            if (tutorData.has("gender")) {
                genderLabel.setText(tutorData.getString("gender"));
            }
            
            if (tutorData.has("email")) {
                userEmail = tutorData.getString("email");
                emailLabel.setText(userEmail);
            }
            if (tutorData.has("phone")) {
                phoneLabel.setText(tutorData.getString("phone"));
            }
            
            if (tutorData.has("preferredFee")) {
                preferredFeeLabel.setText(tutorData.getString("preferredFee") + " BDT");
            }
            if (tutorData.has("preferredDay")) {
                preferredDayLabel.setText(tutorData.getString("preferredDay"));
            }
            if (tutorData.has("preferredTime")) {
                preferredTimeLabel.setText(tutorData.getString("preferredTime"));
            }
            if (tutorData.has("preferredLocation")) {
                preferredLocationLabel.setText(tutorData.getString("preferredLocation"));
            }
            
            if (tutorData.has("additionalInfo")) {
                additionalInfoLabel.setText(tutorData.getString("additionalInfo"));
            }

            // Load profile picture
            if (tutorData.has("profilePicture")) {
                String picturePath = tutorData.getString("profilePicture");
                File imageFile = new File(picturePath);
                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString());
                    profileImageView.setImage(image);
                }
            }
            
            // Load Verification Document
            String base64Doc = tutorData.optString("verification_document_image", "");
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
        }
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

    public void setTutorId(int id) {
        this.tutorId = id;
    }

    private boolean isAdmin = false;

    public void setAdminMode(boolean isAdmin) {
        this.isAdmin = isAdmin;
        if (isAdmin) {
            adminActionBox.setVisible(true);
            adminActionBox.setManaged(true);
            setShowContactDetails(true); // Admin sees contact info
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

            controller.setReportDetails(reporterId, reporterType, tutorId, "Tutor");
            
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Report User");
            stage.setScene(new javafx.scene.Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleApprove() {
        DatabaseManager.getInstance().verifyTutor(tutorId, true);
        sendApprovalEmail();
        handleClose();
    }

    @FXML
    private void handleReject() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Reject Application");
        dialog.setHeaderText("Reason for Rejection");
        dialog.setContentText("Please enter the reason:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(reason -> {
            DatabaseManager.getInstance().verifyTutor(tutorId, false);
            sendRejectionEmail(reason);
            handleClose();
        });
    }

    private void sendRejectionEmail(String reason) {
        if (userEmail != null && !userEmail.isEmpty()) {
            String subject = "Application Rejected - MyHomeTutor";
            String body = "<html><body style='font-family: Arial, sans-serif;'>" +
                          "<p>Dear " + (userName != null ? userName : "User") + ",</p>" +
                          "<p>We regret to inform you that your application has been rejected by the admin.</p>" +
                          "<p><strong>Reason:</strong> " + reason + "</p>" +
                          "<p>Please contact support or re-apply with correct information.</p>" +
                          "<br><p>Best regards,<br>MyHomeTutor Team</p>" +
                          "</body></html>";
            
            new Thread(() -> {
                EmailService.sendEmail(userEmail, subject, body);
            }).start();
        }
    }

    private void sendApprovalEmail() {
        if (userEmail != null && !userEmail.isEmpty()) {
            String subject = "Account Verified - MyHomeTutor";
            String body = "<html><body style='font-family: Arial, sans-serif;'>" +
                          "<p>Dear " + (userName != null ? userName : "User") + ",</p>" +
                          "<p>Congratulations! Your MyHomeTutor account has been verified by the admin.</p>" +
                          "<p>You can now log in to your account and start using our services.</p>" +
                          "<br><p>Best regards,<br>MyHomeTutor Team</p>" +
                          "</body></html>";
                          
            new Thread(() -> {
                EmailService.sendEmail(userEmail, subject, body);
            }).start();
        }
    }
}
