package com.example.fileservice;

import com.example.fileservice.security.SSLUtils;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;

/**
 * SSL Client for testing the Secure File Service
 * Demonstrates SSL connection and file operations
 * Member 3 - JSSE Implementation
 */
public class SSLFileClient {
    
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 9090;
    
    public static void main(String[] args) {
        printBanner();
        
        try {
            // Create SSL socket
            System.out.println("[Client] Creating SSL connection to " + SERVER_HOST + ":" + SERVER_PORT);
            SSLSocketFactory factory = SSLUtils.getClientSocketFactory();
            SSLSocket socket = (SSLSocket) factory.createSocket(SERVER_HOST, SERVER_PORT);
            
            System.out.println("[Client] ✓ SSL Connection established");
            SSLUtils.printSessionInfo(socket);
            
            // Setup I/O streams
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
            
            // Read welcome message
            String welcome = in.readLine();
            System.out.println("\n[Server] " + welcome + "\n");
            
            printHelp();
            
            // Interactive command loop
            String command;
            while (true) {
                System.out.print("\nSSL File Client> ");
                command = console.readLine();
                
                if (command == null || command.trim().isEmpty()) {
                    continue;
                }
                
                command = command.trim();
                
                if (command.equalsIgnoreCase("help")) {
                    printHelp();
                    continue;
                }
                
                if (command.equalsIgnoreCase("exit")) {
                    out.println("EXIT");
                    String response = in.readLine();
                    System.out.println("[Server] " + response);
                    break;
                }
                
                // Handle STORE command specially (multi-line)
                if (command.toUpperCase().startsWith("STORE")) {
                    handleStore(command, in, out, console);
                } else {
                    // Send command
                    out.println(command);
                    
                    // Receive response
                    String response = in.readLine();
                    
                    if (response.startsWith("SUCCESS::")) {
                        String result = response.substring(9);
                        System.out.println("\n✓ Success:");
                        System.out.println(result);
                    } else if (response.startsWith("ERROR::")) {
                        String error = response.substring(7);
                        System.out.println("\n✗ Error:");
                        System.out.println(error);
                    } else {
                        System.out.println("\n[Server] " + response);
                    }
                }
            }
            
            socket.close();
            System.out.println("\n[Client] Connection closed");
            
        } catch (IOException e) {
            System.err.println("\n[Client ERROR] " + e.getMessage());
            System.err.println("\nMake sure the Secure File Service is running!");
            e.printStackTrace();
        }
    }
    
    /**
     * Handle STORE command with file content input
     */
    private static void handleStore(String command, BufferedReader in, PrintWriter out, BufferedReader console) throws IOException {
        String[] parts = command.split("\\s+");
        
        if (parts.length < 2) {
            System.out.println("\n✗ Usage: STORE <filename>");
            return;
        }
        
        String filename = parts[1];
        
        System.out.println("Enter file content (type 'END' on new line to finish):");
        StringBuilder content = new StringBuilder();
        String line;
        
        while ((line = console.readLine()) != null) {
            if (line.equals("END")) {
                break;
            }
            if (content.length() > 0) {
                content.append("\n");
            }
            content.append(line);
        }
        
        String fileContent = content.toString();
        int size = fileContent.length();
        
        // Send STORE command
        out.println("STORE::" + filename + "::" + size);
        
        // Send content
        out.print(fileContent);
        out.flush();
        
        // Get response
        String response = in.readLine();
        
        if (response.startsWith("SUCCESS::")) {
            String result = response.substring(9);
            System.out.println("\n✓ Success:");
            System.out.println(result);
        } else if (response.startsWith("ERROR::")) {
            String error = response.substring(7);
            System.out.println("\n✗ Error:");
            System.out.println(error);
        } else {
            System.out.println("\n[Server] " + response);
        }
    }
    
    /**
     * Print banner
     */
    private static void printBanner() {
        System.out.println("\n============================================");
        System.out.println("  SSL FILE CLIENT - Test Tool");
        System.out.println("  Member 3 - Secure File Service");
        System.out.println("============================================\n");
    }
    
    /**
     * Print help
     */
    private static void printHelp() {
        System.out.println("\n┌─────────────────────────────────────────┐");
        System.out.println("│ Available Commands:                     │");
        System.out.println("├─────────────────────────────────────────┤");
        System.out.println("│ STORE <filename>  - Store a file        │");
        System.out.println("│ RETRIEVE <filename> - Get a file        │");
        System.out.println("│ LIST             - List all files       │");
        System.out.println("│ DELETE <filename> - Delete a file       │");
        System.out.println("│ HELP             - Show this help       │");
        System.out.println("│ EXIT             - Disconnect           │");
        System.out.println("└─────────────────────────────────────────┘");
    }
}
