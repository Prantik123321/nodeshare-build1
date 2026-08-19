package com.prantik.nodeshare.controllers;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;  // ✅ ADD THIS IMPORT
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.application.Platform;
import com.prantik.nodeshare.models.Node;
import com.prantik.nodeshare.services.NodeService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CreateNodeController {
    
    private Stage stage;
    private VBox root;
    private TextField titleField;
    private TextArea contentArea;
    private ComboBox<String> typeCombo;
    private VBox fileListContainer;
    private List<File> selectedFiles;
    private Button shareBtn;
    private Label errorLabel;
    private NodeService nodeService;
    private Runnable onSuccessCallback;
    
    public CreateNodeController() {
        this.nodeService = NodeService.getInstance();
        this.selectedFiles = new ArrayList<>();
        createModal();
    }
    
    private void createModal() {
        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle("Share Node");
        
        root = new VBox(16);
        root.setPadding(new Insets(32, 32, 32, 32));
        root.setStyle("-fx-background-color: white; -fx-background-radius: 20px;");
        root.setPrefWidth(500);
        root.setMaxWidth(500);
        
        // Header
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Text title = new Text("📝 Share Node");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-fill: #1a2332;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button closeBtn = new Button("✕");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #6b7a8a; -fx-font-size: 20px; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> stage.close());
        
        header.getChildren().addAll(title, spacer, closeBtn);
        
        // Form
        VBox form = new VBox(12);
        
        // Title
        titleField = new TextField();
        titleField.setPromptText("Title *");
        titleField.setStyle("-fx-padding: 12px; -fx-background-radius: 10px; " +
                           "-fx-border-color: #e1e5eb; -fx-border-radius: 10px; -fx-font-size: 14px;");
        
        // Type
        typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("Note", "Homework", "Important", "Announcement", "Other");
        typeCombo.setValue("Note");
        typeCombo.setStyle("-fx-padding: 8px; -fx-background-radius: 10px; " +
                          "-fx-border-color: #e1e5eb; -fx-border-radius: 10px; -fx-font-size: 14px;");
        typeCombo.setPrefWidth(Double.MAX_VALUE);
        
        // Content
        contentArea = new TextArea();
        contentArea.setPromptText("Write something...");
        contentArea.setPrefHeight(120);
        contentArea.setStyle("-fx-background-radius: 10px; -fx-border-color: #e1e5eb; " +
                            "-fx-border-radius: 10px; -fx-padding: 8px; -fx-font-size: 14px;");
        
        // File upload
        VBox fileSection = new VBox(8);
        Label fileLabel = new Label("📎 Attachments");
        fileLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #1a2332;");
        
        Button uploadBtn = new Button("Choose Files");
        uploadBtn.setStyle("-fx-background-color: #f0f2f5; -fx-text-fill: #1a2332; -fx-padding: 8px 16px; " +
                          "-fx-background-radius: 8px; -fx-cursor: hand;");
        uploadBtn.setOnAction(e -> chooseFiles());
        
        fileListContainer = new VBox(4);
        fileListContainer.setPadding(new Insets(4, 0, 0, 0));
        
        fileSection.getChildren().addAll(fileLabel, uploadBtn, fileListContainer);
        
        // Error
        errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 13px;");
        errorLabel.setVisible(false);
        
        // Buttons
        HBox actions = new HBox(12);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setPadding(new Insets(8, 0, 0, 0));
        
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #6b7a8a; -fx-font-size: 14px; " +
                          "-fx-padding: 10px 20px; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> stage.close());
        
        shareBtn = new Button("Share Node");
        shareBtn.setStyle("-fx-background-color: #4f7cff; -fx-text-fill: white; -fx-font-weight: bold; " +
                         "-fx-padding: 10px 30px; -fx-background-radius: 10px; -fx-font-size: 14px; -fx-cursor: hand;");
        shareBtn.setOnAction(e -> handleShare());
        
        actions.getChildren().addAll(cancelBtn, shareBtn);
        
        form.getChildren().addAll(titleField, typeCombo, contentArea, fileSection, errorLabel);
        
        root.getChildren().addAll(header, form, actions);
        
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        stage.setScene(scene);
    }
    
    private void chooseFiles() {
        FileChooser fileChooser = new FileChooser();  // ✅ Now works with import
        fileChooser.setTitle("Select Files");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("All Files", "*.*"),
            new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.webp"),
            new FileChooser.ExtensionFilter("Documents", "*.pdf", "*.doc", "*.docx"),
            new FileChooser.ExtensionFilter("Text", "*.txt")
        );
        
        List<File> files = fileChooser.showOpenMultipleDialog(stage);
        if (files != null) {
            selectedFiles.addAll(files);
            updateFileList();
        }
    }
    
    private void updateFileList() {
        fileListContainer.getChildren().clear();
        for (File file : selectedFiles) {
            HBox item = new HBox(8);
            item.setAlignment(Pos.CENTER_LEFT);
            item.setStyle("-fx-background-color: #f0f2f5; -fx-background-radius: 8px; -fx-padding: 6px 12px;");
            
            Label name = new Label("📎 " + file.getName());
            name.setStyle("-fx-text-fill: #1a2332; -fx-font-size: 13px;");
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            
            Button removeBtn = new Button("✕");
            removeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-cursor: hand;");
            removeBtn.setOnAction(e -> {
                selectedFiles.remove(file);
                updateFileList();
            });
            
            item.getChildren().addAll(name, spacer, removeBtn);
            fileListContainer.getChildren().add(item);
        }
    }
    
    private void handleShare() {
        String title = titleField.getText().trim();
        String content = contentArea.getText().trim();
        String type = typeCombo.getValue();
        
        if (title.isEmpty()) {
            showError("Please enter a title");
            return;
        }
        
        shareBtn.setDisable(true);
        shareBtn.setText("Sharing...");
        errorLabel.setVisible(false);
        
        new Thread(() -> {
            try {
                Node node = new Node();
                node.setTitle(title);
                node.setContent(content);
                node.setTags(List.of(type));
                
                // If files selected, handle file upload (simplified)
                if (!selectedFiles.isEmpty()) {
                    File firstFile = selectedFiles.get(0);
                    node.setFileName(firstFile.getName());
                    node.setFileUrl("https://nodeshare.com/files/" + firstFile.getName());
                    node.setFileSize(firstFile.length());
                }
                
                Node created = nodeService.createNode(node);
                
                Platform.runLater(() -> {
                    shareBtn.setDisable(false);
                    shareBtn.setText("Share Node");
                    stage.close();
                    if (onSuccessCallback != null) {
                        onSuccessCallback.run();
                    }
                });
                
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    shareBtn.setDisable(false);
                    shareBtn.setText("Share Node");
                    showError(ex.getMessage());
                });
            }
        }).start();
    }
    
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
    
    public void show() {
        stage.showAndWait();
    }
    
    public void setOnSuccess(Runnable callback) {
        this.onSuccessCallback = callback;
    }
}
