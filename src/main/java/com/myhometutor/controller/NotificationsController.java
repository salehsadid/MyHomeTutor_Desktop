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
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class
NotificationsController {

    @FXML private VBox notificationsContainer;
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
        
        loadNotifications();
        markAsRead();
    }
    
    private void markAsRead() {
        int userId = sessionManager.getUserId();
        String userType = sessionManager.getUserType();
        dbManager.markNotificationsAsRead(userId, userType);
    }
    
    @FXML
    private void handleThemeToggle() {
        ThemeManager themeManager = ThemeManager.getInstance();
        themeManager.toggleTheme(themeToggle.getScene());
    }

    @FXML
    private void handleRefresh() {
        loadNotifications();
    }

    private void loadNotifications() {
        notificationsContainer.getChildren().clear();
        int userId = sessionManager.getUserId();
        String userType = sessionManager.getUserType();
        
        JSONArray notifications = dbManager.getUserNotifications(userId, userType);

        if (notifications.length() == 0) {
            Label noNotifLabel = new Label("No notifications.");
            noNotifLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 16px;");
            notificationsContainer.getChildren().add(noNotifLabel);
            return;
        }

        for (int i = 0; i < notifications.length(); i++) {
            JSONObject notif = notifications.getJSONObject(i);
            HBox notifCard = createNotificationCard(notif);
            notificationsContainer.getChildren().add(notifCard);
        }
    }

    private HBox createNotificationCard(JSONObject notif) {
        HBox card = new HBox(15);
        card.getStyleClass().add("post-card");
        card.setPadding(new Insets(15));
        card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        card.setCursor(javafx.scene.Cursor.HAND);
        
        VBox content = new VBox(5);
        Label messageLabel = new Label(notif.getString("message"));
        messageLabel.setWrapText(true);
        messageLabel.getStyleClass().add("notification-message");
        
        Label dateLabel = new Label(notif.getString("createdAt"));
        dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");
        
        content.getChildren().addAll(messageLabel, dateLabel);
        HBox.setHgrow(content, Priority.ALWAYS);
        
        Button deleteBtn = new Button("✕");
        deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ef4444; -fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 0 5 0 5;");
        deleteBtn.setVisible(false);
        
        deleteBtn.setOnAction(e -> {
            e.consume(); // Prevent card click
            if (dbManager.deleteNotification(notif.getInt("id"))) {
                notificationsContainer.getChildren().remove(card);
                if (notificationsContainer.getChildren().isEmpty()) {
                    Label noNotifLabel = new Label("No notifications.");
                    noNotifLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 16px;");
                    notificationsContainer.getChildren().add(noNotifLabel);
                }
            }
        });
        
        card.hoverProperty().addListener((obs, oldVal, newVal) -> {
            deleteBtn.setVisible(newVal);
        });
        
        card.getChildren().addAll(content, deleteBtn);
        
        if (!notif.getBoolean("isRead")) {
            card.setStyle(card.getStyle() + "-fx-border-color: #3b82f6; -fx-border-width: 0 0 0 4;");
        }
        
        card.setOnMouseClicked(e -> handleNotificationClick(notif));
        
        return card;
    }

    private void handleNotificationClick(JSONObject notif) {
        String userType = sessionManager.getUserType();
        int referenceId = notif.optInt("referenceId", 0);
        String referenceType = notif.optString("referenceType", null);

        try {
            if (referenceId > 0 && referenceType != null) {
                if ("tuition_post".equals(referenceType) && "Student".equals(userType)) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/StudentPosts.fxml"));
                    Parent root = loader.load();
                    
                    StudentPostsController controller = loader.getController();
                    controller.filterByPostId(referenceId);
                    
                    Stage stage = (Stage) notificationsContainer.getScene().getWindow();
                    Scene scene = new Scene(root);
                    ThemeManager.getInstance().applyTheme(scene);
                    stage.setScene(scene);
                    stage.setTitle("MyHomeTutor - My Posts");
                    return;
                } else if ("application".equals(referenceType) && "Tutor".equals(userType)) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TutorApplications.fxml"));
                    Parent root = loader.load();
                    
                    TutorApplicationsController controller = loader.getController();
                    controller.filterByApplicationId(referenceId);
                    
                    Stage stage = (Stage) notificationsContainer.getScene().getWindow();
                    Scene scene = new Scene(root);
                    ThemeManager.getInstance().applyTheme(scene);
                    stage.setScene(scene);
                    stage.setTitle("MyHomeTutor - My Applications");
                    return;
                }
            }

            String fxmlFile;
            String title;
            
            if ("Student".equals(userType)) {
                fxmlFile = "/fxml/StudentPosts.fxml";
                title = "MyHomeTutor - My Posts";
            } else {
                fxmlFile = "/fxml/TutorApplications.fxml";
                title = "MyHomeTutor - My Applications";
            }
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            
            Stage stage = (Stage) notificationsContainer.getScene().getWindow();
            Scene scene = new Scene(root);
            ThemeManager.getInstance().applyTheme(scene);
            stage.setScene(scene);
            stage.setTitle(title);
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to load page.");
        }
    }

    @FXML
    private void handleBack() {
        try {
            String userType = sessionManager.getUserType();
            String fxmlFile = userType.equals("Student") ? "/fxml/StudentDashboard.fxml" : "/fxml/TutorDashboard.fxml";
            String title = userType.equals("Student") ? "MyHomeTutor - Student Dashboard" : "MyHomeTutor - Tutor Dashboard";
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(root);
            ThemeManager.getInstance().applyTheme(scene);
            stage.setScene(scene);
            stage.setTitle(title);
            
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
