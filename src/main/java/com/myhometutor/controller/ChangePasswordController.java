package com.myhometutor.controller;

import com.myhometutor.database.DatabaseManager;
import com.myhometutor.model.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

import javafx.scene.control.TextField;

public class ChangePasswordController {

    @FXML private PasswordField currentPasswordField;
    @FXML private TextField currentPasswordText;
    @FXML private Button toggleCurrentBtn;
    
    @FXML private PasswordField newPasswordField;
    @FXML private TextField newPasswordText;
    @FXML private Button toggleNewBtn;
    
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField confirmPasswordText;
    @FXML private Button toggleConfirmBtn;
    
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private DatabaseManager dbManager;
    private SessionManager sessionManager;
    
    private boolean isCurrentVisible = false;
    private boolean isNewVisible = false;
    private boolean isConfirmVisible = false;

    @FXML
    private void initialize() {
        dbManager = DatabaseManager.getInstance();
        sessionManager = SessionManager.getInstance();
        
        setupPasswordToggle(currentPasswordField, currentPasswordText);
        setupPasswordToggle(newPasswordField, newPasswordText);
        setupPasswordToggle(confirmPasswordField, confirmPasswordText);
    }
    
    private void setupPasswordToggle(PasswordField pf, TextField tf) {
        tf.textProperty().bindBidirectional(pf.textProperty());
    }
    
    @FXML
    private void toggleCurrentPassword() {
        isCurrentVisible = !isCurrentVisible;
        toggleVisibility(isCurrentVisible, currentPasswordField, currentPasswordText, toggleCurrentBtn);
    }
    
    @FXML
    private void toggleNewPassword() {
        isNewVisible = !isNewVisible;
        toggleVisibility(isNewVisible, newPasswordField, newPasswordText, toggleNewBtn);
    }
    
    @FXML
    private void toggleConfirmPassword() {
        isConfirmVisible = !isConfirmVisible;
        toggleVisibility(isConfirmVisible, confirmPasswordField, confirmPasswordText, toggleConfirmBtn);
    }
    
    private void toggleVisibility(boolean isVisible, PasswordField pf, TextField tf, Button btn) {
        if (isVisible) {
            tf.setVisible(true);
            pf.setVisible(false);
            btn.setText("🙈");
        } else {
            tf.setVisible(false);
            pf.setVisible(true);
            btn.setText("👁");
        }
    }

    @FXML
    private void handleSave() {
        String currentPass = isCurrentVisible ? currentPasswordText.getText() : currentPasswordField.getText();
        String newPass = isNewVisible ? newPasswordText.getText() : newPasswordField.getText();
        String confirmPass = isConfirmVisible ? confirmPasswordText.getText() : confirmPasswordField.getText();

        if (currentPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill all fields.");
            return;
        }

        if (!newPass.equals(confirmPass)) {
            showAlert(Alert.AlertType.ERROR, "Error", "New passwords do not match.");
            return;
        }

        int userId = sessionManager.getUserId();
        String userType = sessionManager.getUserType();

        if (dbManager.updatePassword(userId, userType, currentPass, newPass)) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Password updated successfully.");
            closeDialog();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Incorrect current password or update failed.");
        }
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}