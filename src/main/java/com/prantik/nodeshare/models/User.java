package com.prantik.nodeshare.models;

public class User {
    private String id;
    private String username;
    private String email;
    private String token;
    private boolean isOnline;
    private String createdAt;
    
    public User() {}
    
    public User(String id, String username, String email, String token) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.token = token;
        this.isOnline = true;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    
    public boolean isOnline() { return isOnline; }
    public void setOnline(boolean online) { isOnline = online; }
    
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}