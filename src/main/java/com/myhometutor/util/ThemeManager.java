package com.myhometutor.util;

import javafx.scene.Scene;

public class ThemeManager {
    private static ThemeManager instance;
    private boolean isDarkMode = true;
    
    private ThemeManager() {}
    
    public static ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }
    
    public boolean isDarkMode() {
        return isDarkMode;
    }
    
    public void setDarkMode(boolean darkMode) {
        this.isDarkMode = darkMode;
    }
    
    public void applyTheme(Scene scene) {
        if (scene == null) return;
        scene.getStylesheets().clear();
        String cssPath = isDarkMode ? "/css/style.css" : "/css/style-light.css";
        
        java.net.URL resource = getClass().getResource(cssPath);
        if (resource != null) {
            scene.getStylesheets().add(resource.toExternalForm());
        } else {
            System.err.println("Error: Could not find CSS file: " + cssPath);
        }
    }
    
    public void toggleTheme(Scene scene) {
        isDarkMode = !isDarkMode;
        applyTheme(scene);
    }
}
