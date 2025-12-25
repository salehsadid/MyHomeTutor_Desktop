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
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class TutorDashboardController {
    
    @FXML private Button notificationBtn;
    @FXML private ToggleButton themeToggle;
    @FXML private Button changePhotoBtn;
    
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
    @FXML private Label emailLabel;
    @FXML private Label phoneLabel;
    
    @FXML private Label preferredFeeLabel;
    @FXML private Label preferredDayLabel;
    @FXML private Label preferredTimeLabel;
    @FXML private Label preferredLocationLabel;
    
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
    }
    
    private void loadUserData() {
        JSONObject userData = sessionManager.getCurrentUser();
        
        if (userData != null) {
            if (userData.has("name")) {
                userNameLabel.setText(userData.getString("name").toUpperCase());
            }
            
            // College Info
            if (userData.has("collegeName")) {
                collegeNameLabel.setText(userData.getString("collegeName"));
            }
            if (userData.has("collegeGroup")) {
                collegeGroupLabel.setText(userData.getString("collegeGroup"));
            }
            if (userData.has("hscYear")) {
                hscYearLabel.setText(userData.getString("hscYear"));
            }
            
            // University Info
            if (userData.has("universityName")) {
                universityNameLabel.setText(userData.getString("universityName"));
            }
            if (userData.has("universityDept")) {
                universityDeptLabel.setText(userData.getString("universityDept"));
            }
            if (userData.has("universityYear")) {
                universityYearLabel.setText(userData.getString("universityYear"));
            }
            if (userData.has("universitySession")) {
                universitySessionLabel.setText(userData.getString("universitySession"));
            }
            
            if (userData.has("experience")) {
                experienceLabel.setText(userData.getString("experience"));
            }
            
            if (userData.has("division")) {
                divisionLabel.setText(userData.getString("division"));
            }
            if (userData.has("district")) {
                districtLabel.setText(userData.getString("district"));
            }
            if (userData.has("area")) {
                areaLabel.setText(userData.getString("area"));
            }
            
            if (userData.has("gender")) {
                genderLabel.setText(userData.getString("gender"));
            }

            if (userData.has("email")) {
                emailLabel.setText(userData.getString("email"));
            }
            if (userData.has("phone")) {
                phoneLabel.setText(userData.getString("phone"));
            }
            
            if (userData.has("preferredFee")) {
                preferredFeeLabel.setText(userData.getString("preferredFee") + " BDT");
            }
            if (userData.has("preferredDay")) {
                preferredDayLabel.setText(userData.getString("preferredDay"));
            }
            if (userData.has("preferredTime")) {
                preferredTimeLabel.setText(userData.getString("preferredTime"));
            }
            if (userData.has("preferredLocation")) {
                preferredLocationLabel.setText(userData.getString("preferredLocation"));
            }
            
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
                
                String userId = String.valueOf(sessionManager.getUserId());
                String extension = selectedFile.getName().substring(selectedFile.getName().lastIndexOf("."));
                File destFile = new File(profilePicsDir, "tutor_" + userId + extension);
                
                Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                
                JSONObject userData = sessionManager.getCurrentUser();
                userData.put("profilePicture", destFile.getAbsolutePath());
                
                boolean updated = dbManager.updateTutor(Integer.parseInt(userId), userData);
                
                if (updated) {
                    sessionManager.setCurrentUser(userData, "Tutor");
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
    private void handleApplications() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TutorApplications.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) notificationBtn.getScene().getWindow();
            Scene scene = new Scene(root);
            themeManager.applyTheme(scene);
            stage.setScene(scene);
            stage.setTitle("MyHomeTutor - My Applications");
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", 
                    "Failed to load applications page: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleExplore() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ExploreTuitions.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) notificationBtn.getScene().getWindow();
            Scene scene = new Scene(root);
            themeManager.applyTheme(scene);
            stage.setScene(scene);
            stage.setTitle("MyHomeTutor - Explore Tuitions");
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", 
                    "Failed to load explore page: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleEditProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EditProfileDialog.fxml"));
            Parent root = loader.load();
            
            EditProfileDialogController controller = loader.getController();
            controller.setUserType("Tutor");
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
    private void handleThemeToggle() {
        themeManager.toggleTheme(themeToggle.getScene());
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
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to logout?");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                sessionManager.logout();
                navigateToHome();
            }
        });
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
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Account");
        alert.setHeaderText("Are you sure you want to delete your account?");
        alert.setContentText("This action cannot be undone. All your data will be permanently removed.");

        if (alert.showAndWait().get() == ButtonType.OK) {
            String username = sessionManager.getUsername();
            String userType = sessionManager.getUserType().toLowerCase();
            
            if (dbManager.deleteUser(username, userType)) {
                handleLogout();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Could not delete account. Please try again.");
            }
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
