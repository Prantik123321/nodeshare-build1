package com.prantik.nodeshare.services;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.prantik.nodeshare.models.Node;
import com.prantik.nodeshare.models.User;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class RealtimeService {
    private static RealtimeService instance;
    private ApiClient apiClient;
    private Gson gson;
    private boolean isConnected;
    private List<Consumer<Node>> nodeCreatedListeners;
    private List<Consumer<Node>> nodeUpdatedListeners;
    private List<Consumer<String>> nodeDeletedListeners;
    private List<Consumer<User>> userOnlineListeners;
    private List<Consumer<User>> userOfflineListeners;
    private Thread pollingThread;
    private boolean running;
    
    private RealtimeService() {
        this.apiClient = ApiClient.getInstance();
        this.gson = new Gson();
        this.nodeCreatedListeners = new ArrayList<>();
        this.nodeUpdatedListeners = new ArrayList<>();
        this.nodeDeletedListeners = new ArrayList<>();
        this.userOnlineListeners = new ArrayList<>();
        this.userOfflineListeners = new ArrayList<>();
        this.isConnected = false;
        this.running = false;
    }
    
    public static RealtimeService getInstance() {
        if (instance == null) {
            instance = new RealtimeService();
        }
        return instance;
    }
    
    public void connect() {
        if (isConnected) return;
        
        String token = apiClient.getAuthToken();
        if (token == null || token.isEmpty()) {
            System.out.println("❌ Cannot connect: No auth token");
            return;
        }
        
        isConnected = true;
        running = true;
        startPolling();
        System.out.println("📡 Realtime service connected");
    }
    
    public void disconnect() {
        running = false;
        isConnected = false;
        if (pollingThread != null) {
            pollingThread.interrupt();
        }
        System.out.println("📡 Realtime service disconnected");
    }
    
    private void startPolling() {
        pollingThread = new Thread(() -> {
            while (running) {
                try {
                    // Poll for new nodes
                    pollForUpdates();
                    Thread.sleep(5000); // Poll every 5 seconds
                } catch (InterruptedException e) {
                    break;
                } catch (Exception e) {
                    System.err.println("Polling error: " + e.getMessage());
                }
            }
        });
        pollingThread.setDaemon(true);
        pollingThread.start();
    }
    
    private void pollForUpdates() {
        try {
            ApiClient.ApiResponse response = apiClient.get("/nodes?limit=10&skip=0");
            if (response.success) {
                // Parse and notify listeners
                JsonObject jsonResponse = gson.fromJson(response.data, JsonObject.class);
                JsonObject data = jsonResponse.getAsJsonObject("data");
                if (data != null && data.has("nodes")) {
                    // Notify listeners with new nodes
                    // In production, this would use Pusher or WebSockets
                }
            }
        } catch (Exception e) {
            // Silent fail for polling
        }
    }
    
    // Event listeners
    public void onNodeCreated(Consumer<Node> listener) {
        nodeCreatedListeners.add(listener);
    }
    
    public void onNodeUpdated(Consumer<Node> listener) {
        nodeUpdatedListeners.add(listener);
    }
    
    public void onNodeDeleted(Consumer<String> listener) {
        nodeDeletedListeners.add(listener);
    }
    
    public void onUserOnline(Consumer<User> listener) {
        userOnlineListeners.add(listener);
    }
    
    public void onUserOffline(Consumer<User> listener) {
        userOfflineListeners.add(listener);
    }
    
    // Notification methods
    public void notifyNodeCreated(Node node) {
        for (Consumer<Node> listener : nodeCreatedListeners) {
            try {
                listener.accept(node);
            } catch (Exception e) {
                System.err.println("Error in node created listener: " + e.getMessage());
            }
        }
    }
    
    public void notifyNodeUpdated(Node node) {
        for (Consumer<Node> listener : nodeUpdatedListeners) {
            try {
                listener.accept(node);
            } catch (Exception e) {
                System.err.println("Error in node updated listener: " + e.getMessage());
            }
        }
    }
    
    public void notifyNodeDeleted(String nodeId) {
        for (Consumer<String> listener : nodeDeletedListeners) {
            try {
                listener.accept(nodeId);
            } catch (Exception e) {
                System.err.println("Error in node deleted listener: " + e.getMessage());
            }
        }
    }
    
    public boolean isConnected() {
        return isConnected;
    }
}