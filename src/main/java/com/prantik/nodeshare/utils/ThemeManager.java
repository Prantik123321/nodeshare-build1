package com.prantik.nodeshare.utils;

import javafx.scene.Scene;

public class ThemeManager {
    private String currentTheme = "light";
    
    public ThemeManager() {
        String saved = System.getProperty("nodeshare.theme", "light");
        this.currentTheme = saved;
    }
    
    public void applyTheme(Scene scene) {
        try {
            String css = getClass().getResource("/css/styles.css").toExternalForm();
            scene.getStylesheets().clear();
            scene.getStylesheets().add(css);
        } catch (Exception e) {
            System.err.println("Failed to apply theme: " + e.getMessage());
        }
    }
    
    public void setTheme(String theme) {
        this.currentTheme = theme;
        System.setProperty("nodeshare.theme", theme);
    }
    
    public String getCurrentTheme() {
        return currentTheme;
    }
}
