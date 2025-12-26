package com.myhometutor.controller;

import com.myhometutor.util.ThemeManager;
import com.myhometutor.database.DatabaseManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.io.File;

public class ViewTutorProfileController {

    @FXML private ImageView profileImageView;
    @FXML private Label userNameLabel;
    @FXML private Label userTypeLabel;
    
    // College Info
    @FXML private Label collegeNameLabel;
    @FXML private Label collegeGroupLabel;
    @FXML private Label hscYearLabel;
    
    // University Info
    @FXML private Label universityNameLabel;
    @FXML private Label universityDeptLabel;
    @FXML private Label universityYearLabel;
    @FXML private Label universitySessionLabel;
    
    @FXML private Label experienceLabel;
    
    @FXML private Label divisionLabel;
    @FXML private Label districtLabel;
    @FXML private Label areaLabel;
    
    @FXML private Label genderLabel;
    
    @FXML private javafx.scene.layout.VBox contactDetailsBox;
    @FXML private Label emailLabel;
    @FXML private Label phoneLabel;
    
    @FXML private Label preferredFeeLabel;
    @FXML private Label preferredDayLabel;
    @FXML private Label preferredTimeLabel;
    @FXML private Label preferredLocationLabel;
    
    @FXML private Label additionalInfoLabel;
    
    @FXML private Button closeButton;
    @FXML private HBox adminActionBox;

    private int tutorId;

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

    public void setTutorData(JSONObject tutorData) {
        if (tutorData != null) {
            if (tutorData.has("name")) {
                userNameLabel.setText(tutorData.getString("name").toUpperCase());
            }
            
            // College Info
            if (tutorData.has("collegeName")) {
                collegeNameLabel.setText(tutorData.getString("collegeName"));
            }
            if (tutorData.has("collegeGroup")) {
                collegeGroupLabel.setText(tutorData.getString("collegeGroup"));
            }
            if (tutorData.has("hscYear")) {
                hscYearLabel.setText(tutorData.getString("hscYear"));
            }
            
            // University Info
            if (tutorData.has("universityName")) {
                universityNameLabel.setText(tutorData.getString("universityName"));
            }
            if (tutorData.has("universityDept")) {
                universityDeptLabel.setText(tutorData.getString("universityDept"));
            }
            if (tutorData.has("universityYear")) {
                universityYearLabel.setText(tutorData.getString("universityYear"));
            }
            if (tutorData.has("universitySession")) {
                universitySessionLabel.setText(tutorData.getString("universitySession"));
            }
            
            if (tutorData.has("experience")) {
                experienceLabel.setText(tutorData.getString("experience"));
            }
            
            if (tutorData.has("division")) {
                divisionLabel.setText(tutorData.getString("division"));
            }
            if (tutorData.has("district")) {
                districtLabel.setText(tutorData.getString("district"));
            }
            if (tutorData.has("area")) {
                areaLabel.setText(tutorData.getString("area"));
            }
            
            if (tutorData.has("gender")) {
                genderLabel.setText(tutorData.getString("gender"));
            }
            
            if (tutorData.has("email")) {
                emailLabel.setText(tutorData.getString("email"));
            }
            if (tutorData.has("phone")) {
                phoneLabel.setText(tutorData.getString("phone"));
            }
            
            if (tutorData.has("preferredFee")) {
                preferredFeeLabel.setText(tutorData.getString("preferredFee") + " BDT");
            }
            if (tutorData.has("preferredDay")) {
                preferredDayLabel.setText(tutorData.getString("preferredDay"));
            }
            if (tutorData.has("preferredTime")) {
                preferredTimeLabel.setText(tutorData.getString("preferredTime"));
            }
            if (tutorData.has("preferredLocation")) {
                preferredLocationLabel.setText(tutorData.getString("preferredLocation"));
            }
            
            if (tutorData.has("additionalInfo")) {
                additionalInfoLabel.setText(tutorData.getString("additionalInfo"));
            }

            // Load profile picture
            if (tutorData.has("profilePicture")) {
                String picturePath = tutorData.getString("profilePicture");
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

    public void setTutorId(int id) {
        this.tutorId = id;
    }

    public void setAdminMode(boolean isAdmin) {
        if (isAdmin) {
            adminActionBox.setVisible(true);
            adminActionBox.setManaged(true);
            setShowContactDetails(true); // Admin sees contact info
        } else {
            adminActionBox.setVisible(false);
            adminActionBox.setManaged(false);
        }
    }

    @FXML
    private void handleApprove() {
        DatabaseManager.getInstance().verifyTutor(tutorId, true);
        handleClose();
    }

    @FXML
    private void handleReject() {
        DatabaseManager.getInstance().verifyTutor(tutorId, false);
        handleClose();
    }
}
