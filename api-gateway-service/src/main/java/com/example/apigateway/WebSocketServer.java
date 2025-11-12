package com.example.apigateway;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.example.apigateway.security.SSLClientUtils;
import io.javalin.Javalin;
import io.javalin.websocket.WsContext;
import org.eclipse.jetty.server.AbstractConnector;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * WebSocketServer - Handles WebSocket connections from React Dashboard
 * 
 * Responsibilities:
 * - Accept WebSocket connections from dashboard
 * - Receive commands (e.g., "fetchWeather")
 * - Call external APIs based on commands
 * - Send results back to dashboard via WebSocket
 * - Broadcast service status updates
 * 
 * Protocol:
 * Client → Server:
 * {
 *   "command": "fetchWeather",
 *   "city": "Colombo"
 * }
 * 
 * Server → Client:
 * {
 *   "type": "WEATHER_RESPONSE",
 *   "payload": {
 *     "location": "Colombo, Sri Lanka",
 *     "temperature": 28.5,
 *     "condition": "Sunny"
 *   }
 * }
 */
public class WebSocketServer {
    private final Javalin app;
    private final ExternalApiClient apiClient;
    private final CopyOnWriteArraySet<WsContext> sessions = new CopyOnWriteArraySet<>();
    private final Gson gson = new Gson();
    
    /**
     * Initialize WebSocket server
     */
    public WebSocketServer(ExternalApiClient apiClient) {
        this.app = Javalin.create(config -> {
            config.plugins.enableCors(cors -> cors.add(it -> it.anyHost()));
        });
        this.apiClient = apiClient;
        setupRoutes();
    }
    
    /**
     * Setup Javalin routes and WebSocket endpoint
     */
    private void setupRoutes() {
        // Health check endpoint
        app.get("/health", ctx -> {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", "UP");
            response.put("service", "ApiGateway");
            response.put("port", 9001);
            ctx.json(response);
        });
        
        // Status endpoint
        app.get("/status", ctx -> {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("service", "ApiGateway");
            response.put("status", "Running");
            response.put("connectedDashboards", sessions.size());
            response.put("timestamp", System.currentTimeMillis());
            ctx.json(response);
        });
        
        // WebSocket endpoint for dashboard
        app.ws("/api", ws -> {
            ws.onConnect(ctx -> {
                System.out.println("[WebSocket] Dashboard connected");
                sessions.add(ctx);
                broadcastStatusUpdate();
            });
            
            ws.onMessage(ctx -> {
                try {
                    handleWebSocketMessage(ctx.message(), ctx);
                } catch (Exception e) {
                    System.err.println("[WebSocket] Error processing message: " + e.getMessage());
                    ctx.send(createErrorResponse("Failed to process command: " + e.getMessage()));
                }
            });
            
            ws.onClose(ctx -> {
                System.out.println("[WebSocket] Dashboard disconnected");
                sessions.remove(ctx);
                broadcastStatusUpdate();
            });
            
            ws.onError(ctx -> {
                System.err.println("[WebSocket] Error: " + ctx.error());
                if (ctx.error() != null) {
                    ctx.error().printStackTrace();
                }
            });
        });
    }
    
    /**
     * Handle incoming WebSocket message from dashboard
     */
    private void handleWebSocketMessage(String message, WsContext ctx) {
        System.out.println("[WebSocket] Received message: " + message);
        
        try {
            JsonObject json = gson.fromJson(message, JsonObject.class);
            String command = json.get("command").getAsString();
            
            switch (command.toLowerCase()) {
                case "fetchweather":
                    handleFetchWeatherCommand(ctx, json);
                    break;
                    
                case "getservicestatus":
                    handleGetServiceStatusCommand(ctx);
                    break;
                    
                case "uploadfile":
                    handleUploadFileCommand(ctx, json);
                    break;
                    
                case "listfiles":
                    handleListFilesCommand(ctx);
                    break;
                    
                case "downloadfile":
                    handleDownloadFileCommand(ctx, json);
                    break;
                    
                case "ping":
                    ctx.send(createPongResponse());
                    break;
                    
                case "listfiles":
                    handleListFilesCommand(ctx);
                    break;
                    
                case "uploadfile":
                    handleUploadFileCommand(ctx, json);
                    break;
                    
                case "downloadfile":
                    handleDownloadFileCommand(ctx, json);
                    break;
                    
                case "deletefile":
                    handleDeleteFileCommand(ctx, json);
                    break;
                    
                default:
                    ctx.send(createErrorResponse("Unknown command: " + command));
            }
        } catch (Exception e) {
            System.err.println("[WebSocket] Error parsing message: " + e.getMessage());
            ctx.send(createErrorResponse("Invalid message format: " + e.getMessage()));
        }
    }
    
    /**
     * Handle fetchWeather command
     */
    private void handleFetchWeatherCommand(WsContext ctx, JsonObject json) {
        try {
            String city = json.get("city").getAsString();
            System.out.println("[ApiGateway] Fetching weather for: " + city);
            
            // Call external API
            ExternalApiClient.WeatherData weatherData = apiClient.fetchWeatherByCity(city);
            
            // Send response to dashboard using Map for proper JSON serialization
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("type", "WEATHER_RESPONSE");
            response.put("location", weatherData.location);
            response.put("temperature", weatherData.temperature);
            response.put("condition", weatherData.condition);
            response.put("timestamp", weatherData.timestamp);
            response.put("status", "success");
            
            ctx.send(gson.toJson(response));
            System.out.println("[ApiGateway] ✓ Weather response sent to dashboard");
            
        } catch (Exception e) {
            System.err.println("[ApiGateway] Error fetching weather: " + e.getMessage());
            ctx.send(createErrorResponse("Failed to fetch weather: " + e.getMessage()));
        }
    }
    
    /**
     * Handle getServiceStatus command
     */
    private void handleGetServiceStatusCommand(WsContext ctx) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("type", "SERVICE_STATUS");
        response.put("service", "ApiGateway");
        response.put("status", "online");
        response.put("port", 9001);
        response.put("connectedDashboards", sessions.size());
        response.put("timestamp", System.currentTimeMillis());
        
        ctx.send(gson.toJson(response));
    }
    
    /**
     * Handle uploadFile command - proxy to Secure File Service
     */
    private void handleUploadFileCommand(WsContext ctx, JsonObject json) {
        try {
            String fileName = json.get("fileName").getAsString();
            String fileData = json.get("fileData").getAsString();
            
            System.out.println("[ApiGateway] Uploading file: " + fileName + " (" + fileData.length() + " chars)");
            
            // Connect to Secure File Service via SSL
            // Use custom SSL context that accepts self-signed certificates
            SSLSocketFactory factory = SSLClientUtils.getSSLSocketFactory();
            SSLSocket socket = (SSLSocket) factory.createSocket("localhost", 9090);
            
            // Enable TLS protocols
            socket.setEnabledProtocols(new String[]{"TLSv1.2", "TLSv1.3"});
            
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            // Skip WELCOME message
            String welcome = in.readLine();
            System.out.println("[ApiGateway] Secure File Service: " + welcome);
            
            // Send STORE command with size - NO println, use write to control exact bytes
            String storeCommand = "STORE::" + fileName + "::" + fileData.length();
            out.println(storeCommand);
            out.flush();
            
            // Send file data as raw bytes without adding newline
            OutputStream rawOut = socket.getOutputStream();
            rawOut.write(fileData.getBytes());
            rawOut.write('\n');  // Single newline to signal end of data
            rawOut.flush();
            
            // Read response
            String response = in.readLine();
            System.out.println("[ApiGateway] Upload response: " + response);
            
            socket.close();
            
            if (response != null && response.startsWith("SUCCESS::")) {
                Map<String, Object> wsResponse = new LinkedHashMap<>();
                wsResponse.put("type", "FILE_UPLOAD_SUCCESS");
                wsResponse.put("fileName", fileName);
                wsResponse.put("message", "✅ " + fileName + " uploaded successfully");
                wsResponse.put("timestamp", System.currentTimeMillis());
                ctx.send(gson.toJson(wsResponse));
                System.out.println("[ApiGateway] ✓ File uploaded successfully: " + fileName);
            } else {
                String errorMsg = response != null ? response : "Unknown error";
                ctx.send(createErrorResponse("Failed to upload file: " + errorMsg));
            }
            
        } catch (Exception e) {
            System.err.println("[ApiGateway] Error uploading file: " + e.getMessage());
            e.printStackTrace();
            ctx.send(createErrorResponse("Failed to upload file: " + e.getMessage()));
        }
    }
    
    /**
     * Handle listFiles command - proxy to Secure File Service
     */
    private void handleListFilesCommand(WsContext ctx) {
        try {
            System.out.println("[ApiGateway] Listing files from Secure File Service");
            
            // Connect to Secure File Service via SSL
            // Use custom SSL context that accepts self-signed certificates
            SSLSocketFactory factory = SSLClientUtils.getSSLSocketFactory();
            SSLSocket socket = (SSLSocket) factory.createSocket("localhost", 9090);
            
            // Enable TLS protocols
            socket.setEnabledProtocols(new String[]{"TLSv1.2", "TLSv1.3"});
            
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            // Skip WELCOME message
            String welcome = in.readLine();
            System.out.println("[ApiGateway] Secure File Service: " + welcome);
            
            // Send LIST command
            out.println("LIST");
            
            // Read response
            String response = in.readLine();
            System.out.println("[ApiGateway] List response: " + response);
            
            if (response != null && response.startsWith("SUCCESS::")) {
                // Parse the file list
                String fileListContent = response.substring(9); // Remove "SUCCESS::" prefix
                
                String[] files;
                if (fileListContent.isEmpty() || fileListContent.equals("No files stored")) {
                    files = new String[0];
                } else {
                    // Split by newline and extract just the filename (before the size info)
                    String[] lines = fileListContent.split("\n");
                    files = new String[lines.length];
                    for (int i = 0; i < lines.length; i++) {
                        // Extract filename from "filename (size bytes)" format
                        String line = lines[i].trim();
                        if (line.contains("(")) {
                            files[i] = line.substring(0, line.lastIndexOf("(")).trim();
                        } else {
                            files[i] = line;
                        }
                    }
                }
                
                socket.close();
                
                Map<String, Object> wsResponse = new LinkedHashMap<>();
                wsResponse.put("type", "FILE_LIST");
                wsResponse.put("files", files);
                wsResponse.put("count", files.length);
                wsResponse.put("timestamp", System.currentTimeMillis());
                ctx.send(gson.toJson(wsResponse));
                System.out.println("[ApiGateway] ✓ Listed " + files.length + " files");
            } else {
                socket.close();
                ctx.send(createErrorResponse("Failed to list files: " + response));
            }
            
        } catch (Exception e) {
            System.err.println("[ApiGateway] Error listing files: " + e.getMessage());
            e.printStackTrace();
            ctx.send(createErrorResponse("Failed to list files: " + e.getMessage()));
        }
    }
    
    /**
     * Handle downloadFile command - proxy to Secure File Service
     */
    private void handleDownloadFileCommand(WsContext ctx, JsonObject json) {
        try {
            String fileName = json.get("fileName").getAsString();
            
            System.out.println("[ApiGateway] Downloading file: " + fileName);
            
            // Connect to Secure File Service via SSL
            // Use custom SSL context that accepts self-signed certificates
            SSLSocketFactory factory = SSLClientUtils.getSSLSocketFactory();
            SSLSocket socket = (SSLSocket) factory.createSocket("localhost", 9090);
            
            // Enable TLS protocols
            socket.setEnabledProtocols(new String[]{"TLSv1.2", "TLSv1.3"});
            
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            // Skip WELCOME message
            String welcome = in.readLine();
            System.out.println("[ApiGateway] Secure File Service: " + welcome);
            
            // Send RETRIEVE command
            out.println("RETRIEVE::" + fileName);
            
            // Read response
            String response = in.readLine();
            System.out.println("[ApiGateway] Download response: " + response);
            
            if (response != null && response.startsWith("SUCCESS::")) {
                // Read file data
                StringBuilder fileData = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    fileData.append(line).append("\n");
                }
                
                socket.close();
                
                Map<String, Object> wsResponse = new LinkedHashMap<>();
                wsResponse.put("type", "FILE_DOWNLOAD_SUCCESS");
                wsResponse.put("fileName", fileName);
                wsResponse.put("fileData", fileData.toString().trim());
                wsResponse.put("timestamp", System.currentTimeMillis());
                ctx.send(gson.toJson(wsResponse));
                System.out.println("[ApiGateway] ✓ File downloaded successfully: " + fileName);
            } else {
                socket.close();
                ctx.send(createErrorResponse("Failed to download file: " + response));
            }
            
        } catch (Exception e) {
            System.err.println("[ApiGateway] Error downloading file: " + e.getMessage());
            e.printStackTrace();
            ctx.send(createErrorResponse("Failed to download file: " + e.getMessage()));
        }
    }
    
    /**
     * Broadcast service status to all connected dashboards
     */
    private void broadcastStatusUpdate() {
        Map<String, Object> statusUpdate = new LinkedHashMap<>();
        statusUpdate.put("type", "SERVICE_STATUS_UPDATE");
        statusUpdate.put("service", "ApiGateway");
        statusUpdate.put("status", "online");
        statusUpdate.put("connectedDashboards", sessions.size());
        statusUpdate.put("timestamp", System.currentTimeMillis());
        
        String message = gson.toJson(statusUpdate);
        sessions.forEach(session -> {
            try {
                session.send(message);
            } catch (Exception e) {
                System.err.println("[WebSocket] Error sending broadcast: " + e.getMessage());
            }
        });
    }
    
    /**
     * Create error response
     */
    private String createErrorResponse(String error) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("type", "ERROR");
        response.put("error", error);
        response.put("timestamp", System.currentTimeMillis());
        return gson.toJson(response);
    }
    
    /**
     * Create pong response
     */
    private String createPongResponse() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("type", "PONG");
        response.put("timestamp", System.currentTimeMillis());
        return gson.toJson(response);
    }
    
    /**
     * Handle listFiles command - list all files from Secure File Service
     */
    private void handleListFilesCommand(WsContext ctx) {
        try {
            System.out.println("[ApiGateway] Listing files from Secure File Service...");
            
            String response = SecureFileClient.listFiles();
            
            if (response.startsWith("SUCCESS::")) {
                // Parse file list - response format is "SUCCESS::\nfile1\nfile2"
                String fileListStr = response.substring(9); // Remove "SUCCESS::"
                
                // Split by newline and filter out empty strings
                String[] allFiles = fileListStr.split("\n");
                java.util.List<String> filesList = new java.util.ArrayList<>();
                for (String file : allFiles) {
                    if (file != null && !file.trim().isEmpty()) {
                        filesList.add(file.trim());
                    }
                }
                
                Map<String, Object> jsonResponse = new LinkedHashMap<>();
                jsonResponse.put("type", "FILE_LIST");
                jsonResponse.put("files", filesList);
                jsonResponse.put("count", filesList.size());
                jsonResponse.put("timestamp", System.currentTimeMillis());
                
                ctx.send(gson.toJson(jsonResponse));
                System.out.println("[ApiGateway] ✓ Sent file list (" + filesList.size() + " files)");
            } else if (response.startsWith("ERROR::")) {
                String error = response.substring(7);
                ctx.send(createErrorResponse("Failed to list files: " + error));
            } else {
                ctx.send(createErrorResponse("Unexpected response from file service"));
            }
            
        } catch (Exception e) {
            System.err.println("[ApiGateway] Error listing files: " + e.getMessage());
            e.printStackTrace();
            ctx.send(createErrorResponse("Failed to list files: " + e.getMessage()));
        }
    }
    
    /**
     * Handle uploadFile command - upload file to Secure File Service
     */
    private void handleUploadFileCommand(WsContext ctx, JsonObject json) {
        try {
            // Frontend sends "fileName" and "fileData"
            System.out.println("[ApiGateway] Upload request received: " + json.toString());
            
            String filename = json.get("fileName").getAsString();
            String content = json.get("fileData").getAsString();
            
            System.out.println("[ApiGateway] Uploading file: " + filename + " (" + content.length() + " bytes)");
            
            String response = SecureFileClient.storeFile(filename, content);
            
            System.out.println("[ApiGateway] Secure File Service response: " + response);
            
            if (response.startsWith("SUCCESS::")) {
                String message = response.substring(9);
                
                Map<String, Object> jsonResponse = new LinkedHashMap<>();
                jsonResponse.put("type", "FILE_UPLOAD_SUCCESS");
                jsonResponse.put("fileName", filename);
                jsonResponse.put("message", message);
                jsonResponse.put("timestamp", System.currentTimeMillis());
                
                String responseJson = gson.toJson(jsonResponse);
                System.out.println("[ApiGateway] Sending response to frontend: " + responseJson);
                ctx.send(responseJson);
                System.out.println("[ApiGateway] ✓ File uploaded successfully");
            } else if (response.startsWith("ERROR::")) {
                String error = response.substring(7);
                System.err.println("[ApiGateway] Upload error: " + error);
                ctx.send(createErrorResponse("Failed to upload file: " + error));
            } else {
                System.err.println("[ApiGateway] Unexpected response: " + response);
                ctx.send(createErrorResponse("Unexpected response from file service"));
            }
            
        } catch (Exception e) {
            System.err.println("[ApiGateway] Error uploading file: " + e.getMessage());
            e.printStackTrace();
            ctx.send(createErrorResponse("Failed to upload file: " + e.getMessage()));
        }
    }
    
    /**
     * Handle downloadFile command - download file from Secure File Service
     */
    private void handleDownloadFileCommand(WsContext ctx, JsonObject json) {
        try {
            // Frontend sends "fileName"
            String filename = json.get("fileName").getAsString();
            
            System.out.println("[ApiGateway] Downloading file: " + filename);
            
            SecureFileClient.FileData fileData = SecureFileClient.retrieveFile(filename);
            
            if (fileData.success) {
                Map<String, Object> jsonResponse = new LinkedHashMap<>();
                jsonResponse.put("type", "FILE_DOWNLOAD_SUCCESS");
                jsonResponse.put("fileName", filename);
                jsonResponse.put("fileData", fileData.content);
                jsonResponse.put("timestamp", System.currentTimeMillis());
                
                ctx.send(gson.toJson(jsonResponse));
                System.out.println("[ApiGateway] ✓ File downloaded successfully");
            } else {
                ctx.send(createErrorResponse("Failed to download file: " + fileData.error));
            }
            
        } catch (Exception e) {
            System.err.println("[ApiGateway] Error downloading file: " + e.getMessage());
            ctx.send(createErrorResponse("Failed to download file: " + e.getMessage()));
        }
    }
    
    /**
     * Handle deleteFile command - delete file from Secure File Service
     */
    private void handleDeleteFileCommand(WsContext ctx, JsonObject json) {
        try {
            // Frontend sends "fileName"
            String filename = json.get("fileName").getAsString();
            
            System.out.println("[ApiGateway] Deleting file: " + filename);
            
            String response = SecureFileClient.deleteFile(filename);
            
            if (response.startsWith("SUCCESS::")) {
                String message = response.substring(9);
                
                Map<String, Object> jsonResponse = new LinkedHashMap<>();
                jsonResponse.put("type", "FILE_DELETE_SUCCESS");
                jsonResponse.put("fileName", filename);
                jsonResponse.put("message", message);
                jsonResponse.put("timestamp", System.currentTimeMillis());
                
                ctx.send(gson.toJson(jsonResponse));
                System.out.println("[ApiGateway] ✓ File deleted successfully");
            } else if (response.startsWith("ERROR::")) {
                String error = response.substring(7);
                ctx.send(createErrorResponse("Failed to delete file: " + error));
            } else {
                ctx.send(createErrorResponse("Unexpected response from file service"));
            }
            
        } catch (Exception e) {
            System.err.println("[ApiGateway] Error deleting file: " + e.getMessage());
            ctx.send(createErrorResponse("Failed to delete file: " + e.getMessage()));
        }
    }
    
    /**
     * Start WebSocket server on port 9001
     */
    public void start() {
        try {
            System.out.println("[WebSocketServer] Starting on port 9001...");
            app.start(9001);
            
            // Configure idle timeout after server starts (in a separate thread)
            // This prevents "Connection Idle Timeout" errors on WebSocket connections
            new Thread(() -> {
                try {
                    // Wait a moment for the server to fully initialize
                    Thread.sleep(1000);
                    
                    // Access the underlying Jetty Server from Javalin's JettyServer wrapper
                    var jettyServerWrapper = app.jettyServer();
                    if (jettyServerWrapper != null) {
                        Server jettyServer = jettyServerWrapper.server();
                        
                        if (jettyServer != null) {
                            // Set idle timeout on all connectors
                            for (Connector connector : jettyServer.getConnectors()) {
                                if (connector instanceof AbstractConnector) {
                                    AbstractConnector abstractConnector = (AbstractConnector) connector;
                                    long currentTimeout = abstractConnector.getIdleTimeout();
                                    abstractConnector.setIdleTimeout(300000); // 5 minutes (300 seconds)
                                    System.out.println("[WebSocketServer] ✓ Jetty idle timeout configured");
                                    System.out.println("[WebSocketServer]   Before: " + currentTimeout + "ms, After: 300000ms");
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("[WebSocketServer] Note: Could not configure idle timeout: " + e.getMessage());
                }
            }).start();
            
            System.out.println("[WebSocketServer] ✓ WebSocket server started successfully");
            System.out.println("[WebSocketServer] Dashboard WebSocket: ws://localhost:9001/api");
            System.out.println("[WebSocketServer] Health endpoint: http://localhost:9001/health");
        } catch (Exception e) {
            System.err.println("[WebSocketServer] Failed to start: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Stop WebSocket server
     */
    public void stop() {
        System.out.println("[WebSocketServer] Stopping WebSocket server...");
        app.stop();
        System.out.println("[WebSocketServer] ✓ WebSocket server stopped");
    }
    
    /**
     * Check if server is running
     */
    public boolean isRunning() {
        return app != null;
    }
    
    /**
     * Get number of connected dashboards
     */
    public int getConnectedDashboardCount() {
        return sessions.size();
    }
}
