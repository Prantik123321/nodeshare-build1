package com.prantik.nodeshare.controllers;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.application.Platform;
import com.prantik.nodeshare.NodeShareApp;
import com.prantik.nodeshare.models.Node;
import com.prantik.nodeshare.services.NodeService;

import java.util.List;

public class FeedController {
    private Scene scene;
    private NodeService nodeService;
    private VBox feedContainer;
    private ScrollPane scrollPane;
    private Label statusLabel;
    private Button createNodeBtn;
    private TextField searchField;
    
    public FeedController() {
        this.nodeService = NodeService.getInstance();
        createScene();
        loadNodes();
    }
    
    private void createScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f0f2f5;");
        
        // Header
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(12, 24));
        header.setStyle("-fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 4, 0, 0, 2);");
        
        Text title = new Text("📦 NodeShare");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-fill: #1a2332;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        searchField = new TextField();
        searchField.setPromptText("Search nodes...");
        searchField.setStyle("-fx-background-color: #f0f2f5; -fx-background-radius: 20px; -fx-padding: 8px 16px; -fx-pref-width: 250px;");
        
        statusLabel = new Label("● Online");
        statusLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-size: 13px; -fx-font-weight: 600; " +
                             "-fx-padding: 4px 12px; -fx-background-color: rgba(46, 204, 113, 0.15); -fx-background-radius: 12px;");
        
        Button profileBtn = new Button("👤");
        profileBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 20px; -fx-cursor: hand;");
        
        Button settingsBtn = new Button("⚙️");
        settingsBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 20px; -fx-cursor: hand;");
        
        header.getChildren().addAll(title, spacer, searchField, statusLabel, profileBtn, settingsBtn);
        root.setTop(header);
        
        // Sidebar
        VBox sidebar = new VBox(4);
        sidebar.setPadding(new Insets(16, 0));
        sidebar.setPrefWidth(200);
        sidebar.setStyle("-fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 4, 0, 0, 2);");
        
        String[] navItems = {"🏠 Feed", "📋 My Nodes", "📥 Downloads", "⭐ Saved"};
        for (String item : navItems) {
            Button navBtn = new Button(item);
            navBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #6b7a8a; -fx-font-size: 14px; " +
                            "-fx-padding: 12px 20px; -fx-alignment: center-left; -fx-pref-width: 200px;");
            if (item.equals("🏠 Feed")) {
                navBtn.setStyle("-fx-background-color: #4f7cff; -fx-text-fill: white; -fx-font-size: 14px; " +
                                "-fx-padding: 12px 20px; -fx-background-radius: 0 20px 20px 0; -fx-pref-width: 200px;");
            }
            sidebar.getChildren().add(navBtn);
        }
        
        VBox sidebarBottom = new VBox(8);
        sidebarBottom.setPadding(new Insets(16, 0, 0, 0));
        sidebarBottom.setStyle("-fx-border-color: #e1e5eb; -fx-border-width: 1 0 0 0; -fx-padding: 16px 0 0 0;");
        
        createNodeBtn = new Button("➕ Share Node");
        createNodeBtn.setStyle("-fx-background-color: #4f7cff; -fx-text-fill: white; -fx-font-weight: bold; " +
                               "-fx-padding: 12px; -fx-background-radius: 10px; -fx-pref-width: 180px;");
        createNodeBtn.setOnAction(e -> showCreateNodeModal());
        
        sidebarBottom.getChildren().add(createNodeBtn);
        sidebar.getChildren().add(sidebarBottom);
        root.setLeft(sidebar);
        
        // Feed
        feedContainer = new VBox(16);
        feedContainer.setPadding(new Insets(20));
        
        scrollPane = new ScrollPane(feedContainer);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-padding: 0 20px 20px 20px;");
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        root.setCenter(scrollPane);
        
        scene = new Scene(root, 1000, 700);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
    }
    
    private void loadNodes() {
        feedContainer.getChildren().clear();
        Label loading = new Label("Loading nodes...");
        loading.setStyle("-fx-font-size: 14px; -fx-text-fill: #6b7a8a;");
        feedContainer.getChildren().add(loading);
        
        new Thread(() -> {
            try {
                List<Node> nodes = nodeService.getNodes(50, 0);
                Platform.runLater(() -> {
                    feedContainer.getChildren().clear();
                    if (nodes.isEmpty()) {
                        showEmptyState();
                    } else {
                        for (Node node : nodes) {
                            feedContainer.getChildren().add(createNodeCard(node));
                        }
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    feedContainer.getChildren().clear();
                    Label error = new Label("❌ " + e.getMessage());
                    error.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 14px;");
                    feedContainer.getChildren().add(error);
                });
            }
        }).start();
    }
    
    private VBox createNodeCard(Node node) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16px; " +
                      "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 8, 0, 0, 2);");
        card.setMaxWidth(800);
        
        // Header
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label avatar = new Label(node.getUsername().substring(0, 1).toUpperCase());
        avatar.setStyle("-fx-background-color: #4f7cff; -fx-background-radius: 50%; " +
                        "-fx-pref-width: 36px; -fx-pref-height: 36px; -fx-alignment: center; " +
                        "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        
        VBox userInfo = new VBox(2);
        Label username = new Label(node.getUsername());
        username.setStyle("-fx-font-weight: 600; -fx-text-fill: #1a2332;");
        Label time = new Label(node.getCreatedAt() != null ? node.getCreatedAt().substring(0, 10) : "Just now");
        time.setStyle("-fx-text-fill: #8a9baa; -fx-font-size: 12px;");
        userInfo.getChildren().addAll(username, time);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label type = new Label("Note");
        type.setStyle("-fx-background-color: #f0f2f5; -fx-text-fill: #6b7a8a; -fx-font-size: 11px; " +
                      "-fx-font-weight: 600; -fx-padding: 4px 12px; -fx-background-radius: 12px;");
        
        header.getChildren().addAll(avatar, userInfo, spacer, type);
        
        // Content
        Label title = new Label(node.getTitle());
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: 600; -fx-text-fill: #1a2332;");
        
        Label content = new Label(node.getContent() != null ? node.getContent() : "");
        content.setStyle("-fx-text-fill: #4a5a6a; -fx-font-size: 14px; -fx-wrap-text: true;");
        content.setMaxWidth(700);
        
        // Actions
        HBox actions = new HBox(8);
        actions.setPadding(new Insets(8, 0, 0, 0));
        
        Button downloadBtn = new Button("📥 Download");
        downloadBtn.setStyle("-fx-background-color: #4f7cff; -fx-text-fill: white; -fx-font-size: 12px; " +
                             "-fx-font-weight: 500; -fx-padding: 6px 16px; -fx-background-radius: 8px;");
        
        Button deleteBtn = new Button("🗑️ Delete");
        deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 12px; " +
                           "-fx-font-weight: 500; -fx-padding: 6px 16px; -fx-background-radius: 8px;");
        deleteBtn.setOnAction(e -> deleteNode(node.getId()));
        
        actions.getChildren().addAll(downloadBtn, deleteBtn);
        
        card.getChildren().addAll(header, title, content, actions);
        return card;
    }
    
    private void showEmptyState() {
        VBox empty = new VBox(12);
        empty.setAlignment(Pos.CENTER);
        empty.setPadding(new Insets(60, 20));
        
        Label icon = new Label("📦");
        icon.setStyle("-fx-font-size: 48px;");
        Label title = new Label("No nodes yet");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: 600; -fx-text-fill: #1a2332;");
        Label desc = new Label("Be the first to share something!");
        desc.setStyle("-fx-text-fill: #8a9baa; -fx-font-size: 14px;");
        
        Button createBtn = new Button("Share a Node");
        createBtn.setStyle("-fx-background-color: #4f7cff; -fx-text-fill: white; -fx-font-weight: bold; " +
                           "-fx-padding: 12px 30px; -fx-background-radius: 10px;");
        createBtn.setOnAction(e -> showCreateNodeModal());
        
        empty.getChildren().addAll(icon, title, desc, createBtn);
        feedContainer.getChildren().add(empty);
    }
    
    private void showCreateNodeModal() {
        // TODO: Implement create node modal
        System.out.println("📝 Create node modal - To be implemented");
    }
    
    private void deleteNode(String nodeId) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Node");
        alert.setHeaderText("Are you sure?");
        alert.setContentText("This action cannot be undone.");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                new Thread(() -> {
                    try {
                        nodeService.deleteNode(nodeId);
                        Platform.runLater(() -> loadNodes());
                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            Alert error = new Alert(Alert.AlertType.ERROR);
                            error.setTitle("Error");
                            error.setHeaderText("Failed to delete node");
                            error.setContentText(e.getMessage());
                            error.showAndWait();
                        });
                    }
                }).start();
            }
        });
    }
    
    public Scene getScene() {
        return scene;
    }
}