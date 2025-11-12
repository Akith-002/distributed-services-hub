package com.example.apigateway;

import javax.net.ssl.*;
import java.io.*;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

/**
 * SSL Client for API Gateway to communicate with Secure File Service
 * Bridges WebSocket requests from frontend to SSL file service
 */
public class SecureFileClient {
    
    private static final String FILE_SERVICE_HOST = "localhost";
    private static final int FILE_SERVICE_PORT = 9090;
    
    /**
     * Create SSL socket factory that trusts all certificates
     */
    private static SSLSocketFactory getSSLSocketFactory() throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
        };
        
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCerts, new SecureRandom());
        return sslContext.getSocketFactory();
    }
    
    /**
     * List all files from Secure File Service
     */
    public static String listFiles() {
        try {
            SSLSocketFactory factory = getSSLSocketFactory();
            SSLSocket socket = (SSLSocket) factory.createSocket(FILE_SERVICE_HOST, FILE_SERVICE_PORT);
            
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            
            // Read welcome message
            in.readLine();
            
            // Send LIST command
            out.println("LIST");
            
            // Read the full response - first line is SUCCESS:: or ERROR::, followed by file names
            StringBuilder response = new StringBuilder();
            String line;
            
            // Read first line (status)
            line = in.readLine();
            if (line != null) {
                response.append(line);
                
                // If success, read all file names until END marker or connection closes
                if (line.startsWith("SUCCESS::")) {
                    // Set a timeout to avoid hanging
                    socket.setSoTimeout(1000); // 1 second timeout
                    
                    try {
                        while ((line = in.readLine()) != null) {
                            if (line.equals("END")) {
                                break;
                            }
                            response.append("\n").append(line);
                        }
                    } catch (java.net.SocketTimeoutException e) {
                        // Timeout is expected when all files are read
                        System.out.println("[SecureFileClient] List complete (timeout)");
                    }
                }
            }
            
            socket.close();
            return response.toString();
            
        } catch (Exception e) {
            System.err.println("[SecureFileClient] Error listing files: " + e.getMessage());
            return "ERROR::Failed to list files: " + e.getMessage();
        }
    }
    
    /**
     * Store a file to Secure File Service
     */
    public static String storeFile(String filename, String content) {
        SSLSocket socket = null;
        try {
            System.out.println("[SecureFileClient] Connecting to Secure File Service...");
            SSLSocketFactory factory = getSSLSocketFactory();
            socket = (SSLSocket) factory.createSocket(FILE_SERVICE_HOST, FILE_SERVICE_PORT);
            
            // Set timeout to prevent hanging
            socket.setSoTimeout(10000); // 10 second timeout
            
            System.out.println("[SecureFileClient] Connected, setting up streams...");
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            
            // Read welcome message
            System.out.println("[SecureFileClient] Reading welcome message...");
            String welcome = in.readLine();
            System.out.println("[SecureFileClient] Welcome: " + welcome);
            
            // Send STORE command
            int size = content.length();
            String storeCommand = "STORE::" + filename + "::" + size;
            System.out.println("[SecureFileClient] Sending: " + storeCommand);
            out.println(storeCommand);
            
            // Send content (without println to avoid extra newline)
            System.out.println("[SecureFileClient] Sending file content (" + size + " bytes)...");
            out.print(content);
            out.flush();
            
            // Read response
            System.out.println("[SecureFileClient] Waiting for response...");
            String response = in.readLine();
            System.out.println("[SecureFileClient] Response: " + response);
            
            socket.close();
            return response;
            
        } catch (Exception e) {
            System.err.println("[SecureFileClient] Error storing file: " + e.getMessage());
            e.printStackTrace();
            if (socket != null) {
                try { socket.close(); } catch (Exception ex) {}
            }
            return "ERROR::Failed to store file: " + e.getMessage();
        }
    }
    
    /**
     * Retrieve a file from Secure File Service
     */
    public static FileData retrieveFile(String filename) {
        try {
            SSLSocketFactory factory = getSSLSocketFactory();
            SSLSocket socket = (SSLSocket) factory.createSocket(FILE_SERVICE_HOST, FILE_SERVICE_PORT);
            
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            
            // Read welcome message
            in.readLine();
            
            // Send RETRIEVE command
            out.println("RETRIEVE::" + filename);
            
            // Read response header
            String header = in.readLine();
            
            if (header.startsWith("SUCCESS::")) {
                // Parse size from header: "SUCCESS::filename::size"
                String[] parts = header.split("::");
                int size = Integer.parseInt(parts[2]);
                
                // Read file content
                char[] buffer = new char[size];
                in.read(buffer, 0, size);
                String content = new String(buffer);
                
                socket.close();
                return new FileData(filename, content, true, null);
            } else {
                socket.close();
                return new FileData(filename, null, false, header);
            }
            
        } catch (Exception e) {
            System.err.println("[SecureFileClient] Error retrieving file: " + e.getMessage());
            return new FileData(filename, null, false, "ERROR::Failed to retrieve file: " + e.getMessage());
        }
    }
    
    /**
     * Delete a file from Secure File Service
     */
    public static String deleteFile(String filename) {
        try {
            SSLSocketFactory factory = getSSLSocketFactory();
            SSLSocket socket = (SSLSocket) factory.createSocket(FILE_SERVICE_HOST, FILE_SERVICE_PORT);
            
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            
            // Read welcome message
            in.readLine();
            
            // Send DELETE command
            out.println("DELETE::" + filename);
            
            // Read response
            String response = in.readLine();
            
            socket.close();
            return response;
            
        } catch (Exception e) {
            System.err.println("[SecureFileClient] Error deleting file: " + e.getMessage());
            return "ERROR::Failed to delete file: " + e.getMessage();
        }
    }
    
    /**
     * Data class for file retrieval
     */
    public static class FileData {
        public final String filename;
        public final String content;
        public final boolean success;
        public final String error;
        
        public FileData(String filename, String content, boolean success, String error) {
            this.filename = filename;
            this.content = content;
            this.success = success;
            this.error = error;
        }
    }
}
