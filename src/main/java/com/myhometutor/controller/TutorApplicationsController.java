package com.myhometutor.controller;

import com.myhometutor.database.DatabaseManager;
import com.myhometutor.model.SessionManager;
import com.myhometutor.util.ThemeManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class TutorApplicationsController {

    @FXML private VBox applicationsContainer;
    @FXML private Button backButton;
    @FXML private Button refreshButton;
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
        
        loadApplications();
    }
    
    @FXML
    private void handleThemeToggle() {
        ThemeManager themeManager = ThemeManager.getInstance();
        themeManager.toggleTheme(themeToggle.getScene());
    }

    @FXML
    private void handleRefresh() {
        loadApplications();
    }

    private void loadApplications() {
        applicationsContainer.getChildren().clear();
        int tutorId = sessionManager.getUserId();
        JSONArray applications = dbManager.getTutorApplications(tutorId);

        if (applications.length() == 0) {
            Label noAppsLabel = new Label("You haven't applied to any tuitions yet.");
            noAppsLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 16px;");
            applicationsContainer.getChildren().add(noAppsLabel);
            return;
        }

        for (int i = 0; i < applications.length(); i++) {
            JSONObject app = applications.getJSONObject(i);
            VBox appCard = createApplicationCard(app);
            applicationsContainer.getChildren().add(appCard);
        }
    }

    private VBox createApplicationCard(JSONObject app) {
        VBox card = new VBox(10);
        card.getStyleClass().add("post-card");
        
        JSONObject post = app.getJSONObject("post");
        
        // Header: Subject and Status
        HBox header = new HBox(15);
        Label subjectLabel = new Label(post.optString("subject", "Unknown Subject"));
        subjectLabel.getStyleClass().add("post-title");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        String status = app.getString("status");
        Label statusLabel = new Label(status.toUpperCase());
        statusLabel.setStyle("-fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 15;");
        
        if ("accepted".equalsIgnoreCase(status)) {
            statusLabel.setStyle(statusLabel.getStyle() + "-fx-background-color: #dcfce7; -fx-text-fill: #166534;");
        } else {
            statusLabel.setStyle(statusLabel.getStyle() + "-fx-background-color: #fef9c3; -fx-text-fill: #854d0e;");
        }
        
        header.getChildren().addAll(subjectLabel, spacer, statusLabel);
        
        // Details
        VBox details = new VBox(5);
        details.getChildren().add(createDetailRow("Class:", post.optString("class", "-")));
        details.getChildren().add(createDetailRow("Salary:", post.optString("salary", "-") + " BDT"));
        details.getChildren().add(createDetailRow("Location:", post.optString("address", "-")));
        details.getChildren().add(createDetailRow("Applied On:", app.getString("appliedAt")));
        
        // Footer: Student Contact (only if accepted)
        if ("accepted".equalsIgnoreCase(status)) {
            VBox footer = new VBox(5);
            footer.setPadding(new Insets(10, 0, 0, 0));
            Label contactLabel = new Label("Student Contact: " + post.optString("studentPhone", "Hidden"));
            contactLabel.setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold;");
            footer.getChildren().add(contactLabel);
            card.getChildren().addAll(header, new Separator(), details, new Separator(), footer);
        } else {
            card.getChildren().addAll(header, new Separator(), details);
        }
        
        return card;
    }
    
    private HBox createDetailRow(String label, String value) {
        HBox row = new HBox(10);
        Label l = new Label(label);
        l.getStyleClass().add("post-detail-label");
        l.setMinWidth(80);
        
        Label v = new Label(value);
        v.getStyleClass().add("post-detail-value");
        v.setWrapText(true);
        
        row.getChildren().addAll(l, v);
        return row;
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TutorDashboard.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(root);
            ThemeManager.getInstance().applyTheme(scene);
            stage.setScene(scene);
            stage.setTitle("MyHomeTutor - Tutor Dashboard");
            
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
