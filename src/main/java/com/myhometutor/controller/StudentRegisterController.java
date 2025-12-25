package com.myhometutor.controller;

import com.myhometutor.database.DatabaseManager;
import com.myhometutor.util.ThemeManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.io.IOException;

public class StudentRegisterController {
    
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private RadioButton maleRadio;
    @FXML private RadioButton femaleRadio;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordTextField;
    @FXML private Button togglePasswordBtn;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField confirmPasswordTextField;
    @FXML private Button toggleConfirmPasswordBtn;
    @FXML private Label lengthCheckLabel;
    @FXML private Label alphaCheckLabel;
    @FXML private Label numberCheckLabel;
    @FXML private Label matchCheckLabel;
    @FXML private TextField instituteField;
    @FXML private TextField classField;
    @FXML private ComboBox<String> groupCombo;
    @FXML private ComboBox<String> divisionCombo;
    @FXML private ComboBox<String> districtCombo;
    @FXML private ComboBox<String> areaCombo;
    @FXML private TextArea requirementsArea;
    @FXML private Button registerButton;
    @FXML private Button backButton;
    
    private DatabaseManager dbManager;
    private ToggleGroup genderGroup;
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;
    
    @FXML
    private void initialize() {
        populateDivisions();
        populateGroups();
        setupDivisionListener();
        setupPasswordListener();
        
        genderGroup = new ToggleGroup();
        maleRadio.setToggleGroup(genderGroup);
        femaleRadio.setToggleGroup(genderGroup);
        
        passwordTextField.textProperty().bindBidirectional(passwordField.textProperty());
        confirmPasswordTextField.textProperty().bindBidirectional(confirmPasswordField.textProperty());
        
        dbManager = DatabaseManager.getInstance();
    }

    @FXML
    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;
        if (isPasswordVisible) {
            passwordTextField.setVisible(true);
            passwordField.setVisible(false);
            togglePasswordBtn.setText("🙈");
        } else {
            passwordTextField.setVisible(false);
            passwordField.setVisible(true);
            togglePasswordBtn.setText("👁");
        }
    }

    @FXML
    private void toggleConfirmPasswordVisibility() {
        isConfirmPasswordVisible = !isConfirmPasswordVisible;
        if (isConfirmPasswordVisible) {
            confirmPasswordTextField.setVisible(true);
            confirmPasswordField.setVisible(false);
            toggleConfirmPasswordBtn.setText("🙈");
        } else {
            confirmPasswordTextField.setVisible(false);
            confirmPasswordField.setVisible(true);
            toggleConfirmPasswordBtn.setText("👁");
        }
    }

    private void setupPasswordListener() {
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            updatePasswordValidation(newValue);
            checkPasswordMatch();
        });
        
        confirmPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            checkPasswordMatch();
        });
    }

    private void checkPasswordMatch() {
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        
        if (confirmPassword.isEmpty()) {
            matchCheckLabel.setText("");
            return;
        }
        
        if (password.equals(confirmPassword)) {
            matchCheckLabel.setText("✅ Password matched");
            matchCheckLabel.setStyle("-fx-text-fill: #22c55e; -fx-font-size: 11px;");
        } else {
            matchCheckLabel.setText("❌ Password doesn't match");
            matchCheckLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px;");
        }
    }

    private void updatePasswordValidation(String password) {
        boolean lengthValid = password.length() >= 8;
        boolean alphaValid = password.matches(".*[a-zA-Z].*");
        boolean numberValid = password.matches(".*\\d.*");

        updateValidationLabel(lengthCheckLabel, lengthValid, "Minimum 8 characters");
        updateValidationLabel(alphaCheckLabel, alphaValid, "Contains alphabet");
        updateValidationLabel(numberCheckLabel, numberValid, "Contains number");
    }

    private void updateValidationLabel(Label label, boolean isValid, String text) {
        if (isValid) {
            label.setText("✅ " + text);
            label.setStyle("-fx-text-fill: #22c55e; -fx-font-size: 11px;");
        } else {
            label.setText("❌ " + text);
            label.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px;");
        }
    }

    private void populateGroups() {
        groupCombo.getItems().addAll("Science", "Commerce", "Humanities", "Dakhil");
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
                districtCombo.getItems().addAll("Khulna","magura", "Jashore", "Satkhira", "Bagerhat", "Kushtia");
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
        
        if (district.equals("Dhaka")) {
            areaCombo.getItems().addAll(
                "Dhanmondi", "Gulshan", "Banani", "Mirpur", "Uttara", 
                "Mohammadpur", "Bashundhara", "Badda", "Rampura", "Motijheel"
            );
        }  else {
            areaCombo.getItems().addAll(
                "Area 1", "Area 2", "Area 3", "Area 4", "Area 5"
            );
        }
    }
    
    @FXML
    private void handleRegister() {
        if (!validateFields()) {
            return;
        }
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String password = passwordField.getText();
        String institute = instituteField.getText().trim();
        String studentClass = classField.getText().trim();
        String group = groupCombo.getValue();
        String division = divisionCombo.getValue();
        String district = districtCombo.getValue();
        String area = areaCombo.getValue();
        String additionalInfo = requirementsArea.getText().trim();
        
        // Check if username already exists
        if (dbManager.usernameExists(email, "Student")) {
            showAlert(Alert.AlertType.ERROR, "Registration Error", 
                    "This email is already registered.\nPlease use a different email or login.");
            return;
        }
        
        // Create JSON object with student data
        JSONObject studentData = new JSONObject();
        studentData.put("name", name);
        studentData.put("email", email);
        studentData.put("phone", phone);
        studentData.put("gender", maleRadio.isSelected() ? "Male" : "Female");
        studentData.put("institute", institute);
        studentData.put("class", studentClass);
        studentData.put("group", group);
        studentData.put("division", division);
        studentData.put("district", district);
        studentData.put("area", area);
        studentData.put("additionalInfo", additionalInfo);
        
        // Save to database
        boolean success = dbManager.registerStudent(email, password, studentData);
        
        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Registration Successful", 
                    "Student account created successfully!\n" +
                    "You can now login with your credentials.");
            handleBack();
        } else {
            showAlert(Alert.AlertType.ERROR, "Registration Error", 
                    "Failed to create account.\nPlease try again.");
        }
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
        
        if (genderGroup.getSelectedToggle() == null) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please select your gender.");
            return false;
        }
        
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter a password.");
            return false;
        }

        if (password.length() < 8 || !password.matches(".*[a-zA-Z].*") || !password.matches(".*\\d.*")) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Password does not meet all requirements.");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Password doesn't match.");
            return false;
        }
        
        if (instituteField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter your institute name.");
            return false;
        }
        
        if (classField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter your class/grade.");
            return false;
        }
        
        if (groupCombo.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please select your group.");
            return false;
        }
        
        if (divisionCombo.getValue() == null || districtCombo.getValue() == null || areaCombo.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please select complete location (Division, District, Area).");
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
            double width = stage.getWidth();
            double height = stage.getHeight();
            boolean maximized = stage.isMaximized();
            boolean fullScreen = stage.isFullScreen();

            Scene scene = new Scene(root);
            ThemeManager.getInstance().applyTheme(scene);
            stage.setScene(scene);
            stage.setTitle("MyHomeTutor - Home");
            
            // Restore dimensions
            stage.setWidth(width);
            stage.setHeight(height);
            stage.setMaximized(maximized);
            stage.setFullScreen(fullScreen);
            
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
