package com.prantik.nodeshare.services;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.*;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ApiClient {
    private static ApiClient instance;
    private static final String API_BASE_URL = "https://nodeshare-backend-gw20nmsh9-prantik-s-projects1.vercel.app/api";
    
    private final OkHttpClient client;
    private final Gson gson;
    private String authToken;
    
    private ApiClient() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
    }
    
    public static ApiClient getInstance() {
        if (instance == null) {
            instance = new ApiClient();
        }
        return instance;
    }
    
    public void setAuthToken(String token) {
        this.authToken = token;
    }
    
    public String getAuthToken() {
        return authToken;
    }
    
    public void clearAuthToken() {
        this.authToken = null;
    }
    
    private Request.Builder getRequestBuilder(String endpoint) {
        String url = API_BASE_URL + endpoint;
        Request.Builder builder = new Request.Builder().url(url);
        
        if (authToken != null && !authToken.isEmpty()) {
            builder.addHeader("Authorization", "Bearer " + authToken);
        }
        
        builder.addHeader("Content-Type", "application/json");
        builder.addHeader("Accept", "application/json");
        
        return builder;
    }
    
    public ApiResponse get(String endpoint) throws IOException {
        Request request = getRequestBuilder(endpoint).get().build();
        return execute(request);
    }
    
    public ApiResponse post(String endpoint, Object body) throws IOException {
        String json = gson.toJson(body);
        RequestBody requestBody = RequestBody.create(
                json,
                MediaType.parse("application/json")
        );
        
        Request request = getRequestBuilder(endpoint)
                .post(requestBody)
                .build();
        
        return execute(request);
    }
    
    public ApiResponse put(String endpoint, Object body) throws IOException {
        String json = gson.toJson(body);
        RequestBody requestBody = RequestBody.create(
                json,
                MediaType.parse("application/json")
        );
        
        Request request = getRequestBuilder(endpoint)
                .put(requestBody)
                .build();
        
        return execute(request);
    }
    
    public ApiResponse delete(String endpoint) throws IOException {
        Request request = getRequestBuilder(endpoint).delete().build();
        return execute(request);
    }
    
    private ApiResponse execute(Request request) throws IOException {
        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";
            boolean success = response.isSuccessful();
            
            if (success) {
                return new ApiResponse(true, response.code(), responseBody, null);
            } else {
                return new ApiResponse(false, response.code(), null, responseBody);
            }
        }
    }
    
    public class ApiResponse {
        public final boolean success;
        public final int statusCode;
        public final String data;
        public final String error;
        
        public ApiResponse(boolean success, int statusCode, String data, String error) {
            this.success = success;
            this.statusCode = statusCode;
            this.data = data;
            this.error = error;
        }
    }
}
