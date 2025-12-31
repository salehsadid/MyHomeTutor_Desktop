package com.myhometutor.controller;

import com.myhometutor.database.DatabaseManager;
import com.myhometutor.model.SessionManager;
import com.myhometutor.util.ThemeManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Optional;

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

    public void filterByPostId(int postId) {
        postsContainer.getChildren().clear();
        int studentId = sessionManager.getUserId();
        JSONArray posts = dbManager.getStudentPosts(studentId);

        // Add a "Show All" button
        Button showAllBtn = new Button("← Show All Posts");
        showAllBtn.setOnAction(e -> loadPosts());
        showAllBtn.getStyleClass().add("action-button");
        showAllBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #3b82f6; -fx-border-color: #3b82f6; -fx-border-radius: 5;");
        HBox buttonContainer = new HBox(showAllBtn);
        buttonContainer.setPadding(new Insets(0, 0, 10, 0));
        postsContainer.getChildren().add(buttonContainer);

        boolean found = false;
        for (int i = 0; i < posts.length(); i++) {
            JSONObject post = posts.getJSONObject(i);
            if (post.getInt("id") == postId) {
                VBox postCard = createPostCard(post);
                // Highlight it
                postCard.setStyle(postCard.getStyle() + "-fx-border-color: #3b82f6; -fx-border-width: 2;");
                postsContainer.getChildren().add(postCard);
                found = true;
                break; 
            }
        }
        
        if (!found) {
            Label label = new Label("Post details not found.");
            label.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 14px;");
            postsContainer.getChildren().add(label);
        }
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
        
        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("secondary-button");
        deleteBtn.setStyle("-fx-text-fill: #ef4444; -fx-border-color: #ef4444; -fx-font-size: 11px; -fx-padding: 5 10;");
        deleteBtn.setOnAction(e -> handleDeletePost(post.getInt("id")));
        
        Label dateLabel = new Label(post.optString("createdAt", "").split(" ")[0]);
        dateLabel.getStyleClass().add("post-footer-text");
        
        header.getChildren().addAll(subjectLabel, classLabel, spacer, deleteBtn, dateLabel);
        
        // Details
        VBox details = new VBox(5);
        details.getChildren().add(createDetailRow("Type:", post.optString("type", "-")));
        details.getChildren().add(createDetailRow("Group:", post.optString("group", "-")));
        details.getChildren().add(createDetailRow("Salary:", post.optString("salary", "-") + " BDT"));
        details.getChildren().add(createDetailRow("Days:", post.optString("days", "-")));
        details.getChildren().add(createDetailRow("Hours:", post.optString("hours", "-")));
        details.getChildren().add(createDetailRow("Timing:", post.optString("timing", "-")));
        details.getChildren().add(createDetailRow("Location:", post.optString("address", "-")));
        if (post.has("additional") && !post.getString("additional").isEmpty()) {
            details.getChildren().add(createDetailRow("Note:", post.getString("additional")));
        }
        
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
        
        Button viewProfileBtn = new Button("View Profile");
        viewProfileBtn.getStyleClass().add("secondary-button");
        viewProfileBtn.setStyle("-fx-font-size: 11px; -fx-padding: 5 10;");
        viewProfileBtn.setOnAction(e -> handleViewProfile(app));
        
        String status = app.getString("status");
        if ("pending".equalsIgnoreCase(status)) {
            Button acceptBtn = new Button("Accept");
            acceptBtn.getStyleClass().add("primary-button");
            acceptBtn.setStyle("-fx-font-size: 11px; -fx-padding: 5 10;");
            acceptBtn.setOnAction(e -> handleAccept(app, post));
            row.getChildren().addAll(tutorInfo, spacer, viewProfileBtn, acceptBtn);
        } else {
            Label statusLabel = new Label(status.toUpperCase());
            if ("accepted".equalsIgnoreCase(status)) {
                statusLabel.setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold; -fx-font-size: 11px;");
            } else {
                statusLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-font-size: 11px;");
            }
            row.getChildren().addAll(tutorInfo, spacer, viewProfileBtn, statusLabel);
        }
        
        return row;
    }

    private void handleViewProfile(JSONObject app) {
        int tutorId = app.getInt("tutorId");
        String status = app.getString("status");
        boolean isAccepted = "accepted".equalsIgnoreCase(status);
        
        JSONObject tutor = dbManager.getTutorProfile(tutorId);
        if (tutor == null) return;
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ViewTutorProfile.fxml"));
            Parent root = loader.load();
            
            ViewTutorProfileController controller = loader.getController();
            controller.setTutorData(tutor);
            controller.setShowContactDetails(isAccepted);
            
            Stage stage = new Stage();
            stage.setTitle("Tutor Profile");
            stage.setScene(new Scene(root));
            
            // Apply current theme
            ThemeManager.getInstance().applyTheme(stage.getScene());
            
            // Set size to 80% of screen
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            stage.setWidth(screenBounds.getWidth() * 0.8);
            stage.setHeight(screenBounds.getHeight() * 0.8);
            
            stage.showAndWait();
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open tutor profile: " + e.getMessage());
        }
    }

    private void handleDeletePost(int postId) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Post");
        alert.setHeaderText("Are you sure you want to delete this post?");
        
        if (alert.showAndWait().get() == ButtonType.OK) {
            if (dbManager.deleteTuitionPost(postId)) {
                loadPosts();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete post.");
            }
        }
    }
    
    private void handleAccept(JSONObject app, JSONObject post) {
        boolean success = dbManager.acceptApplication(app.getInt("id"));
        if (success) {
            // Notify Tutor
            String studentName = sessionManager.getCurrentUser().getString("name");
            String message = "Congratulations! Student " + studentName + " has accepted your application for " + post.getString("subject") + ".";
            dbManager.createNotification(app.getInt("tutorId"), "Tutor", message, app.getInt("id"), "application");
            
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
