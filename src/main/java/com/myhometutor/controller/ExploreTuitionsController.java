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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class ExploreTuitionsController {

    @FXML private VBox postsContainer;
    @FXML private Button backButton;
    @FXML private Button refreshButton;
    @FXML private ToggleButton themeToggle;

    private DatabaseManager dbManager;

    @FXML
    private void initialize() {
        dbManager = DatabaseManager.getInstance();
        
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
        JSONArray posts = dbManager.getAllTuitionPosts();

        if (posts.length() == 0) {
            Label noPostsLabel = new Label("No tuition posts available at the moment.");
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
        
        // Header: Subject and Class
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
        
        // Details Grid
        VBox details = new VBox(5);
        details.getChildren().add(createDetailRow("Group:", post.optString("group", "-")));
        details.getChildren().add(createDetailRow("Type:", post.optString("type", "-")));
        details.getChildren().add(createDetailRow("Days:", post.optString("days", "-")));
        details.getChildren().add(createDetailRow("Timing:", post.optString("timing", "-")));
        details.getChildren().add(createDetailRow("Salary:", post.optString("salary", "-") + " BDT"));
        details.getChildren().add(createDetailRow("Location:", post.optString("address", "-")));
        
        // Footer: Student Name and Apply Button
        HBox footer = new HBox(15);
        footer.setPadding(new Insets(10, 0, 0, 0));
        
        Label studentLabel = new Label("Posted by: " + post.optString("studentName", "Unknown"));
        studentLabel.getStyleClass().add("post-footer-text");
        
        Region footerSpacer = new Region();
        HBox.setHgrow(footerSpacer, Priority.ALWAYS);
        
        Button applyButton = new Button("Apply Now");
        applyButton.getStyleClass().add("primary-button");
        applyButton.setOnAction(e -> handleApply(post));
        
        footer.getChildren().addAll(studentLabel, footerSpacer, applyButton);
        
        card.getChildren().addAll(header, new Separator(), details, new Separator(), footer);
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

    private void handleApply(JSONObject post) {
        SessionManager sessionManager = SessionManager.getInstance();
        int tutorId = sessionManager.getUserId();
        int postId = post.getInt("id");
        int studentId = post.getInt("studentId");
        
        boolean success = dbManager.applyForTuition(tutorId, postId);
        
        if (success) {
            // Notify the student
            String tutorName = sessionManager.getCurrentUser().getString("name");
            String message = "Tutor " + tutorName + " has applied for your tuition post (ID: " + postId + ").";
            dbManager.createNotification(studentId, "Student", message);
            
            showAlert(Alert.AlertType.INFORMATION, "Success", "Application submitted successfully!");
        } else {
            showAlert(Alert.AlertType.WARNING, "Application Failed", "You have already applied for this post or an error occurred.");
        }
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
