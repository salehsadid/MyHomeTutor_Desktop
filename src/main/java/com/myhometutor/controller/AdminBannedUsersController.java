package com.myhometutor.controller;

import com.myhometutor.database.DatabaseManager;
import com.myhometutor.util.EmailService;
import com.myhometutor.util.ThemeManager;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class AdminBannedUsersController {

    @FXML private TableView<BannedUserViewModel> bannedUsersTable;
    @FXML private TableColumn<BannedUserViewModel, Integer> idColumn;
    @FXML private TableColumn<BannedUserViewModel, String> usernameColumn;
    @FXML private TableColumn<BannedUserViewModel, String> typeColumn;
    @FXML private TableColumn<BannedUserViewModel, Void> actionColumn;
    @FXML private ToggleButton themeToggle;

    private ObservableList<BannedUserViewModel> bannedUserList = FXCollections.observableArrayList();
    private String currentFilter = "All";

    @FXML
    private void initialize() {
        themeToggle.setSelected(ThemeManager.getInstance().isDarkMode());
        idColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        usernameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUsername()));
        typeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getType()));

        setupActionColumn();
        loadBannedUsers();
    }

    @FXML
    private void handleThemeToggle() {
        ThemeManager.getInstance().toggleTheme(themeToggle.getScene());
    }

    private void setupActionColumn() {
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button unbanBtn = new Button("Remove Ban");
            private final HBox pane = new HBox(10, unbanBtn);

            {
                unbanBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                
                unbanBtn.setOnAction(event -> {
                    BannedUserViewModel user = getTableView().getItems().get(getIndex());
                    handleUnban(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                }
            }
        });
    }

    private void handleUnban(BannedUserViewModel user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Unban");
        alert.setHeaderText("Unban User: " + user.getUsername());
        alert.setContentText("Are you sure you want to remove the ban for this user?");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            if (DatabaseManager.getInstance().unbanUser(user.getId(), user.getType())) {
                // Send Email
                String subject = "Account Ban Removed - MyHomeTutor";
                String body = "Dear " + user.getUsername() + ",<br><br>" +
                        "Your account ban has been removed by the administrator. You can now login to your account.<br><br>" +
                        "Please ensure you follow our community guidelines to avoid future bans.<br><br>" +
                        "Best regards,<br>MyHomeTutor Admin Team";
                
                new Thread(() -> EmailService.sendEmail(user.getUsername(), subject, body)).start();
                
                loadBannedUsers(); // Refresh list
                showAlert("Success", "User unbanned successfully.");
            } else {
                showAlert("Error", "Failed to unban user.");
            }
        }
    }

    private void loadBannedUsers() {
        bannedUserList.clear();
        List<JSONObject> users = DatabaseManager.getInstance().getBannedUsers();
        
        for (JSONObject json : users) {
            String type = json.getString("type");
            if ("All".equals(currentFilter) || type.equalsIgnoreCase(currentFilter)) {
                bannedUserList.add(new BannedUserViewModel(
                    json.getInt("id"),
                    json.getString("username"),
                    type
                ));
            }
        }
        bannedUsersTable.setItems(bannedUserList);
    }

    @FXML
    private void handleFilterAll() {
        currentFilter = "All";
        loadBannedUsers();
    }

    @FXML
    private void handleFilterStudents() {
        currentFilter = "Student";
        loadBannedUsers();
    }

    @FXML
    private void handleFilterTutors() {
        currentFilter = "Tutor";
        loadBannedUsers();
    }

    // Navigation Handlers
    @FXML private void handleDashboard() { navigateTo("/fxml/AdminDashboard.fxml"); }
    @FXML private void handleConnections() { navigateTo("/fxml/AdminConnectionList.fxml"); }
    @FXML private void handleTuitionPosts() { navigateTo("/fxml/AdminTuitionPostList.fxml"); }
    @FXML private void handleReports() { navigateTo("/fxml/AdminReportList.fxml"); }
    @FXML private void handleLogout() { navigateTo("/fxml/HomePage.fxml"); }

    @FXML
    private void handleTutors() {
        openUserManagement("Tutor");
    }

    @FXML
    private void handleStudents() {
        openUserManagement("Student");
    }
    
    private void openUserManagement(String type) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AdminUserManagement.fxml"));
            Parent root = loader.load();
            
            AdminUserManagementController controller = loader.getController();
            controller.setFilter(type, "All");
            
            Stage stage = (Stage) bannedUsersTable.getScene().getWindow();
            Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
            ThemeManager.getInstance().applyTheme(scene);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void navigateTo(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) bannedUsersTable.getScene().getWindow();
            Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
            ThemeManager.getInstance().applyTheme(scene);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static class BannedUserViewModel {
        private final int id;
        private final String username;
        private final String type;

        public BannedUserViewModel(int id, String username, String type) {
            this.id = id;
            this.username = username;
            this.type = type;
        }

        public int getId() { return id; }
        public String getUsername() { return username; }
        public String getType() { return type; }
    }
}
