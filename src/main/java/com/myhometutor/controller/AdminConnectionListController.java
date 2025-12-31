package com.myhometutor.controller;

import com.myhometutor.database.DatabaseManager;
import com.myhometutor.util.ThemeManager;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class AdminConnectionListController {

    @FXML private TableView<ConnectionViewModel> connectionsTable;
    @FXML private TableColumn<ConnectionViewModel, Integer> idColumn;
    @FXML private TableColumn<ConnectionViewModel, String> tutorNameColumn;
    @FXML private TableColumn<ConnectionViewModel, String> studentNameColumn;
    @FXML private TableColumn<ConnectionViewModel, String> dateColumn;
    @FXML private javafx.scene.control.ToggleButton themeToggle;

    private ObservableList<ConnectionViewModel> connectionList = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        themeToggle.setSelected(ThemeManager.getInstance().isDarkMode());
        idColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        tutorNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTutorName()));
        studentNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStudentName()));
        dateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDate()));

        loadConnections();
    }

    @FXML
    private void handleThemeToggle() {
        ThemeManager.getInstance().toggleTheme(themeToggle.getScene());
    }

    private void loadConnections() {
        connectionList.clear();
        List<JSONObject> connections = DatabaseManager.getInstance().getAllConnections();
        for (JSONObject json : connections) {
            connectionList.add(new ConnectionViewModel(
                json.getInt("id"),
                json.optString("tutor_name", "Unknown"),
                json.optString("student_name", "Unknown"),
                json.optString("created_at", "N/A"),
                json
            ));
        }
        connectionsTable.setItems(connectionList);
    }

    @FXML
    private void handleDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AdminDashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) connectionsTable.getScene().getWindow();
            Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
            ThemeManager.getInstance().applyTheme(scene);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleStudents() {
        openUserManagement("Student", "All");
    }

    @FXML
    private void handleTutors() {
        openUserManagement("Tutor", "All");
    }

    private void openUserManagement(String userType, String filterStatus) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AdminUserManagement.fxml"));
            Parent root = loader.load();
            
            AdminUserManagementController controller = loader.getController();
            controller.setFilter(userType, filterStatus);
            
            Stage stage = (Stage) connectionsTable.getScene().getWindow();
            Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
            ThemeManager.getInstance().applyTheme(scene);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleTuitionPosts() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AdminTuitionPostList.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) connectionsTable.getScene().getWindow();
            Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
            ThemeManager.getInstance().applyTheme(scene);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleReports() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AdminReportList.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) connectionsTable.getScene().getWindow();
            Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
            ThemeManager.getInstance().applyTheme(scene);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBannedUsers() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AdminBannedUsers.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) connectionsTable.getScene().getWindow();
            Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
            ThemeManager.getInstance().applyTheme(scene);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/HomePage.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) connectionsTable.getScene().getWindow();
            Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
            ThemeManager.getInstance().applyTheme(scene);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}