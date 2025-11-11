package com.example.fileservice;

import javax.net.ssl.SSLSocket;
import java.io.*;
import java.nio.file.*;
import java.util.stream.Collectors;

/**
 * Handles individual client connections to the Secure File Service
 * Processes file commands: STORE, RETRIEVE, LIST, DELETE
 * Member 3 - JSSE SSL/TLS Implementation
 */
public class FileServiceHandler implements Runnable {
    
    private final SSLSocket socket;
    private static final String FILES_DIR = "files/";
    
    public FileServiceHandler(SSLSocket socket) {
        this.socket = socket;
        
        // Ensure files directory exists
        try {
            Files.createDirectories(Paths.get(FILES_DIR));
        } catch (IOException e) {
            System.err.println("[FileHandler] Error creating files directory: " + e.getMessage());
        }
    }
    
    @Override
    public void run() {
        String clientAddress = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
        System.out.println("\n[FileHandler] New SSL connection from " + clientAddress);
        
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            
            out.println("WELCOME::SecureFileService::SSL");
            
            String command;
            while ((command = in.readLine()) != null) {
                System.out.println("[FileHandler] Received command: " + command);
                
                String response = processCommand(command, in);
                out.println(response);
                
                if (command.equals("EXIT")) {
                    break;
                }
            }
            
        } catch (IOException e) {
            System.err.println("[FileHandler] Error handling client: " + e.getMessage());
        } finally {
            try {
                socket.close();
                System.out.println("[FileHandler] Connection closed: " + clientAddress);
            } catch (IOException e) {
                System.err.println("[FileHandler] Error closing socket: " + e.getMessage());
            }
        }
    }
    
    /**
     * Process file commands
     */
    private String processCommand(String command, BufferedReader in) throws IOException {
        String[] parts = command.split("::", 2);
        String cmd = parts[0].toUpperCase();
        
        switch (cmd) {
            case "STORE":
                return handleStore(parts.length > 1 ? parts[1] : "", in);
                
            case "RETRIEVE":
                return handleRetrieve(parts.length > 1 ? parts[1] : "");
                
            case "LIST":
                return handleList();
                
            case "DELETE":
                return handleDelete(parts.length > 1 ? parts[1] : "");
                
            case "EXIT":
                return "BYE::Connection closed";
                
            default:
                return "ERROR::Unknown command. Available: STORE, RETRIEVE, LIST, DELETE, EXIT";
        }
    }
    
    /**
     * STORE <filename>::<size>
     * Then read <size> bytes of content
     */
    private String handleStore(String params, BufferedReader in) throws IOException {
        if (params.isEmpty()) {
            return "ERROR::Usage: STORE::<filename>::<size>";
        }
        
        String[] parts = params.split("::");
        if (parts.length < 2) {
            return "ERROR::Usage: STORE::<filename>::<size>";
        }
        
        String filename = sanitizeFilename(parts[0]);
        int size;
        
        try {
            size = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            return "ERROR::Invalid size parameter";
        }
        
        // Read file content
        char[] buffer = new char[size];
        int bytesRead = in.read(buffer, 0, size);
        
        if (bytesRead < 0) {
            return "ERROR::Failed to read file content";
        }
        
        String content = new String(buffer, 0, bytesRead);
        
        // Store file
        Path filePath = Paths.get(FILES_DIR, filename);
        try {
            Files.write(filePath, content.getBytes());
            System.out.println("[FileHandler] ✓ File stored: " + filename + " (" + bytesRead + " bytes)");
            return "SUCCESS::File stored: " + filename + " (" + bytesRead + " bytes)";
        } catch (IOException e) {
            System.err.println("[FileHandler] ✗ Error storing file: " + e.getMessage());
            return "ERROR::Failed to store file: " + e.getMessage();
        }
    }
    
    /**
     * RETRIEVE <filename>
     */
    private String handleRetrieve(String filename) throws IOException {
        if (filename.isEmpty()) {
            return "ERROR::Usage: RETRIEVE::<filename>";
        }
        
        filename = sanitizeFilename(filename);
        Path filePath = Paths.get(FILES_DIR, filename);
        
        if (!Files.exists(filePath)) {
            System.out.println("[FileHandler] ✗ File not found: " + filename);
            return "ERROR::File not found: " + filename;
        }
        
        try {
            String content = Files.readString(filePath);
            System.out.println("[FileHandler] ✓ File retrieved: " + filename + " (" + content.length() + " bytes)");
            return "SUCCESS::" + content;
        } catch (IOException e) {
            System.err.println("[FileHandler] ✗ Error reading file: " + e.getMessage());
            return "ERROR::Failed to read file: " + e.getMessage();
        }
    }
    
    /**
     * LIST - list all stored files
     */
    private String handleList() throws IOException {
        try (var stream = Files.list(Paths.get(FILES_DIR))) {
            String files = stream
                .filter(Files::isRegularFile)
                .map(path -> {
                    try {
                        long size = Files.size(path);
                        return path.getFileName().toString() + " (" + size + " bytes)";
                    } catch (IOException e) {
                        return path.getFileName().toString();
                    }
                })
                .collect(Collectors.joining("\n"));
            
            if (files.isEmpty()) {
                System.out.println("[FileHandler] No files stored");
                return "SUCCESS::No files stored";
            }
            
            System.out.println("[FileHandler] ✓ Listing files");
            return "SUCCESS::\n" + files;
        } catch (IOException e) {
            System.err.println("[FileHandler] ✗ Error listing files: " + e.getMessage());
            return "ERROR::Failed to list files: " + e.getMessage();
        }
    }
    
    /**
     * DELETE <filename>
     */
    private String handleDelete(String filename) throws IOException {
        if (filename.isEmpty()) {
            return "ERROR::Usage: DELETE::<filename>";
        }
        
        filename = sanitizeFilename(filename);
        Path filePath = Paths.get(FILES_DIR, filename);
        
        if (!Files.exists(filePath)) {
            System.out.println("[FileHandler] ✗ File not found: " + filename);
            return "ERROR::File not found: " + filename;
        }
        
        try {
            Files.delete(filePath);
            System.out.println("[FileHandler] ✓ File deleted: " + filename);
            return "SUCCESS::File deleted: " + filename;
        } catch (IOException e) {
            System.err.println("[FileHandler] ✗ Error deleting file: " + e.getMessage());
            return "ERROR::Failed to delete file: " + e.getMessage();
        }
    }
    
    /**
     * Sanitize filename to prevent directory traversal attacks
     */
    private String sanitizeFilename(String filename) {
        // Remove any path separators and parent directory references
        return filename.replaceAll("[/\\\\]", "").replaceAll("\\.\\.", "");
    }
}
