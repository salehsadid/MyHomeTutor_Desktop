package com.myhometutor.controller;

import com.myhometutor.database.DatabaseManager;
import com.myhometutor.model.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.json.JSONObject;
import java.util.Optional;

public class EditProfileDialogController {
    
    @FXML private TextField nameField;
    @FXML private TextField phoneField;
    
    // Student Fields
    @FXML private VBox studentFields;
    @FXML private TextField instituteField;
    @FXML private TextField classField;
    @FXML private ComboBox<String> groupCombo;
    
    // Tutor Fields
    @FXML private VBox tutorFields;
    @FXML private TextField collegeNameField;
    @FXML private ComboBox<String> collegeGroupCombo;
    @FXML private TextField hscYearField;
    @FXML private TextField universityNameField;
    @FXML private TextField universityDeptField;
    @FXML private TextField universityYearField;
    @FXML private TextField universitySessionField;
    @FXML private TextField preferredFeeField;
    @FXML private TextField experienceField;
    @FXML private TextField preferredDayField;
    @FXML private TextField preferredTimeField;
    @FXML private TextField preferredLocationField;
    @FXML private TextArea additionalInfoArea;
    
    // Location Fields
    @FXML private ComboBox<String> divisionCombo;
    @FXML private ComboBox<String> districtCombo;
    @FXML private ComboBox<String> areaCombo;
    
    private SessionManager sessionManager;
    private DatabaseManager dbManager;
    private String userType;
    private Object dashboardController;
    
    @FXML
    private void initialize() {
        sessionManager = SessionManager.getInstance();
        dbManager = DatabaseManager.getInstance();
        setupLocationListeners();
        setupCombos();
    }
    
    private void setupCombos() {
        if (groupCombo != null) {
            groupCombo.getItems().addAll("Science", "Commerce", "Arts");
        }
        if (collegeGroupCombo != null) {
            collegeGroupCombo.getItems().addAll("Science", "Commerce", "Arts");
        }
    }
    
    public void setUserType(String type) {
        this.userType = type;
        
        if ("Student".equals(type)) {
            studentFields.setVisible(true);
            studentFields.setManaged(true);
            tutorFields.setVisible(false);
            tutorFields.setManaged(false);
        } else {
            studentFields.setVisible(false);
            studentFields.setManaged(false);
            tutorFields.setVisible(true);
            tutorFields.setManaged(true);
        }
        
        loadCurrentData();
    }
    
    public void setDashboardController(Object controller) {
        this.dashboardController = controller;
    }
    
    private void loadCurrentData() {
        JSONObject userData = sessionManager.getCurrentUser();
        
        if (userData != null) {
            if (userData.has("name")) nameField.setText(userData.getString("name"));
            if (userData.has("phone")) phoneField.setText(userData.getString("phone"));
            if (userData.has("division")) divisionCombo.setValue(userData.getString("division"));
            if (userData.has("district")) districtCombo.setValue(userData.getString("district"));
            if (userData.has("area")) areaCombo.setValue(userData.getString("area"));
            if (userData.has("additionalInfo")) additionalInfoArea.setText(userData.getString("additionalInfo"));
            
            if ("Student".equals(userType)) {
                if (userData.has("institute")) instituteField.setText(userData.getString("institute"));
                if (userData.has("class")) classField.setText(userData.getString("class"));
                if (userData.has("group")) groupCombo.setValue(userData.getString("group"));
            } else {
                if (userData.has("college")) collegeNameField.setText(userData.getString("college"));
                if (userData.has("collegeGroup")) collegeGroupCombo.setValue(userData.getString("collegeGroup"));
                if (userData.has("hscYear")) hscYearField.setText(userData.getString("hscYear"));
                if (userData.has("university")) universityNameField.setText(userData.getString("university"));
                if (userData.has("department")) universityDeptField.setText(userData.getString("department"));
                if (userData.has("year")) universityYearField.setText(userData.getString("year"));
                if (userData.has("session")) universitySessionField.setText(userData.getString("session"));
                if (userData.has("preferredFee")) preferredFeeField.setText(userData.getString("preferredFee"));
                if (userData.has("experience")) experienceField.setText(userData.getString("experience"));
                if (userData.has("preferredDays")) preferredDayField.setText(userData.getString("preferredDays"));
                if (userData.has("preferredTime")) preferredTimeField.setText(userData.getString("preferredTime"));
                if (userData.has("preferredLocation")) preferredLocationField.setText(userData.getString("preferredLocation"));
            }
        }
    }
    
    private void setupLocationListeners() {
        divisionCombo.getItems().addAll("Dhaka", "Chittagong", "Rajshahi", "Khulna", "Barisal", "Sylhet", "Rangpur", "Mymensingh");
        
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
                districtCombo.getItems().addAll("Khulna", "Jashore", "Satkhira", "Bagerhat", "Kushtia");
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
    private void handleChangePassword() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Change Password");
        dialog.setHeaderText("Enter your new password");
        
        ButtonType changeButtonType = new ButtonType("Change", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(changeButtonType, ButtonType.CANCEL);
        
        VBox content = new VBox(10);
        PasswordField currentPass = new PasswordField();
        currentPass.setPromptText("Current Password");
        PasswordField newPass = new PasswordField();
        newPass.setPromptText("New Password");
        PasswordField confirmPass = new PasswordField();
        confirmPass.setPromptText("Confirm New Password");
        
        content.getChildren().addAll(
            new Label("Current Password:"), currentPass,
            new Label("New Password:"), newPass,
            new Label("Confirm Password:"), confirmPass
        );
        
        dialog.getDialogPane().setContent(content);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == changeButtonType) {
                if (newPass.getText().equals(confirmPass.getText())) {
                    if (dbManager.updatePassword(sessionManager.getUserId(), userType, 
                            currentPass.getText(), newPass.getText())) {
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Password changed successfully!");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", "Incorrect current password or update failed.");
                    }
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "New passwords do not match.");
                }
            }
            return null;
        });
        
        dialog.showAndWait();
    }
    
    @FXML
    private void handleSave() {
        if (!validateFields()) {
            return;
        }
        
        JSONObject userData = sessionManager.getCurrentUser();
        
        userData.put("name", nameField.getText().trim());
        userData.put("phone", phoneField.getText().trim());
        userData.put("division", divisionCombo.getValue());
        userData.put("district", districtCombo.getValue());
        userData.put("area", areaCombo.getValue());
        userData.put("additionalInfo", additionalInfoArea.getText().trim());
        
        if ("Student".equals(userType)) {
            userData.put("institute", instituteField.getText().trim());
            userData.put("class", classField.getText().trim());
            userData.put("group", groupCombo.getValue());
        } else {
            userData.put("college", collegeNameField.getText().trim());
            userData.put("collegeGroup", collegeGroupCombo.getValue());
            userData.put("hscYear", hscYearField.getText().trim());
            userData.put("university", universityNameField.getText().trim());
            userData.put("department", universityDeptField.getText().trim());
            userData.put("year", universityYearField.getText().trim());
            userData.put("session", universitySessionField.getText().trim());
            userData.put("preferredFee", preferredFeeField.getText().trim());
            userData.put("experience", experienceField.getText().trim());
            userData.put("preferredDays", preferredDayField.getText().trim());
            userData.put("preferredTime", preferredTimeField.getText().trim());
            userData.put("preferredLocation", preferredLocationField.getText().trim());
        }
        
        int userId = sessionManager.getUserId();
        boolean success;
        
        if ("Student".equals(userType)) {
            success = dbManager.updateStudent(userId, userData);
        } else {
            success = dbManager.updateTutor(userId, userData);
        }
        
        if (success) {
            sessionManager.setCurrentUser(userData, userType);
            
            if (dashboardController instanceof StudentDashboardController) {
                ((StudentDashboardController) dashboardController).refreshProfile();
            } else if (dashboardController instanceof TutorDashboardController) {
                ((TutorDashboardController) dashboardController).refreshProfile();
            }
            
            showAlert(Alert.AlertType.INFORMATION, "Success", 
                    "Profile updated successfully!");
            
            closeDialog();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", 
                    "Failed to update profile. Please try again.");
        }
    }
    
    private boolean validateFields() {
        if (nameField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter your name.");
            return false;
        }
        
        if (phoneField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter your phone number.");
            return false;
        }
        
        if (divisionCombo.getValue() == null || districtCombo.getValue() == null || areaCombo.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please select complete location.");
            return false;
        }
        
        if ("Student".equals(userType)) {
            if (instituteField.getText().trim().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter your institute.");
                return false;
            }
            if (classField.getText().trim().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter your class.");
                return false;
            }
            if (groupCombo.getValue() == null) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Please select your group.");
                return false;
            }
        } else {
            // Add validation for tutor fields if necessary
        }
        
        return true;
    }
    
    @FXML
    private void handleCancel() {
        closeDialog();
    }
    
    private void closeDialog() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
