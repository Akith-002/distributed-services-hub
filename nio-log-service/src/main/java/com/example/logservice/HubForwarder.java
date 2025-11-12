package com.example.logservice;

import org.json.JSONObject;

import java.io.*;
import java.net.Socket;

/**
 * Forwards log messages to Hub for Dashboard display
 */
public class HubForwarder {
    private static final String HUB_HOST = "localhost";
    private static final int HUB_PORT = 7070;
    
    private Socket hubSocket;
    private PrintWriter out;
    private boolean connected = false;

    /**
     * Connect to Hub for log forwarding
     */
    public void connect() {
        try {
            hubSocket = new Socket(HUB_HOST, HUB_PORT);
            out = new PrintWriter(hubSocket.getOutputStream(), true);
            connected = true;
            System.out.println("[HUB_FORWARDER] Connected to Hub at " + HUB_HOST + ":" + HUB_PORT);
        } catch (IOException e) {
            System.err.println("[HUB_FORWARDER] Failed to connect to Hub: " + e.getMessage());
            connected = false;
        }
    }

    /**
     * Forward log message to Hub for Dashboard display
     * Format: {"result_from": "NIO_SERVICE", "data": "LOG: ..."}
     */
    public void forwardLog(String logMessage) {
        if (!connected) {
            // Try to reconnect
            connect();
        }

        if (connected && out != null) {
            try {
                JSONObject result = new JSONObject();
                result.put("result_from", "NIO_SERVICE");
                result.put("data", "LOG: " + logMessage);
                
                out.println(result.toString());
                
            } catch (Exception e) {
                System.err.println("[HUB_FORWARDER] Error forwarding log: " + e.getMessage());
                connected = false;
            }
        }
    }

    /**
     * Close connection to Hub
     */
    public void close() {
        try {
            if (out != null) {
                out.close();
            }
            if (hubSocket != null && !hubSocket.isClosed()) {
                hubSocket.close();
            }
            System.out.println("[HUB_FORWARDER] Disconnected from Hub");
        } catch (IOException e) {
            System.err.println("[HUB_FORWARDER] Error closing connection: " + e.getMessage());
        }
    }
}
