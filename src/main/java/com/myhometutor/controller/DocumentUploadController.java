package com.myhometutor.controller;

import com.myhometutor.database.DatabaseManager;
import com.myhometutor.util.EmailService;
import com.myhometutor.util.ThemeManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Base64;

public class DocumentUploadController {

    @FXML private ComboBox<String> documentTypeCombo;
    @FXML private ImageView documentPreview;
    @FXML private Label uploadPlaceholder;
    @FXML private Label fileNameLabel;
    @FXML private Button uploadButton;

    private File selectedFile;
    private JSONObject registrationData;
    private String userType; // "Student" or "Tutor"
    private DatabaseManager dbManager;

    @FXML
    private void initialize() {
        dbManager = DatabaseManager.getInstance();
        
        documentTypeCombo.getItems().addAll(
            "National ID card",
            "School ID card",
            "College ID card",
            "University ID card",
            "Birth Certificate",
            "Driving License",
            "Academic Gradesheet"
        );
    }

    public void setRegistrationData(JSONObject data, String type) {
        this.registrationData = data;
        this.userType = type;
    }

    @FXML
    private void handleUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Document Image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        
        Stage stage = (Stage) uploadButton.getScene().getWindow();
        selectedFile = fileChooser.showOpenDialog(stage);
        
        if (selectedFile != null) {
            try {
                Image image = new Image(new FileInputStream(selectedFile));
                documentPreview.setImage(image);
                uploadPlaceholder.setVisible(false);
                fileNameLabel.setText(selectedFile.getName());
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to load image.");
            }
        }
    }

    @FXML
    private void handleFinishRegistration() {
        if (documentTypeCombo.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please select a document type.");
            return;
        }
        
        if (selectedFile == null) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please upload a document image.");
            return;
        }

        // Convert image to Base64 string to store in JSON
        String imageBase64 = encodeImageToBase64(selectedFile);
        if (imageBase64 == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to process image.");
            return;
        }

        // Add verification data to registration object
        registrationData.put("verification_document_type", documentTypeCombo.getValue());
        registrationData.put("verification_document_image", imageBase64);
        registrationData.put("verification_status", "pending");

        boolean success;
        String email = registrationData.getString("email");
        String password = registrationData.getString("password"); // Note: Password should be handled securely
        
        // Check if user exists with 'rejected' status and delete if so
        String existingStatus = dbManager.getUserStatus(email, userType);
        if ("rejected".equals(existingStatus)) {
            dbManager.deleteUserByUsername(email, userType);
        }
        
        // Remove password from JSON data stored in 'data' column, as it's stored in 'password' column
        registrationData.remove("password");

        if ("Student".equals(userType)) {
            success = dbManager.registerStudent(email, password, registrationData);
        } else {
            success = dbManager.registerTutor(email, password, registrationData);
        }

        if (success) {
            sendConfirmationEmail(email);
            showAlert(Alert.AlertType.INFORMATION, "Registration Successful", 
                    "Your account has been created successfully!\n" +
                    "Please wait for admin verification before logging in.");
            navigateToHome();
        } else {
            showAlert(Alert.AlertType.ERROR, "Registration Error", 
                    "Failed to create account.\nPlease try again.");
        }
    }

    private String encodeImageToBase64(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            byte[] bytes = new byte[(int) file.length()];
            fileInputStream.read(bytes);
            return Base64.getEncoder().encodeToString(bytes);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private void sendConfirmationEmail(String email) {
        String subject = "Registration Successful - Verification Pending";
        String userName = registrationData.optString("name", "User");
        String body = "<html><body style='font-family: Arial, sans-serif;'>" +
                  "<p>Dear " + userName + ",</p>" +
                  "<p>Thank you for registering with MyHomeTutor.</p>" +
                  "<p>Your account has been successfully created and is currently under review.</p>" +
                  "<p>You will be notified once your account is verified by an admin.</p>" +
                  "<br><p>Best regards,<br>MyHomeTutor Team</p>" +
                  "</body></html>";
                      
        // Run in background thread to avoid freezing UI
        new Thread(() -> {
            EmailService.sendEmail(email, subject, body);
        }).start();
    }

    private void navigateToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/HomePage.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) uploadButton.getScene().getWindow();
            double width = stage.getWidth();
            double height = stage.getHeight();
            
            Scene scene = new Scene(root, width, height);
            ThemeManager.getInstance().applyTheme(scene);
            
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
