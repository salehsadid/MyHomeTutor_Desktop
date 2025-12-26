package com.myhometutor.controller;

import com.myhometutor.util.ThemeManager;
import com.myhometutor.database.DatabaseManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class AdminDashboardController {

    @FXML private Label totalStudentsLabel;
    @FXML private Label totalTutorsLabel;
    @FXML private Label activePostsLabel;
    @FXML private Label pendingVerificationsLabel;

    @FXML
    private void initialize() {
        loadStats();
    }

    private void loadStats() {
        DatabaseManager db = DatabaseManager.getInstance();
        totalStudentsLabel.setText(String.valueOf(db.getTotalStudents()));
        totalTutorsLabel.setText(String.valueOf(db.getTotalTutors()));
        activePostsLabel.setText(String.valueOf(db.getActivePostsCount()));
        pendingVerificationsLabel.setText(String.valueOf(db.getPendingTutorsCount()));
    }

    @FXML
    private void handleUserManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AdminUserManagement.fxml"));
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

    @FXML
    private void handleVerifyTutors() {
        handleUserManagement(); // For now, redirect to User Management
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