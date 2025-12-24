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

public class PostTuitionController {

    @FXML private TextField subjectField;
    @FXML private TextField classField;
    @FXML private ComboBox<String> typeCombo;
    @FXML private ComboBox<String> groupCombo;
    @FXML private TextField daysField;
    @FXML private TextField hoursField;
    @FXML private TextField timingField;
    @FXML private TextField salaryField;
    @FXML private TextArea addressArea;
    @FXML private TextArea additionalArea;
    @FXML private Button postButton;
    @FXML private Button cancelButton;
    @FXML private ToggleButton themeToggle;

    private DatabaseManager dbManager;
    private SessionManager sessionManager;

    @FXML
    private void initialize() {
        dbManager = DatabaseManager.getInstance();
        sessionManager = SessionManager.getInstance();
        
        ThemeManager themeManager = ThemeManager.getInstance();
        if (themeToggle != null) {
            themeToggle.setSelected(themeManager.isDarkMode());
        }
        
        typeCombo.getItems().addAll("Online", "Offline");
        groupCombo.getItems().addAll("Science", "Commerce", "Humanities", "Dakhil", "General");
    }
    
    @FXML
    private void handleThemeToggle() {
        ThemeManager themeManager = ThemeManager.getInstance();
        themeManager.toggleTheme(themeToggle.getScene());
    }

    @FXML
    private void handlePost() {
        if (!validateFields()) {
            return;
        }

        JSONObject postData = new JSONObject();
        postData.put("subject", subjectField.getText().trim());
        postData.put("class", classField.getText().trim());
        postData.put("type", typeCombo.getValue());
        postData.put("group", groupCombo.getValue());
        postData.put("days", daysField.getText().trim());
        postData.put("hours", hoursField.getText().trim());
        postData.put("timing", timingField.getText().trim());
        postData.put("salary", salaryField.getText().trim());
        postData.put("address", addressArea.getText().trim());
        postData.put("additional", additionalArea.getText().trim());

        int studentId = sessionManager.getUserId();
        boolean success = dbManager.createTuitionPost(studentId, postData);

        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Tuition requirement posted successfully!");
            handleCancel(); // Go back to dashboard
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to post tuition requirement.");
        }
    }

    private boolean validateFields() {
        if (subjectField.getText().trim().isEmpty() || 
            classField.getText().trim().isEmpty() ||
            typeCombo.getValue() == null ||
            groupCombo.getValue() == null ||
            daysField.getText().trim().isEmpty() ||
            hoursField.getText().trim().isEmpty() ||
            timingField.getText().trim().isEmpty() ||
            salaryField.getText().trim().isEmpty() ||
            addressArea.getText().trim().isEmpty()) {
            
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please fill all required fields.");
            return false;
        }
        return true;
    }

    @FXML
    private void handleCancel() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/StudentDashboard.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            Scene scene = new Scene(root);
            ThemeManager.getInstance().applyTheme(scene);
            stage.setScene(scene);
            stage.setTitle("MyHomeTutor - Student Dashboard");
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to load dashboard.");
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
