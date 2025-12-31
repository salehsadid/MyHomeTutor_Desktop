package com.myhometutor.controller;

import com.myhometutor.database.DatabaseManager;
import com.myhometutor.util.ThemeManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class AdminReportListController {

    @FXML private TableView<ReportViewModel> reportsTable;
    @FXML private TableColumn<ReportViewModel, Integer> idColumn;
    @FXML private TableColumn<ReportViewModel, String> reporterColumn;
    @FXML private TableColumn<ReportViewModel, String> reportedColumn;
    @FXML private TableColumn<ReportViewModel, String> reasonColumn;
    @FXML private TableColumn<ReportViewModel, String> statusColumn;
    @FXML private TableColumn<ReportViewModel, String> dateColumn;
    @FXML private TableColumn<ReportViewModel, Void> actionColumn;
    @FXML private ToggleButton themeToggle;

    private ObservableList<ReportViewModel> reportList = FXCollections.observableArrayList();
    private DatabaseManager dbManager;

    @FXML
    public void initialize() {
        themeToggle.setSelected(ThemeManager.getInstance().isDarkMode());
        dbManager = DatabaseManager.getInstance();
        setupTable();
        loadReports();
    }

    @FXML
    private void handleThemeToggle() {
        ThemeManager.getInstance().toggleTheme(themeToggle.getScene());
    }

    private void setupTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        
        reporterColumn.setCellValueFactory(new PropertyValueFactory<>("reporterName"));
        reporterColumn.setCellFactory(param -> new TableCell<>() {
            private final Hyperlink link = new Hyperlink();
            
            {
                link.setOnAction(event -> {
                    ReportViewModel report = getTableView().getItems().get(getIndex());
                    openProfile(report.getReporterId(), report.getReporterType());
                });
            }
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    if (item.startsWith("Unknown")) {
                        setText(item);
                        setGraphic(null);
                    } else {
                        link.setText(item);
                        setGraphic(link);
                        setText(null);
                    }
                }
            }
        });

        reportedColumn.setCellValueFactory(new PropertyValueFactory<>("reportedName"));
        reportedColumn.setCellFactory(param -> new TableCell<>() {
            private final Hyperlink link = new Hyperlink();
            
            {
                link.setOnAction(event -> {
                    ReportViewModel report = getTableView().getItems().get(getIndex());
                    openProfile(report.getReportedId(), report.getReportedType());
                });
            }
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    if (item.startsWith("Unknown")) {
                        setText(item);
                        setGraphic(null);
                    } else {
                        link.setText(item);
                        setGraphic(link);
                        setText(null);
                    }
                }
            }
        });

        reasonColumn.setCellValueFactory(new PropertyValueFactory<>("reason"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button resolveButton = new Button("Resolve");
            private final Button dismissButton = new Button("Dismiss");
            private final Button banButton = new Button("Ban");
            private final Button deleteButton = new Button("Delete");
            private final HBox pane = new HBox(5);

            {
                resolveButton.getStyleClass().add("action-button");
                dismissButton.getStyleClass().add("action-button");
                banButton.getStyleClass().add("action-button");
                banButton.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white;");
                
                deleteButton.getStyleClass().add("action-button");
                deleteButton.setStyle("-fx-background-color: #555555; -fx-text-fill: white;");
                
                resolveButton.setOnAction(event -> {
                    ReportViewModel report = getTableView().getItems().get(getIndex());
                    handleResolve(report);
                });

                dismissButton.setOnAction(event -> {
                    ReportViewModel report = getTableView().getItems().get(getIndex());
                    handleDismiss(report);
                });

                banButton.setOnAction(event -> {
                    ReportViewModel report = getTableView().getItems().get(getIndex());
                    handleBan(report);
                });

                deleteButton.setOnAction(event -> {
                    ReportViewModel report = getTableView().getItems().get(getIndex());
                    handleDelete(report);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    ReportViewModel report = getTableView().getItems().get(getIndex());
                    pane.getChildren().clear();
                    
                    if ("pending".equalsIgnoreCase(report.getStatus())) {
                        pane.getChildren().addAll(resolveButton, dismissButton, banButton);
                    }
                    
                    pane.getChildren().add(deleteButton);
                    setGraphic(pane);
                }
            }
        });
    }

    private void handleDelete(ReportViewModel report) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Report");
        confirm.setContentText("Are you sure you want to delete this report? This action cannot be undone.");
        
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            if (dbManager.deleteReport(report.getId())) {
                loadReports();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Report deleted successfully.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete report.");
            }
        }
    }

    private void loadReports() {
        reportList.clear();
        JSONArray reports = dbManager.getAllReports();
        
        for (int i = 0; i < reports.length(); i++) {
            JSONObject report = reports.getJSONObject(i);
            
            String reporterName = dbManager.getUserName(report.getInt("reporter_id"), report.getString("reporter_type"));
            String reportedName = dbManager.getUserName(report.getInt("reported_id"), report.getString("reported_type"));
            
            reportList.add(new ReportViewModel(
                report.getInt("id"),
                report.getInt("reporter_id"),
                reporterName + " (" + report.getString("reporter_type") + ")",
                report.getString("reporter_type"),
                report.getInt("reported_id"),
                reportedName + " (" + report.getString("reported_type") + ")",
                report.getString("reported_type"),
                report.getString("reason"),
                report.getString("status"),
                report.getString("created_at")
            ));
        }
        reportsTable.setItems(reportList);
    }

    private void openProfile(int userId, String userType) {
        try {
            String fxmlFile = userType.equalsIgnoreCase("student") ? "/fxml/ViewStudentProfile.fxml" : "/fxml/ViewTutorProfile.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            
            JSONObject userData;
            if (userType.equalsIgnoreCase("student")) {
                userData = dbManager.getStudentById(userId);
                if (userData != null) {
                    ViewStudentProfileController controller = loader.getController();
                    controller.setStudentData(userData);
                    controller.setStudentId(userId);
                    controller.setAdminMode(true);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "User not found.");
                    return;
                }
            } else {
                userData = dbManager.getTutorById(userId);
                if (userData != null) {
                    ViewTutorProfileController controller = loader.getController();
                    controller.setTutorData(userData);
                    controller.setTutorId(userId);
                    controller.setAdminMode(true);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "User not found.");
                    return;
                }
            }
            
            Stage stage = new Stage();
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            
            javafx.geometry.Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
            Scene scene = new Scene(root, screenBounds.getWidth() * 0.8, screenBounds.getHeight() * 0.8);
            
            ThemeManager.getInstance().applyTheme(scene);
            stage.setScene(scene);
            stage.showAndWait();
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open profile.");
        }
    }

    private void handleResolve(ReportViewModel report) {
        if (dbManager.updateReportStatus(report.getId(), "resolved")) {
            loadReports();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Report marked as resolved.");
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update report status.");
        }
    }

    private void handleDismiss(ReportViewModel report) {
        if (dbManager.updateReportStatus(report.getId(), "dismissed")) {
            loadReports();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Report dismissed.");
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update report status.");
        }
    }

    private void handleBan(ReportViewModel report) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Ban");
        confirm.setHeaderText("Ban User");
        confirm.setContentText("Are you sure you want to ban this user?");
        
        if (confirm.showAndWait().get() == ButtonType.OK) {
            if (dbManager.banUser(report.getReportedId(), report.getReportedType())) {
                dbManager.updateReportStatus(report.getId(), "resolved");
                
                // Send Email
                String username = dbManager.getUserName(report.getReportedId(), report.getReportedType());
                String subject = "Account Banned - MyHomeTutor";
                String body = "Dear User,<br><br>" +
                        "Your account has been banned due to a violation of our policies reported by another user.<br>" +
                        "Reason: " + report.getReason() + "<br><br>" +
                        "If you believe this is a mistake, please contact support.<br><br>" +
                        "Best regards,<br>MyHomeTutor Admin Team";
                
                new Thread(() -> com.myhometutor.util.EmailService.sendEmail(username, subject, body)).start();

                loadReports();
                showAlert(Alert.AlertType.INFORMATION, "Success", "User has been banned and report resolved.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to ban user.");
            }
        }
    }

    @FXML
    private void handleFilterAll() {
        reportsTable.setItems(reportList);
    }

    @FXML
    private void handleFilterPending() {
        ObservableList<ReportViewModel> filtered = FXCollections.observableArrayList();
        for (ReportViewModel report : reportList) {
            if ("pending".equalsIgnoreCase(report.getStatus())) {
                filtered.add(report);
            }
        }
        reportsTable.setItems(filtered);
    }

    @FXML
    private void handleFilterResolved() {
        ObservableList<ReportViewModel> filtered = FXCollections.observableArrayList();
        for (ReportViewModel report : reportList) {
            if ("resolved".equalsIgnoreCase(report.getStatus())) {
                filtered.add(report);
            }
        }
        reportsTable.setItems(filtered);
    }

    // Navigation Methods
    @FXML
    private void handleDashboard() { navigateTo("/fxml/AdminDashboard.fxml"); }
    
    @FXML
    private void handleStudents() { 
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AdminUserManagement.fxml"));
            Parent root = loader.load();
            
            AdminUserManagementController controller = loader.getController();
            controller.setFilter("Student", "All");
            
            Stage stage = (Stage) reportsTable.getScene().getWindow();
            Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
            ThemeManager.getInstance().applyTheme(scene);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load AdminUserManagement.fxml");
        }
    }

    @FXML
    private void handleTutors() { 
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AdminUserManagement.fxml"));
            Parent root = loader.load();
            
            AdminUserManagementController controller = loader.getController();
            controller.setFilter("Tutor", "All");
            
            Stage stage = (Stage) reportsTable.getScene().getWindow();
            Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
            ThemeManager.getInstance().applyTheme(scene);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load AdminUserManagement.fxml");
        }
    }

    @FXML
    private void handleTuitionPosts() { navigateTo("/fxml/AdminTuitionPostList.fxml"); }
    @FXML
    private void handleConnections() { navigateTo("/fxml/AdminConnectionList.fxml"); }
    @FXML
    private void handleBannedUsers() { navigateTo("/fxml/AdminBannedUsers.fxml"); }
    @FXML
    private void handleLogout() { navigateTo("/fxml/AdminLogin.fxml"); }

    private void navigateTo(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) reportsTable.getScene().getWindow();
            Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
            ThemeManager.getInstance().applyTheme(scene);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load " + fxmlPath);
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
