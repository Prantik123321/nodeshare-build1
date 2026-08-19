package com.prantik.nodeshare.controllers;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.application.Platform;
import com.prantik.nodeshare.NodeShareApp;
import com.prantik.nodeshare.models.User;
import com.prantik.nodeshare.services.AuthService;

public class AuthController {
    private Scene scene;
    private AuthService authService;
    private VBox root;
    
    private TextField usernameField;
    private PasswordField passwordField;
    private Label loginError;
    private Button loginBtn;
    
    private TextField regUsernameField;
    private TextField regEmailField;
    private PasswordField regPasswordField;
    private PasswordField regConfirmField;
    private Label regError;
    private Button registerBtn;
    
    public AuthController() {
        this.authService = AuthService.getInstance();
        createScene();
    }
    
    private void createScene() {
        root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: linear-gradient(135deg, #667eea 0%, #764ba2 100%);");
        
        VBox card = new VBox(16);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 20px; -fx-padding: 40px; " +
                      "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 20, 0, 0, 10);");
        card.setMaxWidth(420);
        
        Text title = new Text("📦 NodeShare");
        title.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-fill: #1a2332;");
        
        Text subtitle = new Text("Share and discover knowledge");
        subtitle.setStyle("-fx-font-size: 14px; -fx-fill: #6b7a8a;");
        
        // Tabs
        HBox tabs = new HBox(8);
        tabs.setAlignment(Pos.CENTER);
        
        Button loginTab = new Button("Login");
        loginTab.setStyle("-fx-background-color: #4f7cff; -fx-text-fill: white; -fx-font-weight: bold; " +
                          "-fx-padding: 8px 24px; -fx-background-radius: 20px;");
        
        Button registerTab = new Button("Register");
        registerTab.setStyle("-fx-background-color: transparent; -fx-text-fill: #6b7a8a; " +
                             "-fx-padding: 8px 24px; -fx-background-radius: 20px;");
        
        tabs.getChildren().addAll(loginTab, registerTab);
        
        // Login Form
        VBox loginForm = new VBox(12);
        
        usernameField = new TextField();
        usernameField.setPromptText("Username or Email");
        usernameField.setStyle("-fx-padding: 12px; -fx-background-radius: 10px; " +
                               "-fx-border-color: #e1e5eb; -fx-border-radius: 10px; -fx-font-size: 14px;");
        
        passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setStyle("-fx-padding: 12px; -fx-background-radius: 10px; " +
                               "-fx-border-color: #e1e5eb; -fx-border-radius: 10px; -fx-font-size: 14px;");
        
        loginError = new Label();
        loginError.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 13px;");
        loginError.setVisible(false);
        
        loginBtn = new Button("Login");
        loginBtn.setStyle("-fx-background-color: #4f7cff; -fx-text-fill: white; -fx-font-size: 16px; " +
                          "-fx-font-weight: bold; -fx-padding: 14px; -fx-background-radius: 10px;");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setOnAction(e -> handleLogin());
        
        loginForm.getChildren().addAll(usernameField, passwordField, loginError, loginBtn);
        
        // Register Form
        VBox registerForm = new VBox(12);
        registerForm.setVisible(false);
        
        regUsernameField = new TextField();
        regUsernameField.setPromptText("Username");
        regUsernameField.setStyle("-fx-padding: 12px; -fx-background-radius: 10px; " +
                                  "-fx-border-color: #e1e5eb; -fx-border-radius: 10px; -fx-font-size: 14px;");
        
        regEmailField = new TextField();
        regEmailField.setPromptText("Email");
        regEmailField.setStyle("-fx-padding: 12px; -fx-background-radius: 10px; " +
                               "-fx-border-color: #e1e5eb; -fx-border-radius: 10px; -fx-font-size: 14px;");
        
        regPasswordField = new PasswordField();
        regPasswordField.setPromptText("Password (min 6 characters)");
        regPasswordField.setStyle("-fx-padding: 12px; -fx-background-radius: 10px; " +
                                  "-fx-border-color: #e1e5eb; -fx-border-radius: 10px; -fx-font-size: 14px;");
        
        regConfirmField = new PasswordField();
        regConfirmField.setPromptText("Confirm Password");
        regConfirmField.setStyle("-fx-padding: 12px; -fx-background-radius: 10px; " +
                                 "-fx-border-color: #e1e5eb; -fx-border-radius: 10px; -fx-font-size: 14px;");
        
        regError = new Label();
        regError.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 13px;");
        regError.setVisible(false);
        
        registerBtn = new Button("Create Account");
        registerBtn.setStyle("-fx-background-color: #4f7cff; -fx-text-fill: white; -fx-font-size: 16px; " +
                             "-fx-font-weight: bold; -fx-padding: 14px; -fx-background-radius: 10px;");
        registerBtn.setMaxWidth(Double.MAX_VALUE);
        registerBtn.setOnAction(e -> handleRegister());
        
        registerForm.getChildren().addAll(regUsernameField, regEmailField, regPasswordField, 
                                          regConfirmField, regError, registerBtn);
        
        // Tab switching
        loginTab.setOnAction(e -> {
            loginTab.setStyle("-fx-background-color: #4f7cff; -fx-text-fill: white; -fx-font-weight: bold; " +
                              "-fx-padding: 8px 24px; -fx-background-radius: 20px;");
            registerTab.setStyle("-fx-background-color: transparent; -fx-text-fill: #6b7a8a; " +
                                 "-fx-padding: 8px 24px; -fx-background-radius: 20px;");
            loginForm.setVisible(true);
            registerForm.setVisible(false);
            loginError.setVisible(false);
        });
        
        registerTab.setOnAction(e -> {
            registerTab.setStyle("-fx-background-color: #4f7cff; -fx-text-fill: white; -fx-font-weight: bold; " +
                                 "-fx-padding: 8px 24px; -fx-background-radius: 20px;");
            loginTab.setStyle("-fx-background-color: transparent; -fx-text-fill: #6b7a8a; " +
                              "-fx-padding: 8px 24px; -fx-background-radius: 20px;");
            loginForm.setVisible(false);
            registerForm.setVisible(true);
            regError.setVisible(false);
        });
        
        card.getChildren().addAll(title, subtitle, tabs, loginForm, registerForm);
        root.getChildren().add(card);
        
        scene = new Scene(root, 500, 700);
    }
    
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        if (username.isEmpty() || password.isEmpty()) {
            loginError.setText("Please fill in all fields");
            loginError.setVisible(true);
            return;
        }
        
        loginBtn.setDisable(true);
        loginBtn.setText("Logging in...");
        loginError.setVisible(false);
        
        new Thread(() -> {
            try {
                User user = authService.login(username, password);
                Platform.runLater(() -> {
                    loginBtn.setDisable(false);
                    loginBtn.setText("Login");
                    NodeShareApp.getInstance().showMainApp();
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    loginBtn.setDisable(false);
                    loginBtn.setText("Login");
                    loginError.setText(ex.getMessage());
                    loginError.setVisible(true);
                });
            }
        }).start();
    }
    
    private void handleRegister() {
        String username = regUsernameField.getText().trim();
        String email = regEmailField.getText().trim();
        String password = regPasswordField.getText();
        String confirm = regConfirmField.getText();
        
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            regError.setText("Please fill in all fields");
            regError.setVisible(true);
            return;
        }
        
        if (password.length() < 6) {
            regError.setText("Password must be at least 6 characters");
            regError.setVisible(true);
            return;
        }
        
        if (!password.equals(confirm)) {
            regError.setText("Passwords do not match");
            regError.setVisible(true);
            return;
        }
        
        registerBtn.setDisable(true);
        registerBtn.setText("Creating account...");
        regError.setVisible(false);
        
        new Thread(() -> {
            try {
                User user = authService.register(username, email, password);
                Platform.runLater(() -> {
                    registerBtn.setDisable(false);
                    registerBtn.setText("Create Account");
                    NodeShareApp.getInstance().showMainApp();
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    registerBtn.setDisable(false);
                    registerBtn.setText("Create Account");
                    regError.setText(ex.getMessage());
                    regError.setVisible(true);
                });
            }
        }).start();
    }
    
    public Scene getScene() {
        return scene;
    }
}
