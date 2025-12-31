package com.myhometutor.controller;

import com.myhometutor.util.ThemeManager;
import com.myhometutor.database.DatabaseManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;

import java.io.IOException;

public class AdminDashboardController {

    @FXML private Label totalStudentsLabel;
    @FXML private Label totalTutorsLabel;
    @FXML private Label pendingTutorVerificationsLabel;
    @FXML private Label pendingStudentVerificationsLabel;
    @FXML private Label totalTuitionPostsLabel;
    @FXML private Label pendingTuitionPostsLabel;
    @FXML private Label approvedTuitionPostsLabel;
    @FXML private Label totalConnectionsLabel;
    @FXML private ToggleButton themeToggle;

    @FXML
    private void initialize() {
        themeToggle.setSelected(ThemeManager.getInstance().isDarkMode());
        loadStats();
    }

    private void loadStats() {
        DatabaseManager db = DatabaseManager.getInstance();
        totalStudentsLabel.setText(String.valueOf(db.getTotalStudents()));
        totalTutorsLabel.setText(String.valueOf(db.getTotalTutors()));
        pendingTutorVerificationsLabel.setText(String.valueOf(db.getPendingTutorVerificationsCount()));
        pendingStudentVerificationsLabel.setText(String.valueOf(db.getPendingStudentVerificationsCount()));
        
        totalTuitionPostsLabel.setText(String.valueOf(db.getTotalTuitionPosts()));
        pendingTuitionPostsLabel.setText(String.valueOf(db.getPendingTuitionPostsCount()));
        approvedTuitionPostsLabel.setText(String.valueOf(db.getApprovedTuitionPostsCount()));
        totalConnectionsLabel.setText(String.valueOf(db.getTotalConnectionsCount()));
    }

    @FXML
    private void handleThemeToggle() {
        ThemeManager.getInstance().toggleTheme(themeToggle.getScene());
    }

    @FXML
    private void handleTotalStudentsClick() {
        openUserManagement("Student", "All");
    }

    @FXML
    private void handlePendingStudentVerificationsClick() {
        openUserManagement("Student", "Pending");
    }

    @FXML
    private void handleTotalTutorsClick() {
        openUserManagement("Tutor", "All");
    }

    @FXML
    private void handlePendingTutorVerificationsClick() {
        openUserManagement("Tutor", "Pending");
    }

    @FXML
    private void handleTotalTuitionPostsClick() {
        openTuitionPostList("All");
    }

    @FXML
    private void handlePendingTuitionPostsClick() {
        openTuitionPostList("Pending");
    }

    @FXML
    private void handleApprovedTuitionPostsClick() {
        openTuitionPostList("Active");
    }

    private void openTuitionPostList(String filterStatus) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AdminTuitionPostList.fxml"));
            Parent root = loader.load();
            
            AdminTuitionPostListController controller = loader.getController();
            controller.setFilter(filterStatus);
            
            Stage stage = (Stage) totalStudentsLabel.getScene().getWindow();
            Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
            ThemeManager.getInstance().applyTheme(scene);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleTotalConnectionsClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AdminConnectionList.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) totalStudentsLabel.getScene().getWindow();
            Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
            ThemeManager.getInstance().applyTheme(scene);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openUserManagement(String userType, String filterStatus) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AdminUserManagement.fxml"));
            Parent root = loader.load();
            
            AdminUserManagementController controller = loader.getController();
            controller.setFilter(userType, filterStatus);
            
            Stage stage = (Stage) totalStudentsLabel.getScene().getWindow();
            Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
            ThemeManager.getInstance().applyTheme(scene);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUserManagement() {
        openUserManagement("All", "All");
    }

    @FXML
    private void handleStudents() {
        openUserManagement("Student", "All");
    }

    @FXML
    private void handleTutors() {
        openUserManagement("Tutor", "All");
    }

    @FXML
    private void handleTuitionPosts() {
        openTuitionPostList("All");
    }

    @FXML
    private void handleConnections() {
        handleTotalConnectionsClick();
    }

    @FXML
    private void handleReports() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AdminReportList.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) totalStudentsLabel.getScene().getWindow();
            Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
            ThemeManager.getInstance().applyTheme(scene);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBannedUsers() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AdminBannedUsers.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) totalStudentsLabel.getScene().getWindow();
            Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
            ThemeManager.getInstance().applyTheme(scene);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/HomePage.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) totalStudentsLabel.getScene().getWindow();
            
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
}