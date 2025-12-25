package com.myhometutor.controller;

import com.myhometutor.database.DatabaseManager;
import com.myhometutor.model.SessionManager;
import com.myhometutor.util.ThemeManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.io.IOException;

public class PostTuitionController {

    @FXML private ComboBox<String> subjectCombo;
    @FXML private ComboBox<String> classCombo;
    @FXML private ComboBox<String> typeCombo;
    @FXML private ComboBox<String> groupCombo;
    @FXML private TextField daysField;
    @FXML private TextField hoursField;
    @FXML private TextField timingField;
    @FXML private TextField salaryField;
    @FXML private VBox locationContainer;
    @FXML private ComboBox<String> divisionCombo;
    @FXML private ComboBox<String> districtCombo;
    @FXML private ComboBox<String> thanaCombo;
    @FXML private TextField areaField;
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
        
        subjectCombo.getItems().addAll("Bangla", "English", "Math", "Physics", "Chemistry", "Biology", "ICT", "Accounting", "Economics", "Finance", "History", "Islamic Studies", "All Subjects");
        classCombo.getItems().addAll("Class 1", "Class 2", "Class 3", "Class 4", "Class 5", "Class 6", "Class 7", "Class 8", "Class 9", "Class 10", "HSC 1st Year", "HSC 2nd Year", "O Level", "A Level");
        
        typeCombo.getItems().addAll("Online", "Offline");
        groupCombo.getItems().addAll("Science", "Commerce", "Humanities", "Dakhil", "General");
        
        setupLocationLogic();
    }
    
    private void setupLocationLogic() {
        // Initial state
        locationContainer.setVisible(false);
        locationContainer.setManaged(false);
        
        typeCombo.setOnAction(e -> {
            boolean isOffline = "Offline".equals(typeCombo.getValue());
            locationContainer.setVisible(isOffline);
            locationContainer.setManaged(isOffline);
        });
        
        populateDivisions();
        
        divisionCombo.setOnAction(e -> {
            populateDistricts(divisionCombo.getValue());
            districtCombo.setDisable(false);
        });
        
        districtCombo.setOnAction(e -> {
            populateThanas(districtCombo.getValue());
            thanaCombo.setDisable(false);
        });
    }
    
    private void populateDivisions() {
        divisionCombo.getItems().addAll("Dhaka", "Chittagong", "Rajshahi", "Khulna", "Barisal", "Sylhet", "Rangpur", "Mymensingh");
    }
    
    private void populateDistricts(String division) {
        districtCombo.getItems().clear();
        thanaCombo.getItems().clear();
        if (division == null) return;
        
        switch (division) {
            case "Dhaka": districtCombo.getItems().addAll("Dhaka", "Gazipur", "Narayanganj", "Tangail", "Munshiganj", "Manikganj"); break;
            case "Chittagong": districtCombo.getItems().addAll("Chittagong", "Cox's Bazar", "Comilla", "Feni", "Noakhali"); break;
            case "Rajshahi": districtCombo.getItems().addAll("Rajshahi", "Bogra", "Pabna", "Natore", "Sirajganj"); break;
            case "Khulna": districtCombo.getItems().addAll("Khulna", "Jashore", "Satkhira", "Bagerhat", "Kushtia"); break;
            case "Barisal": districtCombo.getItems().addAll("Barisal", "Patuakhali", "Bhola", "Pirojpur", "Jhalokati"); break;
            case "Sylhet": districtCombo.getItems().addAll("Sylhet", "Moulvibazar", "Sunamganj", "Habiganj"); break;
            case "Rangpur": districtCombo.getItems().addAll("Rangpur", "Dinajpur", "Kurigram", "Lalmonirhat", "Nilphamari"); break;
            case "Mymensingh": districtCombo.getItems().addAll("Mymensingh", "Jamalpur", "Netrokona", "Sherpur"); break;
        }
    }
    
    private void populateThanas(String district) {
        thanaCombo.getItems().clear();
        if (district == null) return;
        
        if (district.equals("Dhaka")) {
            thanaCombo.getItems().addAll("Dhanmondi", "Gulshan", "Banani", "Mirpur", "Uttara", "Mohammadpur", "Bashundhara", "Badda", "Rampura", "Motijheel");
        } else if (district.equals("Chittagong")) {
            thanaCombo.getItems().addAll("Agrabad", "Panchlaish", "Halishahar", "Khulshi", "GEC Circle");
        } else {
            thanaCombo.getItems().addAll("Sadar", "Thana 1", "Thana 2");
        }
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
        postData.put("subject", subjectCombo.getValue());
        postData.put("class", classCombo.getValue());
        postData.put("type", typeCombo.getValue());
        postData.put("group", groupCombo.getValue());
        postData.put("days", daysField.getText().trim());
        postData.put("hours", hoursField.getText().trim());
        postData.put("timing", timingField.getText().trim());
        postData.put("salary", salaryField.getText().trim());
        postData.put("additional", additionalArea.getText().trim());
        
        if ("Offline".equals(typeCombo.getValue())) {
            String fullAddress = String.format("%s, %s, %s, %s\n%s", 
                areaField.getText().trim(),
                thanaCombo.getValue(),
                districtCombo.getValue(),
                divisionCombo.getValue(),
                addressArea.getText().trim()
            );
            postData.put("address", fullAddress);
            postData.put("division", divisionCombo.getValue());
            postData.put("district", districtCombo.getValue());
            postData.put("thana", thanaCombo.getValue());
            postData.put("area", areaField.getText().trim());
        } else {
            postData.put("address", "Online");
        }

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
        if (subjectCombo.getValue() == null || 
            classCombo.getValue() == null ||
            typeCombo.getValue() == null ||
            groupCombo.getValue() == null ||
            daysField.getText().trim().isEmpty() ||
            hoursField.getText().trim().isEmpty() ||
            timingField.getText().trim().isEmpty() ||
            salaryField.getText().trim().isEmpty()) {
            
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please fill all required fields.");
            return false;
        }
        
        if ("Offline".equals(typeCombo.getValue())) {
            if (divisionCombo.getValue() == null ||
                districtCombo.getValue() == null ||
                thanaCombo.getValue() == null ||
                areaField.getText().trim().isEmpty() ||
                addressArea.getText().trim().isEmpty()) {
                
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Please fill all location details for offline tuition.");
                return false;
            }
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
