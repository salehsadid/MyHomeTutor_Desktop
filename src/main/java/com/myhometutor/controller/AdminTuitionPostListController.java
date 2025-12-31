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
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class AdminTuitionPostListController {

    @FXML private TableView<TuitionPostViewModel> postsTable;
    @FXML private TableColumn<TuitionPostViewModel, Integer> idColumn;
    @FXML private TableColumn<TuitionPostViewModel, String> studentNameColumn;
    @FXML private TableColumn<TuitionPostViewModel, String> subjectColumn;
    @FXML private TableColumn<TuitionPostViewModel, String> classColumn;
    @FXML private TableColumn<TuitionPostViewModel, String> statusColumn;
    @FXML private TableColumn<TuitionPostViewModel, Void> actionColumn;
    @FXML private javafx.scene.control.ToggleButton themeToggle;

    private ObservableList<TuitionPostViewModel> postList = FXCollections.observableArrayList();
    private String currentStatusFilter = "All";

    @FXML
    private void initialize() {
        themeToggle.setSelected(ThemeManager.getInstance().isDarkMode());
        idColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        studentNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStudentName()));
        subjectColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSubject()));
        classColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getClassName()));
        statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));

        addButtonToTable();
        loadAllPosts();
    }

    @FXML
    private void handleThemeToggle() {
        ThemeManager.getInstance().toggleTheme(themeToggle.getScene());
    }

    public void setFilter(String status) {
        this.currentStatusFilter = status;
        applyFilters();
    }

    private void applyFilters() {
        postList.clear();
        List<JSONObject> posts = DatabaseManager.getInstance().getAllTuitionPostsForAdmin();
        for (JSONObject json : posts) {
            String status = json.optString("status", "pending");
            
            boolean statusMatch = "All".equalsIgnoreCase(currentStatusFilter) || 
                                  status.equalsIgnoreCase(currentStatusFilter) ||
                                  ("Active".equalsIgnoreCase(currentStatusFilter) && "assigned".equalsIgnoreCase(status));

            if (statusMatch) {
                postList.add(new TuitionPostViewModel(
                    json.getInt("id"),
                    json.optString("student_name", "Unknown"),
                    json.optString("subject", "N/A"),
                    json.optString("class", "N/A"),
                    status,
                    json
                ));
            }
        }
        postsTable.setItems(postList);
    }

    private void loadAllPosts() {
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
    private void handleFilterApproved() {
        currentStatusFilter = "Active"; // Assuming 'active' is the status for approved
        applyFilters();
    }

    private void addButtonToTable() {
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("View");

            {
                btn.setOnAction(event -> {
                    TuitionPostViewModel post = getTableView().getItems().get(getIndex());
                    openPostDetails(post);
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

    private void openPostDetails(TuitionPostViewModel post) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ViewTuitionPostDetails.fxml"));
            Parent root = loader.load();

            ViewTuitionPostDetailsController controller = loader.getController();
            controller.setPostData(post.getJsonData());
            controller.setAdminMode(true);

            Stage stage = new Stage();
            stage.setTitle("Tuition Post Details");
            stage.initModality(Modality.APPLICATION_MODAL);
            
            javafx.geometry.Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
            Scene scene = new Scene(root, screenBounds.getWidth() * 0.8, screenBounds.getHeight() * 0.8);
            
            ThemeManager.getInstance().applyTheme(scene);
            stage.setScene(scene);
            stage.showAndWait();
            
            // Refresh list after closing details (in case status changed)
            loadAllPosts();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AdminDashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) postsTable.getScene().getWindow();
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
            
            Stage stage = (Stage) postsTable.getScene().getWindow();
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
            Stage stage = (Stage) postsTable.getScene().getWindow();
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
            Stage stage = (Stage) postsTable.getScene().getWindow();
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
            Stage stage = (Stage) postsTable.getScene().getWindow();
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
            Stage stage = (Stage) postsTable.getScene().getWindow();
            Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
            ThemeManager.getInstance().applyTheme(scene);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}