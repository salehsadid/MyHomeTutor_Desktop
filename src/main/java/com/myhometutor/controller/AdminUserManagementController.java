package com.myhometutor.controller;

import com.myhometutor.util.ThemeManager;
import com.myhometutor.database.DatabaseManager;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class AdminUserManagementController {

    @FXML private TableView<UserViewModel> usersTable;
    @FXML private TableColumn<UserViewModel, Integer> idColumn;
    @FXML private TableColumn<UserViewModel, String> usernameColumn;
    @FXML private TableColumn<UserViewModel, String> typeColumn;
    @FXML private TableColumn<UserViewModel, String> statusColumn;
    @FXML private TableColumn<UserViewModel, Void> actionColumn;
    
    @FXML private javafx.scene.control.Label pageTitle;
    @FXML private Button allUsersButton;
    @FXML private Button pendingUsersButton;
    @FXML private javafx.scene.control.ToggleButton themeToggle;

    private ObservableList<UserViewModel> userList = FXCollections.observableArrayList();
    private String currentTypeFilter = "All";
    private String currentStatusFilter = "All";

    @FXML
    private void initialize() {
        themeToggle.setSelected(ThemeManager.getInstance().isDarkMode());
        idColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        usernameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUsername()));
        typeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getType()));
        statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));

        addButtonToTable();
        loadAllUsers();
    }

    @FXML
    private void handleThemeToggle() {
        ThemeManager.getInstance().toggleTheme(themeToggle.getScene());
    }

    public void setFilter(String type, String status) {
        this.currentTypeFilter = type;
        this.currentStatusFilter = status;
        updateUI();
        applyFilters();
    }

    private void updateUI() {
        if ("Student".equalsIgnoreCase(currentTypeFilter)) {
            pageTitle.setText("Students");
            allUsersButton.setText("All Students");
            pendingUsersButton.setText("Pending Students");
        } else if ("Tutor".equalsIgnoreCase(currentTypeFilter)) {
            pageTitle.setText("Tutors");
            allUsersButton.setText("All Tutors");
            pendingUsersButton.setText("Pending Tutors");
        } else {
            pageTitle.setText("User Management");
            allUsersButton.setText("All Users");
            pendingUsersButton.setText("Pending Users");
        }
    }

    private void applyFilters() {
        userList.clear();
        List<JSONObject> users = DatabaseManager.getInstance().getAllUsers();
        for (JSONObject json : users) {
            String userType = json.getString("type");
            String userStatus = json.optString("status", "N/A");

            boolean typeMatch = "All".equalsIgnoreCase(currentTypeFilter) || userType.equalsIgnoreCase(currentTypeFilter);
            boolean statusMatch = "All".equalsIgnoreCase(currentStatusFilter) || userStatus.equalsIgnoreCase(currentStatusFilter);

            if (typeMatch && statusMatch) {
                userList.add(new UserViewModel(
                    json.getInt("id"),
                    json.getString("username"),
                    userType,
                    userStatus,
                    json
                ));
            }
        }
        usersTable.setItems(userList);
    }

    private void loadAllUsers() {
        applyFilters();
    }

    @FXML
    private void handleFilterAll() {
        currentStatusFilter = "All";
        applyFilters();
    }

    @FXML
    private void handleFilterPending() {
        currentStatusFilter = "Pending";
        applyFilters();
    }

    @FXML
    private void handleDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AdminDashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) usersTable.getScene().getWindow();
            Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
            ThemeManager.getInstance().applyTheme(scene);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleConnections() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AdminConnectionList.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) usersTable.getScene().getWindow();
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
            Stage stage = (Stage) usersTable.getScene().getWindow();
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
            Stage stage = (Stage) usersTable.getScene().getWindow();
            Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
            ThemeManager.getInstance().applyTheme(scene);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleStudents() {
        setFilter("Student", "All");
    }

    @FXML
    private void handleTutors() {
        setFilter("Tutor", "All");
    }

    @FXML
    private void handleBannedUsers() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AdminBannedUsers.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) usersTable.getScene().getWindow();
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
            Stage stage = (Stage) usersTable.getScene().getWindow();
            Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
            ThemeManager.getInstance().applyTheme(scene);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addButtonToTable() {
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("View");

            {
                btn.setOnAction(event -> {
                    UserViewModel user = getTableView().getItems().get(getIndex());
                    if ("Tutor".equals(user.getType())) {
                        openTutorProfile(user);
                    } else if ("Student".equals(user.getType())) {
                        openStudentProfile(user);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        });
    }

    private void openTutorProfile(UserViewModel user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ViewTutorProfile.fxml"));
            Parent root = loader.load();
            
            ViewTutorProfileController controller = loader.getController();
            controller.setTutorData(user.getJsonData());
            controller.setTutorId(user.getId());
            controller.setAdminMode(true);
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            
            javafx.geometry.Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
            Scene scene = new Scene(root, screenBounds.getWidth() * 0.8, screenBounds.getHeight() * 0.8);
            
            ThemeManager.getInstance().applyTheme(scene);
            stage.setScene(scene);
            stage.showAndWait();
            
            // Refresh list after closing (in case status changed)
            loadAllUsers(); 
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openStudentProfile(UserViewModel user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ViewStudentProfile.fxml"));
            Parent root = loader.load();
            
            ViewStudentProfileController controller = loader.getController();
            controller.setStudentData(user.getJsonData());
            controller.setStudentId(user.getId());
            controller.setAdminMode(true);
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            
            javafx.geometry.Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
            Scene scene = new Scene(root, screenBounds.getWidth() * 0.8, screenBounds.getHeight() * 0.8);
            
            ThemeManager.getInstance().applyTheme(scene);
            stage.setScene(scene);
            stage.showAndWait();
            
            // Refresh list after closing (in case status changed)
            loadAllUsers(); 
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class UserViewModel {
        private final int id;
        private final String username;
        private final String type;
        private final String status;
        private final JSONObject jsonData;

        public UserViewModel(int id, String username, String type, String status, JSONObject jsonData) {
            this.id = id;
            this.username = username;
            this.type = type;
            this.status = status;
            this.jsonData = jsonData;
        }

        public int getId() { return id; }
        public String getUsername() { return username; }
        public String getType() { return type; }
        public String getStatus() { return status; }
        public JSONObject getJsonData() { return jsonData; }
    }
}