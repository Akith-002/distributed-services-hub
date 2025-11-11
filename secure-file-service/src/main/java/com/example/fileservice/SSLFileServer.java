package com.example.fileservice;

import com.example.fileservice.security.SSLUtils;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.IOException;

/**
 * SSL File Server using SSLServerSocket (JSSE)
 * Member 3 - Secure Sockets with SSL/TLS
 */
public class SSLFileServer {
    
    private static final int PORT = 9090;
    private SSLServerSocket serverSocket;
    private volatile boolean running = false;
    
    /**
     * Start the SSL server
     */
    public void start() throws IOException {
        System.out.println("\n[SSLFileServer] Initializing SSL File Server...");
        
        // Get SSL server socket factory
        SSLServerSocketFactory factory = SSLUtils.getServerSocketFactory();
        
        // Create SSLServerSocket
        serverSocket = (SSLServerSocket) factory.createServerSocket(PORT);
        
        // Configure SSL parameters
        String[] protocols = {"TLSv1.2", "TLSv1.3"};
        serverSocket.setEnabledProtocols(protocols);
        
        System.out.println("[SSLFileServer] ✓ SSL Server Socket created on port " + PORT);
        System.out.println("[SSLFileServer] Enabled protocols: " + String.join(", ", protocols));
        System.out.println("[SSLFileServer] Waiting for SSL connections...\n");
        
        running = true;
        
        // Accept connections
        while (running) {
            try {
                SSLSocket clientSocket = (SSLSocket) serverSocket.accept();
                
                // Print SSL session info
                SSLUtils.printSessionInfo(clientSocket);
                
                // Handle client in new thread
                Thread handler = new Thread(new FileServiceHandler(clientSocket));
                handler.start();
                
            } catch (IOException e) {
                if (running) {
                    System.err.println("[SSLFileServer] Error accepting connection: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Stop the SSL server
     */
    public void stop() {
        running = false;
        
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                System.out.println("[SSLFileServer] ✓ SSL Server stopped");
            }
        } catch (IOException e) {
            System.err.println("[SSLFileServer] Error stopping server: " + e.getMessage());
        }
    }
    
    /**
     * Check if server is running
     */
    public boolean isRunning() {
        return running;
    }
}
