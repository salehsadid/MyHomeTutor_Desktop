package com.myhometutor.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class HomeController {
    
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private RadioButton studentRadio;
    @FXML private RadioButton tutorRadio;
    @FXML private ToggleGroup userTypeGroup;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    
    @FXML
    private void initialize() {
        studentRadio.setSelected(true);
    }
    
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Login Error", "Please enter both username and password.");
            return;
        }
        
        boolean isStudent = studentRadio.isSelected();
        String userType = isStudent ? "Student" : "Tutor";
        
        // Todo: Implement database authentication logic here
        // For now, show a  message
        System.out.println("Login attempt - User: " + username + ", Type: " + userType);
        
        // Placeholder: Show success message
        showAlert(Alert.AlertType.INFORMATION, "Login", 
                "Login functionality will be implemented soon.\n");
    }
    
    @FXML
    private void handleRegister() {
        try {
            String fxmlFile;
            if (studentRadio.isSelected()) {
                fxmlFile = "/fxml/StudentRegister.fxml";
            } else {
                fxmlFile = "/fxml/TutorRegister.fxml";
            }
            
            // Load the registration page
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            
            // Get current stage and set new scene
            Stage stage = (Stage) registerButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("MyHomeTutor - Registration");
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", 
                    "Failed to load registration page: " + e.getMessage());
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
