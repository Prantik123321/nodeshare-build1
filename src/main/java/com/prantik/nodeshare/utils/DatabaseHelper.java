package com.prantik.nodeshare.utils;

import java.sql.*;
import java.io.File;

public class DatabaseHelper {
    private static DatabaseHelper instance;
    private Connection connection;
    
    private DatabaseHelper() {}
    
    public static DatabaseHelper getInstance() {
        if (instance == null) {
            instance = new DatabaseHelper();
        }
        return instance;
    }
    
    public void initDatabase() {
        try {
            String userHome = System.getProperty("user.home");
            String dbPath = userHome + File.separator + ".nodeshare" + File.separator + "nodeshare.db";
            
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            
            createTables();
            
            System.out.println("✅ Database initialized at: " + dbPath);
            
        } catch (Exception e) {
            System.err.println("❌ Database initialization failed: " + e.getMessage());
        }
    }
    
    private void createTables() throws SQLException {
        String createUsersTable = """
            CREATE TABLE IF NOT EXISTS users (
                id TEXT PRIMARY KEY,
                username TEXT NOT NULL,
                email TEXT NOT NULL,
                token TEXT,
                created_at TEXT
            )
        """;
        
        String createNodesTable = """
            CREATE TABLE IF NOT EXISTS nodes (
                id TEXT PRIMARY KEY,
                user_id TEXT,
                username TEXT,
                title TEXT NOT NULL,
                content TEXT,
                file_url TEXT,
                file_name TEXT,
                file_size INTEGER,
                file_type TEXT,
                created_at TEXT,
                updated_at TEXT,
                is_synced INTEGER DEFAULT 1
            )
        """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createUsersTable);
            stmt.execute(createNodesTable);
        }
    }
    
    public Connection getConnection() {
        return connection;
    }
    
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("❌ Failed to close database: " + e.getMessage());
        }
    }
}
