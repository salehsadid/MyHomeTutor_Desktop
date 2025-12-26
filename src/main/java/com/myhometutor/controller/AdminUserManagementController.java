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

    private ObservableList<UserViewModel> userList = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        idColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        usernameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUsername()));
        typeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getType()));
        statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));

        addButtonToTable();
        loadAllUsers();
    }

    private void loadAllUsers() {
        userList.clear();
        List<JSONObject> users = DatabaseManager.getInstance().getAllUsers();
        for (JSONObject json : users) {
            userList.add(new UserViewModel(
                json.getInt("id"),
                json.getString("username"),
                json.getString("type"),
                json.has("status") ? json.getString("status") : "N/A",
                json
            ));
        }
        usersTable.setItems(userList);
    }

    @FXML
    private void handleFilterAll() {
        loadAllUsers();
    }

    @FXML
    private void handleFilterPending() {
        userList.clear();
        List<JSONObject> users = DatabaseManager.getInstance().getAllUsers();
        for (JSONObject json : users) {
            if ("Pending".equals(json.optString("status"))) {
                userList.add(new UserViewModel(
                    json.getInt("id"),
                    json.getString("username"),
                    json.getString("type"),
                    json.getString("status"),
                    json
                ));
            }
        }
        usersTable.setItems(userList);
    }

    private void addButtonToTable() {
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("View");

            {
                btn.setOnAction(event -> {
                    UserViewModel user = getTableView().getItems().get(getIndex());
                    if ("Tutor".equals(user.getType())) {
                        openTutorProfile(user);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    UserViewModel user = getTableView().getItems().get(getIndex());
                    if ("Tutor".equals(user.getType())) {
                        setGraphic(btn);
                    } else {
                        setGraphic(null);
                    }
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
            Scene scene = new Scene(root);
            ThemeManager.getInstance().applyTheme(scene);
            stage.setScene(scene);
            stage.showAndWait();
            
            // Refresh list after closing (in case status changed)
            handleFilterAll(); // Or keep current filter
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AdminDashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) usersTable.getScene().getWindow();
            
            // Preserve current size
            double width = stage.getWidth();
            double height = stage.getHeight();
            
            Scene scene = new Scene(root, width, height);
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
            
            // Preserve current size
            double width = stage.getWidth();
            double height = stage.getHeight();
            
            Scene scene = new Scene(root, width, height);
            ThemeManager.getInstance().applyTheme(scene);
            
            stage.setScene(scene);
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