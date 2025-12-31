package com.myhometutor.controller;

import com.myhometutor.database.DatabaseManager;
import com.myhometutor.model.SessionManager;
import com.myhometutor.util.ThemeManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.io.IOException;

public class HomeController {
    
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordTextField;
    @FXML private Button togglePasswordBtn;
    @FXML private RadioButton studentRadio;
    @FXML private RadioButton tutorRadio;
    @FXML private ToggleGroup userTypeGroup;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private ToggleButton themeToggle;
    
    private DatabaseManager dbManager;
    private SessionManager sessionManager;
    private boolean isPasswordVisible = false;
    
    @FXML
    private void initialize() {
        studentRadio.setSelected(true);
        dbManager = DatabaseManager.getInstance();
        sessionManager = SessionManager.getInstance();
        
        passwordTextField.textProperty().bindBidirectional(passwordField.textProperty());
        
        ThemeManager themeManager = ThemeManager.getInstance();
        if (themeToggle != null) {
            themeToggle.setSelected(themeManager.isDarkMode());
        }
    }

    @FXML
    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;
        if (isPasswordVisible) {
            passwordTextField.setVisible(true);
            passwordField.setVisible(false);
            togglePasswordBtn.setText("🙈");
        } else {
            passwordTextField.setVisible(false);
            passwordField.setVisible(true);
            togglePasswordBtn.setText("👁");
        }
    }
    
    @FXML
    private void handleThemeToggle() {
        ThemeManager themeManager = ThemeManager.getInstance();
        themeManager.toggleTheme(themeToggle.getScene());
    }
    
    @FXML
    private void handleAdminLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AdminLogin.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) loginButton.getScene().getWindow();
            
            // Preserve current size
            double width = stage.getWidth();
            double height = stage.getHeight();
            
            Scene scene = new Scene(root, width, height);
            ThemeManager.getInstance().applyTheme(scene);
            
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Login Error", "Please enter both username and password.");
            return;
        }
        
        boolean isStudent = studentRadio.isSelected();
        String userType = isStudent ? "Student" : "Tutor";
        
        JSONObject userData = null;
        if (isStudent) {
            userData = dbManager.authenticateStudent(username, password);
        } else {
            userData = dbManager.authenticateTutor(username, password);
        }
        
        if (userData != null) {
            String status = userData.optString("status", "pending");
            if ("banned".equalsIgnoreCase(status)) {
                showAlert("Account Banned", 
                    "Your account has been banned due to policy violations.\nPlease contact admin for support.");
                return;
            }
            if ("pending".equalsIgnoreCase(status)) {
                showAlert("Account Verification Pending", 
                    "Your account is currently under review.\nPlease wait for admin verification.");
                return;
            }
            
            sessionManager.setCurrentUser(userData, userType);
            
            if (isStudent) {
                navigateToStudentDashboard();
            } else {
                navigateToTutorDashboard();
            }
        } else {
            showAlert("Login Failed",
                    "Invalid username or password.\nPlease try again.");
        }
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
            double width = stage.getWidth();
            double height = stage.getHeight();
            boolean maximized = stage.isMaximized();
            boolean fullScreen = stage.isFullScreen();

            Scene scene = new Scene(root);
            ThemeManager.getInstance().applyTheme(scene);
            stage.setScene(scene);
            stage.setTitle("MyHomeTutor - Registration");
            
            // Restore dimensions
            stage.setWidth(width);
            stage.setHeight(height);
            stage.setMaximized(maximized);
            stage.setFullScreen(fullScreen);
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Navigation Error",
                    "Failed to load registration page: " + e.getMessage());
        }
    }
    
    private void navigateToStudentDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/StudentDashboard.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) loginButton.getScene().getWindow();
            double width = stage.getWidth();
            double height = stage.getHeight();
            boolean maximized = stage.isMaximized();
            boolean fullScreen = stage.isFullScreen();

            Scene scene = new Scene(root);
            ThemeManager.getInstance().applyTheme(scene);
            stage.setScene(scene);
            stage.setTitle("MyHomeTutor - Student Dashboard");
            
            // Restore dimensions
            stage.setWidth(width);
            stage.setHeight(height);
            stage.setMaximized(maximized);
            stage.setFullScreen(fullScreen);
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Navigation Error",
                    "Failed to load dashboard: " + e.getMessage());
        }
    }
    
    private void navigateToTutorDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TutorDashboard.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) loginButton.getScene().getWindow();
            double width = stage.getWidth();
            double height = stage.getHeight();
            boolean maximized = stage.isMaximized();
            boolean fullScreen = stage.isFullScreen();

            Scene scene = new Scene(root);
            ThemeManager.getInstance().applyTheme(scene);
            stage.setScene(scene);
            stage.setTitle("MyHomeTutor - Tutor Dashboard");
            
            // Restore dimensions
            stage.setWidth(width);
            stage.setHeight(height);
            stage.setMaximized(maximized);
            stage.setFullScreen(fullScreen);
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Navigation Error",
                    "Failed to load tutor dashboard: " + e.getMessage());
        }
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
