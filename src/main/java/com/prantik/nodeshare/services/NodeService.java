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
            
            return nodes;
            
        } catch (Exception e) {
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
            
            return createdNode;
            
        } catch (Exception e) {
            throw new Exception("Failed to create node: " + e.getMessage());
        }
    }
    
    // Add the missing deleteNode method
    public boolean deleteNode(String nodeId) throws Exception {
        try {
            ApiClient.ApiResponse response = apiClient.delete("/nodes/" + nodeId);
            
            if (!response.success) {
                throw new Exception(response.error != null ? response.error : "Failed to delete node");
            }
            
            return true;
            
        } catch (Exception e) {
            throw new Exception("Failed to delete node: " + e.getMessage());
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
}
