package com.myhometutor.controller;

import com.myhometutor.util.ThemeManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.io.File;

public class ViewStudentProfileController {

    @FXML private ImageView profileImageView;
    @FXML private Label userNameLabel;
    @FXML private Label userTypeLabel;
    
    @FXML private Label instituteLabel;
    @FXML private Label classLabel;
    @FXML private Label groupLabel;
    
    @FXML private Label divisionLabel;
    @FXML private Label districtLabel;
    @FXML private Label areaLabel;
    
    @FXML private Label genderLabel;
    
    @FXML private javafx.scene.layout.VBox contactDetailsBox;
    @FXML private Label emailLabel;
    @FXML private Label phoneLabel;
    
    @FXML private Label additionalInfoLabel;
    
    @FXML private Button closeButton;

    @FXML
    private void initialize() {
        // Make profile image circular
        profileImageView.setPreserveRatio(false);
        Circle clip = new Circle(
            75, // radius (150/2)
            75, // centerX
            75  // centerY
        );
        profileImageView.setClip(clip);
    }

    public void setStudentData(JSONObject studentData) {
        if (studentData != null) {
            // Set user name
            if (studentData.has("name")) {
                userNameLabel.setText(studentData.getString("name").toUpperCase());
            }
            
            // Set educational details
            if (studentData.has("institute")) {
                instituteLabel.setText(studentData.getString("institute"));
            }
            if (studentData.has("class")) {
                classLabel.setText(studentData.getString("class"));
            }
            if (studentData.has("group")) {
                groupLabel.setText(studentData.getString("group"));
            } else if (studentData.has("subject")) {
                groupLabel.setText(studentData.getString("subject"));
            }
            
            // Set location details
            if (studentData.has("division")) {
                divisionLabel.setText(studentData.getString("division"));
            }
            if (studentData.has("district")) {
                districtLabel.setText(studentData.getString("district"));
            }
            if (studentData.has("area")) {
                areaLabel.setText(studentData.getString("area"));
            }
            
            // Set personal details
            if (studentData.has("gender")) {
                genderLabel.setText(studentData.getString("gender"));
            }
            
            if (studentData.has("email")) {
                emailLabel.setText(studentData.getString("email"));
            }
            if (studentData.has("phone")) {
                phoneLabel.setText(studentData.getString("phone"));
            }
            
            // Set additional info
            if (studentData.has("additionalInfo")) {
                additionalInfoLabel.setText(studentData.getString("additionalInfo"));
            }

            // Load profile picture
            if (studentData.has("profilePicture")) {
                String picturePath = studentData.getString("profilePicture");
                File imageFile = new File(picturePath);
                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString());
                    profileImageView.setImage(image);
                }
            }
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    public void setShowContactDetails(boolean show) {
        contactDetailsBox.setVisible(show);
        contactDetailsBox.setManaged(show);
    }
}
