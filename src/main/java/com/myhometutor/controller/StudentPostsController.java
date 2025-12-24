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

public class StudentPostsController {

    @FXML private VBox postsContainer;
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
        
        loadPosts();
    }
    
    @FXML
    private void handleThemeToggle() {
        ThemeManager themeManager = ThemeManager.getInstance();
        themeManager.toggleTheme(themeToggle.getScene());
    }

    @FXML
    private void handleRefresh() {
        loadPosts();
    }

    private void loadPosts() {
        postsContainer.getChildren().clear();
        int studentId = sessionManager.getUserId();
        JSONArray posts = dbManager.getStudentPosts(studentId);

        if (posts.length() == 0) {
            Label noPostsLabel = new Label("You haven't posted any tuition requirements yet.");
            noPostsLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 16px;");
            postsContainer.getChildren().add(noPostsLabel);
            return;
        }

        for (int i = 0; i < posts.length(); i++) {
            JSONObject post = posts.getJSONObject(i);
            VBox postCard = createPostCard(post);
            postsContainer.getChildren().add(postCard);
        }
    }

    private VBox createPostCard(JSONObject post) {
        VBox card = new VBox(10);
        card.getStyleClass().add("post-card");
        
        // Header
        HBox header = new HBox(15);
        Label subjectLabel = new Label(post.optString("subject", "Unknown Subject"));
        subjectLabel.getStyleClass().add("post-title");
        
        Label classLabel = new Label("• " + post.optString("class", "Unknown Class"));
        classLabel.getStyleClass().add("post-subtitle");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label dateLabel = new Label(post.optString("createdAt", "").split(" ")[0]);
        dateLabel.getStyleClass().add("post-footer-text");
        
        header.getChildren().addAll(subjectLabel, classLabel, spacer, dateLabel);
        
        // Details
        VBox details = new VBox(5);
        details.getChildren().add(createDetailRow("Salary:", post.optString("salary", "-") + " BDT"));
        details.getChildren().add(createDetailRow("Days:", post.optString("days", "-")));
        
        // Applications Section
        VBox applicationsBox = new VBox(10);
        applicationsBox.setPadding(new Insets(10, 0, 0, 0));
        Label appsTitle = new Label("Applications:");
        appsTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #64748b;");
        applicationsBox.getChildren().add(appsTitle);
        
        JSONArray applications = dbManager.getPostApplications(post.getInt("id"));
        
        if (applications.length() == 0) {
            Label noAppsLabel = new Label("No applications yet.");
            noAppsLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-style: italic;");
            applicationsBox.getChildren().add(noAppsLabel);
        } else {
            for (int i = 0; i < applications.length(); i++) {
                JSONObject app = applications.getJSONObject(i);
                applicationsBox.getChildren().add(createApplicationRow(app, post));
            }
        }
        
        card.getChildren().addAll(header, new Separator(), details, new Separator(), applicationsBox);
        return card;
    }
    
    private HBox createApplicationRow(JSONObject app, JSONObject post) {
        HBox row = new HBox(10);
        row.setStyle("-fx-background-color: rgba(0,0,0,0.05); -fx-padding: 10; -fx-background-radius: 5;");
        
        VBox tutorInfo = new VBox(2);
        Label nameLabel = new Label(app.getString("tutorName"));
        nameLabel.setStyle("-fx-font-weight: bold;");
        Label instLabel = new Label(app.getString("tutorInstitution"));
        instLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");
        tutorInfo.getChildren().addAll(nameLabel, instLabel);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        String status = app.getString("status");
        if ("pending".equalsIgnoreCase(status)) {
            Button acceptBtn = new Button("Accept");
            acceptBtn.getStyleClass().add("primary-button");
            acceptBtn.setStyle("-fx-font-size: 11px; -fx-padding: 5 10;");
            acceptBtn.setOnAction(e -> handleAccept(app, post));
            row.getChildren().addAll(tutorInfo, spacer, acceptBtn);
        } else {
            Label statusLabel = new Label(status.toUpperCase());
            statusLabel.setStyle("-fx-text-fill: #166534; -fx-font-weight: bold; -fx-font-size: 11px;");
            row.getChildren().addAll(tutorInfo, spacer, statusLabel);
        }
        
        return row;
    }
    
    private void handleAccept(JSONObject app, JSONObject post) {
        boolean success = dbManager.acceptApplication(app.getInt("id"));
        if (success) {
            // Notify Tutor
            String studentName = sessionManager.getCurrentUser().getString("name");
            String message = "Congratulations! Student " + studentName + " has accepted your application for " + post.getString("subject") + ".";
            dbManager.createNotification(app.getInt("tutorId"), "Tutor", message);
            
            showAlert(Alert.AlertType.INFORMATION, "Success", "Application accepted!");
            loadPosts(); // Refresh
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to accept application.");
        }
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/StudentDashboard.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) backButton.getScene().getWindow();
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
