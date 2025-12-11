package com.myhometutor.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class TutorRegisterController {
    
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private PasswordField passwordField;
    @FXML private TextField instituteField;
    @FXML private TextField classField;
    @FXML private ComboBox<String> divisionCombo;
    @FXML private ComboBox<String> districtCombo;
    @FXML private ComboBox<String> areaCombo;
    @FXML private TextField preferredFeeField;
    @FXML private TextField experienceField;
    @FXML private TextArea additionalInfoArea;
    @FXML private Button registerButton;
    @FXML private Button backButton;
    
    @FXML
    private void initialize() {
        populateDivisions();
        setupDivisionListener();
    }
    
    private void populateDivisions() {
        divisionCombo.getItems().addAll(
            "Dhaka",
            "Chittagong",
            "Rajshahi",
            "Khulna",
            "Barisal",
            "Sylhet",
            "Rangpur",
            "Mymensingh"
        );
    }
    
    private void setupDivisionListener() {
        divisionCombo.setOnAction(event -> {
            String selectedDivision = divisionCombo.getValue();
            if (selectedDivision != null) {
                populateDistricts(selectedDivision);
                districtCombo.setDisable(false);
            }
        });
        
        districtCombo.setOnAction(event -> {
            String selectedDistrict = districtCombo.getValue();
            if (selectedDistrict != null) {
                populateAreas(selectedDistrict);
                areaCombo.setDisable(false);
            }
        });
        
        districtCombo.setDisable(true);
        areaCombo.setDisable(true);
    }
    
    private void populateDistricts(String division) {
        districtCombo.getItems().clear();
        areaCombo.getItems().clear();
        
        switch (division) {
            case "Dhaka":
                districtCombo.getItems().addAll("Dhaka", "Gazipur", "Narayanganj", "Tangail", "Munshiganj", "Manikganj");
                break;
            case "Chittagong":
                districtCombo.getItems().addAll("Chittagong", "Cox's Bazar", "Comilla", "Feni", "Noakhali");
                break;
            case "Rajshahi":
                districtCombo.getItems().addAll("Rajshahi", "Bogra", "Pabna", "Natore", "Sirajganj");
                break;
            case "Khulna":
                districtCombo.getItems().addAll("Khulna", "Jessore", "Satkhira", "Bagerhat", "Kushtia");
                break;
            case "Barisal":
                districtCombo.getItems().addAll("Barisal", "Patuakhali", "Bhola", "Pirojpur", "Jhalokati");
                break;
            case "Sylhet":
                districtCombo.getItems().addAll("Sylhet", "Moulvibazar", "Sunamganj", "Habiganj");
                break;
            case "Rangpur":
                districtCombo.getItems().addAll("Rangpur", "Dinajpur", "Kurigram", "Lalmonirhat", "Nilphamari");
                break;
            case "Mymensingh":
                districtCombo.getItems().addAll("Mymensingh", "Jamalpur", "Netrokona", "Sherpur");
                break;
        }
    }
    
    private void populateAreas(String district) {
        areaCombo.getItems().clear();
        
        // Sample areas - you can expand this based on your requirements
        if (district.equals("Dhaka")) {
            areaCombo.getItems().addAll(
                "Dhanmondi", "Gulshan", "Banani", "Mirpur", "Uttara", 
                "Mohammadpur", "Bashundhara", "Badda", "Rampura", "Motijheel"
            );
        } else if (district.equals("Chittagong")) {
            areaCombo.getItems().addAll(
                "Agrabad", "Panchlaish", "Halishahar", "Khulshi", "GEC Circle"
            );
        } else {
            areaCombo.getItems().addAll(
                "Area 1", "Area 2", "Area 3", "Area 4", "Area 5"
            );
        }
    }
    
    @FXML
    private void handleRegister() {
        // Validate all required fields
        if (!validateFields()) {
            return;
        }
        
        // Get all field values
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String password = passwordField.getText();
        String institute = instituteField.getText().trim();
        String tutorClass = classField.getText().trim();
        String division = divisionCombo.getValue();
        String district = districtCombo.getValue();
        String area = areaCombo.getValue();
        String preferredFee = preferredFeeField.getText().trim();
        String experience = experienceField.getText().trim();
        String additionalInfo = additionalInfoArea.getText().trim();
        
        // TODO: Implement database insertion logic here
        System.out.println("Tutor Registration:");
        System.out.println("Name: " + name);
        System.out.println("Email: " + email);
        System.out.println("Phone: " + phone);
        System.out.println("Institute: " + institute);
        System.out.println("Class/Degree: " + tutorClass);
        System.out.println("Preferred Location: " + division + ", " + district + ", " + area);
        System.out.println("Preferred Fee: " + preferredFee);
        System.out.println("Experience: " + experience);
        System.out.println("Additional Info: " + additionalInfo);
        
        // Show success message
        showAlert(Alert.AlertType.INFORMATION, "Registration Successful", 
                "Tutor account created successfully!\n" +
                "You can now login with your credentials.");
        
        // Navigate back to home page
        handleBack();
    }
    
    private boolean validateFields() {
        if (nameField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter your full name.");
            return false;
        }
        
        if (emailField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter your email address.");
            return false;
        }
        
        if (!isValidEmail(emailField.getText().trim())) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter a valid email address.");
            return false;
        }
        
        if (phoneField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter your phone number.");
            return false;
        }
        
        if (passwordField.getText().isEmpty() || passwordField.getText().length() < 6) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Password must be at least 6 characters long.");
            return false;
        }
        
        if (instituteField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter your institute/university name.");
            return false;
        }
        
        if (classField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter your class/degree.");
            return false;
        }
        
        if (divisionCombo.getValue() == null || districtCombo.getValue() == null || areaCombo.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please select complete location (Division, District, Area).");
            return false;
        }
        
        if (preferredFeeField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter your preferred fee.");
            return false;
        }
        
        if (experienceField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter your tuition experience.");
            return false;
        }
        
        return true;
    }
    
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }
    
    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/HomePage.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("MyHomeTutor - Home");
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", 
                    "Failed to load home page: " + e.getMessage());
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
