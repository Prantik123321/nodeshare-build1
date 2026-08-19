package com.prantik.nodeshare.services;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.prantik.nodeshare.models.Node;
import com.prantik.nodeshare.utils.DatabaseHelper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NodeService {
    private static NodeService instance;
    private final ApiClient apiClient;
    private final Gson gson;
    private final DatabaseHelper dbHelper;
    
    private NodeService() {
        this.apiClient = ApiClient.getInstance();
        this.gson = new Gson();
        this.dbHelper = DatabaseHelper.getInstance();
    }
    
    public static NodeService getInstance() {
        if (instance == null) {
            instance = new NodeService();
        }
        return instance;
    }
    
    public List<Node> getNodes(int limit, int skip) throws Exception {
        try {
            String endpoint = "/nodes?limit=" + limit + "&skip=" + skip;
            ApiClient.ApiResponse response = apiClient.get(endpoint);
            
            if (!response.success) {
                throw new Exception(response.error != null ? response.error : "Failed to fetch nodes");
            }
            
            JsonObject jsonResponse = gson.fromJson(response.data, JsonObject.class);
            JsonObject data = jsonResponse.getAsJsonObject("data");
            JsonArray nodesArray = data.getAsJsonArray("nodes");
            
            List<Node> nodes = new ArrayList<>();
            for (int i = 0; i < nodesArray.size(); i++) {
                JsonObject nodeJson = nodesArray.get(i).getAsJsonObject();
                Node node = new Node();
                node.setId(nodeJson.get("id").getAsString());
                node.setUserId(nodeJson.get("user_id").getAsString());
                node.setUsername(nodeJson.get("username").getAsString());
                node.setTitle(nodeJson.get("title").getAsString());
                if (nodeJson.has("content") && !nodeJson.get("content").isJsonNull()) {
                    node.setContent(nodeJson.get("content").getAsString());
                }
                if (nodeJson.has("file_url") && !nodeJson.get("file_url").isJsonNull()) {
                    node.setFileUrl(nodeJson.get("file_url").getAsString());
                }
                if (nodeJson.has("file_name") && !nodeJson.get("file_name").isJsonNull()) {
                    node.setFileName(nodeJson.get("file_name").getAsString());
                }
                if (nodeJson.has("created_at")) {
                    node.setCreatedAt(nodeJson.get("created_at").getAsString());
                }
                nodes.add(node);
            }
            
            // Cache nodes
            for (Node node : nodes) {
                cacheNode(node);
            }
            
            return nodes;
            
        } catch (Exception e) {
            // Try to load from cache
            return getCachedNodes();
        }
    }
    
    public Node createNode(Node node) throws Exception {
        try {
            JsonObject request = new JsonObject();
            request.addProperty("title", node.getTitle());
            request.addProperty("content", node.getContent() != null ? node.getContent() : "");
            
            if (node.getFileUrl() != null) {
                request.addProperty("file_url", node.getFileUrl());
                request.addProperty("file_name", node.getFileName());
                request.addProperty("file_size", node.getFileSize());
                request.addProperty("file_type", node.getFileType());
            }
            
            ApiClient.ApiResponse response = apiClient.post("/nodes", request);
            
            if (!response.success) {
                throw new Exception(response.error != null ? response.error : "Failed to create node");
            }
            
            JsonObject jsonResponse = gson.fromJson(response.data, JsonObject.class);
            JsonObject data = jsonResponse.getAsJsonObject("data");
            JsonObject nodeJson = data.getAsJsonObject("node");
            
            Node createdNode = new Node();
            createdNode.setId(nodeJson.get("id").getAsString());
            createdNode.setUserId(nodeJson.get("user_id").getAsString());
            createdNode.setUsername(nodeJson.get("username").getAsString());
            createdNode.setTitle(nodeJson.get("title").getAsString());
            if (nodeJson.has("content") && !nodeJson.get("content").isJsonNull()) {
                createdNode.setContent(nodeJson.get("content").getAsString());
            }
            createdNode.setCreatedAt(nodeJson.get("created_at").getAsString());
            
            cacheNode(createdNode);
            return createdNode;
            
        } catch (Exception e) {
            // Queue for offline
            queueNodeForSync(node);
            throw new Exception("Node queued for sync when online: " + e.getMessage());
        }
    }
    
    public boolean deleteNode(String nodeId) throws Exception {
        try {
            ApiClient.ApiResponse response = apiClient.delete("/nodes/" + nodeId);
            
            if (!response.success) {
                throw new Exception(response.error != null ? response.error : "Failed to delete node");
            }
            
            deleteCachedNode(nodeId);
            return true;
            
        } catch (Exception e) {
            queueDeletionForSync(nodeId);
            throw new Exception("Deletion queued for sync when online: " + e.getMessage());
        }
    }
    
    // Cache methods
    private void cacheNode(Node node) {
        try {
            Connection conn = dbHelper.getConnection();
            String sql = """
                INSERT OR REPLACE INTO nodes 
                (id, user_id, username, title, content, file_url, file_name, file_size, file_type, created_at, is_synced)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1)
            """;
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, node.getId());
            stmt.setString(2, node.getUserId());
            stmt.setString(3, node.getUsername());
            stmt.setString(4, node.getTitle());
            stmt.setString(5, node.getContent());
            stmt.setString(6, node.getFileUrl());
            stmt.setString(7, node.getFileName());
            stmt.setLong(8, node.getFileSize());
            stmt.setString(9, node.getFileType());
            stmt.setString(10, node.getCreatedAt());
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Failed to cache node: " + e.getMessage());
        }
    }
    
    private List<Node> getCachedNodes() {
        List<Node> nodes = new ArrayList<>();
        try {
            Connection conn = dbHelper.getConnection();
            String sql = "SELECT * FROM nodes ORDER BY created_at DESC LIMIT 50";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                Node node = new Node();
                node.setId(rs.getString("id"));
                node.setUserId(rs.getString("user_id"));
                node.setUsername(rs.getString("username"));
                node.setTitle(rs.getString("title"));
                node.setContent(rs.getString("content"));
                node.setFileUrl(rs.getString("file_url"));
                node.setFileName(rs.getString("file_name"));
                node.setFileSize(rs.getLong("file_size"));
                node.setFileType(rs.getString("file_type"));
                node.setCreatedAt(rs.getString("created_at"));
                nodes.add(node);
            }
            
        } catch (SQLException e) {
            System.err.println("Failed to get cached nodes: " + e.getMessage());
        }
        return nodes;
    }
    
    private void deleteCachedNode(String nodeId) {
        try {
            Connection conn = dbHelper.getConnection();
            String sql = "DELETE FROM nodes WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, nodeId);
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Failed to delete cached node: " + e.getMessage());
        }
    }
    
    private void queueNodeForSync(Node node) {
        try {
            Connection conn = dbHelper.getConnection();
            String sql = """
                INSERT INTO offline_queue (operation, node_id, data, created_at)
                VALUES (?, ?, ?, datetime('now'))
            """;
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, "create");
            stmt.setString(2, node.getId());
            stmt.setString(3, gson.toJson(node));
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Failed to queue node for sync: " + e.getMessage());
        }
    }
    
    private void queueDeletionForSync(String nodeId) {
        try {
            Connection conn = dbHelper.getConnection();
            String sql = """
                INSERT INTO offline_queue (operation, node_id, data, created_at)
                VALUES (?, ?, ?, datetime('now'))
            """;
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, "delete");
            stmt.setString(2, nodeId);
            stmt.setString(3, "{\"id\":\"" + nodeId + "\"}");
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Failed to queue deletion for sync: " + e.getMessage());
        }
    }
}