package com.myhometutor.controller;

import com.myhometutor.database.DatabaseManager;
import com.myhometutor.model.SessionManager;
import com.myhometutor.util.ThemeManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;
import java.nio.file.StandardCopyOption;

public class StudentDashboardController {
    
    @FXML private Button notificationBtn;
    @FXML private Circle notificationBadge;
    @FXML private ToggleButton themeToggle;
    @FXML private Button changePhotoBtn;
    
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
    @FXML private Label emailLabel;
    @FXML private Label phoneLabel;
    @FXML private Label additionalInfoLabel;
    
    private SessionManager sessionManager;
    private DatabaseManager dbManager;
    private ThemeManager themeManager;
    
    @FXML
    private void initialize() {
        sessionManager = SessionManager.getInstance();
        dbManager = DatabaseManager.getInstance();
        themeManager = ThemeManager.getInstance();
        
        loadUserData();
        loadProfilePicture();
        
        themeToggle.setSelected(themeManager.isDarkMode());

        // Make profile image circular
        profileImageView.setPreserveRatio(false);
        Circle clip = new Circle(
            profileImageView.getFitWidth() / 2,
            profileImageView.getFitHeight() / 2,
            profileImageView.getFitWidth() / 2
        );
        profileImageView.setClip(clip);
        
        checkNotifications();
    }
    
    private void checkNotifications() {
        JSONObject user = sessionManager.getCurrentUser();
        if (user != null) {
            int unreadCount = dbManager.getUnreadNotificationCount(user.getInt("id"), "Student");
            notificationBadge.setVisible(unreadCount > 0);
        }
    }
    
    private void loadUserData() {
        JSONObject userData = sessionManager.getCurrentUser();
        
        if (userData != null) {
            // Set user name
            if (userData.has("name")) {
                userNameLabel.setText(userData.getString("name").toUpperCase());
            }
            
            // Set educational details
            if (userData.has("institute")) {
                instituteLabel.setText(userData.getString("institute"));
            }
            if (userData.has("class")) {
                classLabel.setText(userData.getString("class"));
            }
            if (userData.has("group")) {
                groupLabel.setText(userData.getString("group"));
            } else if (userData.has("subject")) {
                groupLabel.setText(userData.getString("subject"));
            }
            
            // Set location details
            if (userData.has("division")) {
                divisionLabel.setText(userData.getString("division"));
            }
            if (userData.has("district")) {
                districtLabel.setText(userData.getString("district"));
            }
            if (userData.has("area")) {
                areaLabel.setText(userData.getString("area"));
            }
            
            // Set personal details
            if (userData.has("gender")) {
                genderLabel.setText(userData.getString("gender"));
            }
            if (userData.has("email")) {
                emailLabel.setText(userData.getString("email"));
            }
            if (userData.has("phone")) {
                phoneLabel.setText(userData.getString("phone"));
            }
            
            // Set additional info
            if (userData.has("additionalInfo")) {
                additionalInfoLabel.setText(userData.getString("additionalInfo"));
            }
        }
    }
    
    private void loadProfilePicture() {
        JSONObject userData = sessionManager.getCurrentUser();
        if (userData != null && userData.has("profilePicture")) {
            String picturePath = userData.getString("profilePicture");
            File imageFile = new File(picturePath);
            if (imageFile.exists()) {
                Image image = new Image(imageFile.toURI().toString());
                profileImageView.setImage(image);
            }
        }
    }
    
    @FXML
    private void handleChangePhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        
        Stage stage = (Stage) changePhotoBtn.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        
        if (selectedFile != null) {
            try {
                File profilePicsDir = new File("profile_pictures");
                if (!profilePicsDir.exists()) {
                    profilePicsDir.mkdirs();
                }
                
                int userId = sessionManager.getUserId();
                String extension = selectedFile.getName().substring(selectedFile.getName().lastIndexOf("."));
                File destFile = new File(profilePicsDir, "student_" + userId + extension);
                
                Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                
                JSONObject userData = sessionManager.getCurrentUser();
                userData.put("profilePicture", destFile.getAbsolutePath());
                
                boolean updated = dbManager.updateStudent(userId, userData);
                
                if (updated) {
                    sessionManager.setCurrentUser(userData, "Student");
                    Image image = new Image(destFile.toURI().toString());
                    profileImageView.setImage(image);
                    
                    showAlert(Alert.AlertType.INFORMATION, "Success", 
                            "Profile picture updated successfully!");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", 
                            "Failed to update profile picture in database.");
                }
                
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", 
                        "Failed to save profile picture: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handlePostTuition() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/PostTuition.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) notificationBtn.getScene().getWindow();
            Scene scene = new Scene(root);
            themeManager.applyTheme(scene);
            stage.setScene(scene);
            stage.setTitle("MyHomeTutor - Post Tuition");
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", 
                    "Failed to load post tuition page: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleMyPosts() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/StudentPosts.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) notificationBtn.getScene().getWindow();
            Scene scene = new Scene(root);
            themeManager.applyTheme(scene);
            stage.setScene(scene);
            stage.setTitle("MyHomeTutor - My Posts");
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", 
                    "Failed to load posts page: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleEditProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EditProfileDialog.fxml"));
            Parent root = loader.load();
            
            EditProfileDialogController controller = loader.getController();
            controller.setUserType("Student");
            controller.setDashboardController(this);
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit Profile");
            dialogStage.setScene(new Scene(root));
            
            // Apply current theme
            ThemeManager.getInstance().applyTheme(dialogStage.getScene());
            
            dialogStage.setResizable(false);
            dialogStage.initOwner(notificationBtn.getScene().getWindow());
            dialogStage.showAndWait();
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", 
                    "Failed to open edit profile dialog: " + e.getMessage());
        }
    }
    
    public void refreshProfile() {
        loadUserData();
        loadProfilePicture();
    }
    
    @FXML
    private void handleThemeToggle() {
        themeManager.toggleTheme(themeToggle.getScene());
    }

    @FXML
    private void handleChangePassword() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ChangePasswordDialog.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Change Password");
            stage.setScene(new Scene(root));
            
            // Apply current theme
            ThemeManager.getInstance().applyTheme(stage.getScene());
            
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open change password dialog.");
        }
    }

    @FXML
    private void handleNotifications() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Notifications.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) notificationBtn.getScene().getWindow();
            Scene scene = new Scene(root);
            themeManager.applyTheme(scene);
            stage.setScene(scene);
            stage.setTitle("MyHomeTutor - Notifications");
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", 
                    "Failed to load notifications page: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout");
        alert.setHeaderText("Are you sure you want to logout?");
        alert.setContentText("You will be returned to the login screen.");
        
        ButtonType response = alert.showAndWait().orElse(ButtonType.CANCEL);
        
        if (response == ButtonType.OK) {
            sessionManager.logout();
            navigateToHome();
        }
    }
    
    private void navigateToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/HomePage.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) notificationBtn.getScene().getWindow();
            double width = stage.getWidth();
            double height = stage.getHeight();
            boolean maximized = stage.isMaximized();
            boolean fullScreen = stage.isFullScreen();

            Scene scene = new Scene(root);
            ThemeManager.getInstance().applyTheme(scene);
            stage.setScene(scene);
            stage.setTitle("MyHomeTutor - Home");
            
            // Restore dimensions
            stage.setWidth(width);
            stage.setHeight(height);
            stage.setMaximized(maximized);
            stage.setFullScreen(fullScreen);
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", 
                    "Failed to load home page: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleDeleteAccount() {
        // Create a custom dialog
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Delete Account");
        dialog.setHeaderText("Please enter your password to confirm account deletion.\nThis action cannot be undone.");

        // Set the button types
        ButtonType deleteButtonType = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(deleteButtonType, ButtonType.CANCEL);

        // Create the password field
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setStyle("-fx-pref-width: 200px;");

        VBox content = new VBox(10);
        content.getChildren().add(passwordField);
        dialog.getDialogPane().setContent(content);

        // Request focus on the password field by default
        javafx.application.Platform.runLater(passwordField::requestFocus);

        // Convert the result to a password string when the delete button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == deleteButtonType) {
                return passwordField.getText();
            }
            return null;
        });
        
        // Apply theme
        ThemeManager.getInstance().applyTheme(dialog.getDialogPane().getScene());

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(password -> {
            if (password.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error", "Password cannot be empty.");
                return;
            }

            String username = sessionManager.getUsername();
            String userType = sessionManager.getUserType().toLowerCase();
            
            // Verify password
            boolean isVerified = false;
            if (dbManager.authenticateStudent(username, password) != null) {
                isVerified = true;
            }

            if (isVerified) {
                if (dbManager.deleteUser(username, userType)) {
                    handleLogout();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Could not delete account. Please try again.");
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Authentication Failed", "Incorrect password. Account deletion cancelled.");
            }
        });
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
