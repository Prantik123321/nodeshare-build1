package com.prantik.nodeshare.services;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.prantik.nodeshare.models.User;

public class AuthService {
    private static AuthService instance;
    private final ApiClient apiClient;
    private final Gson gson;
    
    private AuthService() {
        this.apiClient = ApiClient.getInstance();
        this.gson = new Gson();
    }
    
    public static AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }
    
    public User register(String username, String email, String password) throws Exception {
        try {
            JsonObject request = new JsonObject();
            request.addProperty("username", username);
            request.addProperty("email", email);
            request.addProperty("password", password);
            
            ApiClient.ApiResponse response = apiClient.post("/auth/register", request);
            
            if (!response.success) {
                throw new Exception(response.error != null ? response.error : "Registration failed");
            }
            
            JsonObject jsonResponse = gson.fromJson(response.data, JsonObject.class);
            JsonObject data = jsonResponse.getAsJsonObject("data");
            
            String token = data.get("token").getAsString();
            JsonObject userJson = data.getAsJsonObject("user");
            
            User user = new User(
                userJson.get("id").getAsString(),
                userJson.get("username").getAsString(),
                userJson.get("email").getAsString(),
                token
            );
            
            apiClient.setAuthToken(token);
            return user;
            
        } catch (Exception e) {
            throw new Exception("Registration error: " + e.getMessage());
        }
    }
    
    public User login(String email, String password) throws Exception {
        try {
            JsonObject request = new JsonObject();
            request.addProperty("email", email);
            request.addProperty("password", password);
            
            ApiClient.ApiResponse response = apiClient.post("/auth/login", request);
            
            if (!response.success) {
                throw new Exception(response.error != null ? response.error : "Login failed");
            }
            
            JsonObject jsonResponse = gson.fromJson(response.data, JsonObject.class);
            JsonObject data = jsonResponse.getAsJsonObject("data");
            
            String token = data.get("token").getAsString();
            JsonObject userJson = data.getAsJsonObject("user");
            
            User user = new User(
                userJson.get("id").getAsString(),
                userJson.get("username").getAsString(),
                userJson.get("email").getAsString(),
                token
            );
            
            apiClient.setAuthToken(token);
            return user;
            
        } catch (Exception e) {
            throw new Exception("Login error: " + e.getMessage());
        }
    }
    
    public void logout() {
        apiClient.clearAuthToken();
    }
}