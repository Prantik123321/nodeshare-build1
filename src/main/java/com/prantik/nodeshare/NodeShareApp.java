package com.prantik.nodeshare;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.scene.Scene;
import com.prantik.nodeshare.controllers.AuthController;
import com.prantik.nodeshare.utils.DatabaseHelper;
import com.prantik.nodeshare.utils.ThemeManager;
import java.io.File;

public class NodeShareApp extends Application {
    
    private static NodeShareApp instance;
    private Stage primaryStage;
    private ThemeManager themeManager;
    private AuthController authController;
    
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) {
        System.out.println("📦 NodeShare v1.0.0");
        System.out.println("👤 Author: Prantik");
        System.out.println("📱 Platform: JavaFX 21");
        
        instance = this;
        this.primaryStage = primaryStage;
        
        // Initialize theme
        this.themeManager = new ThemeManager();
        
        // Setup app directory
        setupAppDirectory();
        
        // Initialize database
        DatabaseHelper.getInstance().initDatabase();
        
        // Show login screen
        showLoginScreen();
        
        // Handle shutdown
        primaryStage.setOnCloseRequest(event -> {
            shutdown();
        });
    }
    
    private void setupAppDirectory() {
        String userHome = System.getProperty("user.home");
        String appDir = userHome + File.separator + ".nodeshare";
        
        try {
            File dir = new File(appDir);
            if (!dir.exists()) {
                dir.mkdirs();
                System.out.println("📁 Created app directory: " + appDir);
            }
            new File(appDir, "cache").mkdirs();
            new File(appDir, "downloads").mkdirs();
            new File(appDir, "queue").mkdirs();
        } catch (Exception e) {
            System.err.println("❌ Failed to create app directory: " + e.getMessage());
        }
    }
    
    public void showLoginScreen() {
        try {
            authController = new AuthController();
            Scene scene = authController.getScene();
            themeManager.applyTheme(scene);
            
            primaryStage.setTitle("NodeShare — Login");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(400);
            primaryStage.setMinHeight(600);
            primaryStage.setResizable(true);
            primaryStage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Failed to show login screen: " + e.getMessage());
        }
    }
    
    public void showMainApp() {
        Platform.runLater(() -> {
            try {
                // TODO: Show main feed view
                System.out.println("✅ Main app loaded successfully");
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("❌ Failed to load main app: " + e.getMessage());
            }
        });
    }
    
    private void shutdown() {
        System.out.println("🔄 Shutting down NodeShare...");
        DatabaseHelper.getInstance().close();
        Platform.exit();
        System.exit(0);
    }
    
    public static NodeShareApp getInstance() {
        return instance;
    }
    
    public Stage getPrimaryStage() {
        return primaryStage;
    }
    
    public ThemeManager getThemeManager() {
        return themeManager;
    }
}
