package com.myhometutor.controller;

import com.myhometutor.database.DatabaseManager;
import com.myhometutor.util.ThemeManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class TutorRegisterController {
    
    @FXML private ImageView profileImageView;
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
    @FXML private Label caseCheckLabel;
    @FXML private Label symbolCheckLabel;
    @FXML private Label matchCheckLabel;

    @FXML private Button verifyEmailButton;
    @FXML private Label emailVerifiedLabel;
    @FXML private HBox otpBox;
    @FXML private TextField otpField;
    
    // College Info
    @FXML private TextField collegeNameField;
    @FXML private ComboBox<String> collegeGroupCombo;
    @FXML private TextField hscYearField;
    
    // University Info
    @FXML private TextField universityNameField;
    @FXML private TextField universityDeptField;
    @FXML private TextField universityYearField;
    @FXML private TextField universitySessionField;
    
    @FXML private ComboBox<String> divisionCombo;
    @FXML private ComboBox<String> districtCombo;
    @FXML private ComboBox<String> areaCombo;
    @FXML private ComboBox<String> classComboBox;
    
    // Tuition Preferences
    @FXML private TextField preferredFeeField;
    @FXML private TextField experienceField;
    @FXML private TextField preferredDayField;
    @FXML private TextField preferredTimeField;
    @FXML private TextField preferredLocationField;
    
    @FXML private TextArea additionalInfoArea;
    @FXML private Button registerButton;
    @FXML private Button backButton;
    
    private DatabaseManager dbManager;
    private ToggleGroup genderGroup;
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;
    private String generatedOTP;
    private boolean isEmailVerified = false;
    private File selectedProfilePicture;
    
    @FXML
    private void handleProfilePictureUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        
        Stage stage = (Stage) registerButton.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        
        if (file != null) {
            selectedProfilePicture = file;
            try {
                Image image = new Image(new FileInputStream(file));
                profileImageView.setImage(image);
                
                // Optional: Add a circular clip
                javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(50, 50, 50);
                profileImageView.setClip(clip);
                
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to load image.");
            }
        }
    }

    @FXML
    private void initialize() {
        populateDivisions();
        setupDivisionListener();
        setupPasswordListener();
        
        genderGroup = new ToggleGroup();
        maleRadio.setToggleGroup(genderGroup);
        femaleRadio.setToggleGroup(genderGroup);
        
        passwordTextField.textProperty().bindBidirectional(passwordField.textProperty());
        confirmPasswordTextField.textProperty().bindBidirectional(confirmPasswordField.textProperty());
        
        collegeGroupCombo.getItems().addAll("Science", "Arts", "Commerce");
        
        classComboBox.getItems().addAll(
            "Class 1", "Class 2", "Class 3", "Class 4", "Class 5", 
            "Class 6", "Class 7", "Class 8", "Class 9", "Class 10", 
            "Inter First Year", "Inter Second Year", "Admission"
        );
        
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
        boolean caseValid = password.matches(".*[a-z].*") && password.matches(".*[A-Z].*");
        boolean symbolValid = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");

        updateValidationLabel(lengthCheckLabel, lengthValid, "Minimum 8 characters");
        updateValidationLabel(alphaCheckLabel, alphaValid, "Contains alphabet");
        updateValidationLabel(numberCheckLabel, numberValid, "Contains number");
        updateValidationLabel(caseCheckLabel, caseValid, "Mixed case (upper & lower)");
        updateValidationLabel(symbolCheckLabel, symbolValid, "Contains symbol");
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
        
        String collegeName = collegeNameField.getText().trim();
        String collegeGroup = collegeGroupCombo.getValue();
        String hscYear = hscYearField.getText().trim();
        
        String universityName = universityNameField.getText().trim();
        String universityDept = universityDeptField.getText().trim();
        String universityYear = universityYearField.getText().trim();
        String universitySession = universitySessionField.getText().trim();
        
        String division = divisionCombo.getValue();
        String district = districtCombo.getValue();
        String area = areaCombo.getValue();
        
        String preferredClass = classComboBox.getValue();
        String preferredFee = preferredFeeField.getText().trim();
        String experience = experienceField.getText().trim();
        String preferredDay = preferredDayField.getText().trim();
        String preferredTime = preferredTimeField.getText().trim();
        String preferredLocation = preferredLocationField.getText().trim();
        
        String additionalInfo = additionalInfoArea.getText().trim();
        
        // Check if username already exists
        String existingStatus = dbManager.getUserStatus(email, "Tutor");
        if (existingStatus != null && !existingStatus.equals("rejected")) {
            showAlert(Alert.AlertType.ERROR, "Registration Error", 
                    "Email already registered. Please use a different email.");
            return;
        }
        
        // Save profile picture if selected
        String profilePicturePath = "";
        if (selectedProfilePicture != null) {
            try {
                File profilePicsDir = new File("profile_pictures");
                if (!profilePicsDir.exists()) {
                    profilePicsDir.mkdirs();
                }
                
                String extension = "";
                int i = selectedProfilePicture.getName().lastIndexOf('.');
                if (i > 0) {
                    extension = selectedProfilePicture.getName().substring(i);
                }
                
                String fileName = "tutor_" + UUID.randomUUID().toString() + extension;
                File destFile = new File(profilePicsDir, fileName);
                
                Files.copy(selectedProfilePicture.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                profilePicturePath = destFile.getAbsolutePath();
                
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        // Create JSON object with tutor data
        JSONObject tutorData = new JSONObject();
        if (!profilePicturePath.isEmpty()) {
            tutorData.put("profilePicture", profilePicturePath);
        }
        tutorData.put("name", name);
        tutorData.put("email", email);
        tutorData.put("phone", phone);
        tutorData.put("gender", maleRadio.isSelected() ? "Male" : "Female");
        
        tutorData.put("collegeName", collegeName);
        tutorData.put("collegeGroup", collegeGroup);
        tutorData.put("hscYear", hscYear);
        
        tutorData.put("universityName", universityName);
        tutorData.put("universityDept", universityDept);
        tutorData.put("universityYear", universityYear);
        tutorData.put("universitySession", universitySession);
        
        tutorData.put("division", division);
        tutorData.put("district", district);
        tutorData.put("area", area);
        
        tutorData.put("preferredClass", preferredClass);
        tutorData.put("preferredFee", preferredFee);
        tutorData.put("experience", experience);
        tutorData.put("preferredDay", preferredDay);
        tutorData.put("preferredTime", preferredTime);
        tutorData.put("preferredLocation", preferredLocation);
        
        tutorData.put("additionalInfo", additionalInfo);
        tutorData.put("password", password); // Temporarily store password to pass to next screen
        
        navigateToDocumentUpload(tutorData, "Tutor");
    }

    private void navigateToDocumentUpload(JSONObject data, String userType) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DocumentUpload.fxml"));
            Parent root = loader.load();
            
            DocumentUploadController controller = loader.getController();
            controller.setRegistrationData(data, userType);
            
            Stage stage = (Stage) registerButton.getScene().getWindow();
            double width = stage.getWidth();
            double height = stage.getHeight();
            
            Scene scene = new Scene(root, width, height);
            ThemeManager.getInstance().applyTheme(scene);
            
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to load document upload page.");
        }
    }
    
    @FXML
    private void handleVerifyEmail() {
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please enter an email address.");
            return;
        }
        if (!isValidEmail(email)) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please enter a valid email address.");
            return;
        }

        generatedOTP = com.myhometutor.util.EmailService.generateOTP();
        boolean sent = com.myhometutor.util.EmailService.sendOTP(email, generatedOTP);

        if (sent) {
            otpBox.setVisible(true);
            otpBox.setManaged(true);
            verifyEmailButton.setDisable(true);
            emailField.setDisable(true);
            showAlert(Alert.AlertType.INFORMATION, "Success", "OTP sent to your email. Please check and enter it.");
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to send OTP. Please check your internet connection or email address.");
        }
    }

    @FXML
    private void handleConfirmOTP() {
        String enteredOTP = otpField.getText().trim();
        if (enteredOTP.equals(generatedOTP)) {
            isEmailVerified = true;
            otpBox.setVisible(false);
            otpBox.setManaged(false);
            emailVerifiedLabel.setVisible(true);
            verifyEmailButton.setVisible(false);
            verifyEmailButton.setManaged(false);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Email verified successfully!");
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid OTP. Please try again.");
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

        if (!isEmailVerified) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please verify your email address.");
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

        if (password.length() < 8 || !password.matches(".*[a-zA-Z].*") || !password.matches(".*\\d.*") || 
            !password.matches(".*[a-z].*") || !password.matches(".*[A-Z].*") || 
            !password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Password does not meet all requirements.");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Password doesn't match.");
            return false;
        }

        if (collegeNameField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter your college name.");
            return false;
        }
        
        if (collegeGroupCombo.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please select your college group.");
            return false;
        }
        
        if (hscYearField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter your HSC passing year.");
            return false;
        }
        
        if (universityNameField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter your university name.");
            return false;
        }
        
        if (universityDeptField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter your university department.");
            return false;
        }
        
        if (universityYearField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter your university year.");
            return false;
        }
        
        if (universitySessionField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter your university session.");
            return false;
        }
        
        if (divisionCombo.getValue() == null || districtCombo.getValue() == null || areaCombo.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please select complete location (Division, District, Area).");
            return false;
        }
        
        if (classComboBox.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please select your preferred class.");
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
        
        if (preferredDayField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter your preferred days.");
            return false;
        }
        
        if (preferredTimeField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter your preferred time.");
            return false;
        }
        
        if (preferredLocationField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter your preferred location details.");
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
