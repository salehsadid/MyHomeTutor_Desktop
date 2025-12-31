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
import javafx.scene.control.ComboBox;
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
    
    @FXML private ComboBox<String> locationFilter;
    @FXML private ComboBox<String> salaryFilter;
    @FXML private ComboBox<String> genderFilter;
    @FXML private ComboBox<String> typeFilter;
    @FXML private ComboBox<String> classFilter;
    @FXML private ComboBox<String> subjectFilter;

    private DatabaseManager dbManager;

    @FXML
    private void initialize() {
        dbManager = DatabaseManager.getInstance();
        
        ThemeManager themeManager = ThemeManager.getInstance();
        if (themeToggle != null) {
            themeToggle.setSelected(themeManager.isDarkMode());
        }
        
        setupFilters();
        loadPosts();
    }
    
    private void setupFilters() {
        locationFilter.getItems().addAll("Dhaka", "Chittagong", "Rajshahi", "Khulna", "Barisal", "Sylhet", "Rangpur", "Mymensingh");
        salaryFilter.getItems().addAll("Any", "< 3000", "3000 - 5000", "5000 - 8000", "> 8000");
        genderFilter.getItems().addAll("Any", "Male", "Female");
        typeFilter.getItems().addAll("Any", "Online", "Offline");
        classFilter.getItems().addAll("Any", "Class 1", "Class 2", "Class 3", "Class 4", "Class 5", "Class 6", "Class 7", "Class 8", "Class 9", "Class 10", "HSC", "O Level", "A Level");
        subjectFilter.getItems().addAll("Any", "Bangla", "English", "Math", "Physics", "Chemistry", "Biology", "ICT", "Accounting", "Economics");
        
        setupFilterListener(salaryFilter);
        setupFilterListener(genderFilter);
        setupFilterListener(typeFilter);
        setupFilterListener(classFilter);
        setupFilterListener(subjectFilter);
    }
    
    private void setupFilterListener(ComboBox<String> combo) {
        combo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if ("Any".equals(newVal)) {
                combo.setValue(null);
            }
        });
    }
    
    @FXML
    private void applyFilters() {
        loadPosts();
    }

    @FXML
    private void clearFilters() {
        locationFilter.setValue(null);
        salaryFilter.setValue(null);
        genderFilter.setValue(null);
        typeFilter.setValue(null);
        classFilter.setValue(null);
        subjectFilter.setValue(null);
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
            
            if (matchesFilters(post)) {
                VBox postCard = createPostCard(post);
                postsContainer.getChildren().add(postCard);
            }
        }
        
        if (postsContainer.getChildren().isEmpty()) {
             Label noPostsLabel = new Label("No tuition posts match your filters.");
             noPostsLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 16px;");
             postsContainer.getChildren().add(noPostsLabel);
        }
    }
    
    private boolean matchesFilters(JSONObject post) {
        // Location
        String location = locationFilter.getValue();
        if (location != null && !location.isEmpty()) {
             String postDistrict = post.optString("district", "");
             String postArea = post.optString("area", "");
             // Also check address field if district/area not in root (it might be in 'address' string)
             // But DatabaseManager joins with student data, so district/area might be in student data?
             // Wait, getAllTuitionPosts puts student data into post object?
             // No, it puts studentName, studentPhone, studentGender.
             // It does NOT put student address/district.
             // However, the post itself has 'address' field.
             // And the student has 'district'/'area'.
             // Let's assume filtering by the text in 'address' or 'district' if available.
             // Actually, getAllTuitionPosts only selects specific fields.
             // I should update getAllTuitionPosts to include student location if I want to filter by it.
             // Or rely on post 'address'.
             String address = post.optString("address", "");
             if (!address.toLowerCase().contains(location.toLowerCase())) {
                 return false;
             }
        }

        // Salary
        String salaryRange = salaryFilter.getValue();
        if (salaryRange != null && !salaryRange.equals("Any")) {
            double salary = parseSalary(post.optString("salary", "0"));
            if (salaryRange.equals("< 3000") && salary >= 3000) return false;
            if (salaryRange.equals("3000 - 5000") && (salary < 3000 || salary > 5000)) return false;
            if (salaryRange.equals("5000 - 8000") && (salary < 5000 || salary > 8000)) return false;
            if (salaryRange.equals("> 8000") && salary <= 8000) return false;
        }

        // Gender
        String gender = genderFilter.getValue();
        if (gender != null && !gender.equals("Any")) {
            if (!post.optString("studentGender", "").equalsIgnoreCase(gender)) {
                return false;
            }
        }

        // Type
        String type = typeFilter.getValue();
        if (type != null && !type.equals("Any")) {
            if (!post.optString("type", "").equalsIgnoreCase(type)) {
                return false;
            }
        }

        // Class
        String cls = classFilter.getValue();
        if (cls != null && !cls.equals("Any")) {
            if (!post.optString("class", "").equalsIgnoreCase(cls)) {
                return false;
            }
        }

        // Subject
        String subject = subjectFilter.getValue();
        if (subject != null && !subject.equals("Any")) {
            if (!post.optString("subject", "").toLowerCase().contains(subject.toLowerCase())) {
                return false;
            }
        }
        
        return true;
    }
    
    private double parseSalary(String salaryStr) {
        try {
            return Double.parseDouble(salaryStr.replaceAll("[^0-9.]", ""));
        } catch (NumberFormatException e) {
            return 0;
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
        
        String postStatus = post.optString("status", "active");
        Label statusLabel = new Label();
        if ("assigned".equalsIgnoreCase(postStatus)) {
            statusLabel.setText("Unavailable");
            statusLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-font-size: 12px;");
        } else {
            statusLabel.setText("Available");
            statusLabel.setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold; -fx-font-size: 12px;");
        }
        
        header.getChildren().addAll(subjectLabel, classLabel, spacer, statusLabel, new Label(" | "), dateLabel);
        
        // Details Grid
        VBox details = new VBox(5);
        details.getChildren().add(createDetailRow("Group:", post.optString("group", "-")));
        details.getChildren().add(createDetailRow("Gender:", post.optString("studentGender", "Unknown")));
        details.getChildren().add(createDetailRow("Type:", post.optString("type", "Offline")));
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
        
        SessionManager sessionManager = SessionManager.getInstance();
        int tutorId = sessionManager.getUserId();
        int postId = post.getInt("id");
        String status = dbManager.getApplicationStatus(tutorId, postId);
        
        if (status == null) {
            Button applyButton = new Button("Apply Now");
            applyButton.getStyleClass().add("primary-button");
            applyButton.setOnAction(e -> handleApply(post));
            footer.getChildren().addAll(studentLabel, footerSpacer, applyButton);
        } else {
            Label appStatusLabel = new Label(status.toUpperCase());
            if ("accepted".equalsIgnoreCase(status)) {
                appStatusLabel.setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold;");
            } else if ("pending".equalsIgnoreCase(status)) {
                appStatusLabel.setStyle("-fx-text-fill: #eab308; -fx-font-weight: bold;");
            } else {
                appStatusLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
            }
            footer.getChildren().addAll(studentLabel, footerSpacer, appStatusLabel);
        }
        
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
            dbManager.createNotification(studentId, "Student", message, postId, "tuition_post");
            
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
