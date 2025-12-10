package com.myhometutor;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/HomePage.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root, 800, 700);
            
            primaryStage.setTitle("MyHomeTutor - Right Tutor at Right Time");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(700);
            primaryStage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading application: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
