package com.example.fileservice;

import com.example.fileservice.security.SSLUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Socket;
import java.net.SocketException;

/**
 * Security Test Runner for JSSE Service
 * Demonstrates the difference between insecure and secure socket connections
 * Member 3 - JSSE (Java Secure Socket Extension) Implementation
 * 
 * Test 1: Insecure Socket Connection (Should FAIL)
 * - Attempts to connect using regular Socket instead of SSLSocket
 * - SSLServerSocket should reject this connection
 * 
 * Test 2: Secure SSLSocket Connection (Should SUCCEED)
 * - Connects using proper SSLSocket with SSL context
 * - SSLServerSocket should accept and complete handshake
 * 
 * Results are formatted as JSON and sent back to Hub for Dashboard display
 */
public class SecurityTestRunner implements CommandListener {
    
    private final HubClient hubClient;
    private static final String TEST_HOST = "localhost";
    private static final int TEST_PORT = 9090;
    
    /**
     * Create a security test runner
     * 
     * @param hubClient Reference to HubClient for sending results
     */
    public SecurityTestRunner(HubClient hubClient) {
        this.hubClient = hubClient;
    }
    
    /**
     * Handle commands from Hub
     * Listens for "run-test" command and executes security tests
     * 
     * @param command The command string or JSON
     */
    @Override
    public void onCommand(String command) {
        if (command == null) {
            return;
        }
        
        String commandToExecute = null;
        
        // Try to parse as JSON first (new format from Hub)
        if (command.trim().startsWith("{")) {
            try {
                JsonObject jsonObj = JsonParser.parseString(command).getAsJsonObject();
                commandToExecute = jsonObj.has("command") 
                    ? jsonObj.get("command").getAsString() 
                    : null;
            } catch (Exception e) {
                System.out.println("[SecurityTest] Failed to parse JSON command: " + e.getMessage());
                return;
            }
        } else {
            // Plain text command (legacy format)
            commandToExecute = command;
        }
        
        // Execute if it's a run-test command
        if (commandToExecute != null && commandToExecute.equalsIgnoreCase("run-test")) {
            System.out.println("\n[SecurityTest] Executing security tests...\n");
            runSecurityTests();
        }
    }
    
    /**
     * Run both security tests and send results to Hub
     */
    private void runSecurityTests() {
        System.out.println("[SecurityTest] ============================================");
        System.out.println("[SecurityTest] JSSE SECURITY TEST SUITE");
        System.out.println("[SecurityTest] ============================================\n");
        
        // Test 1: Insecure Socket (Should FAIL)
        String test1Result = runInsecureSocketTest();
        
        // Brief pause between tests
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Test 2: Secure SSLSocket (Should SUCCEED)
        String test2Result = runSecureSocketTest();
        
        System.out.println("\n[SecurityTest] ============================================");
        System.out.println("[SecurityTest] TEST SUITE COMPLETE");
        System.out.println("[SecurityTest] ============================================\n");
        
        // Send results back to Hub
        sendTestResultsToHub(test1Result, test2Result);
    }
    
    /**
     * Test 1: Attempt connection with regular Socket (insecure)
     * This should FAIL because SSLServerSocket only accepts SSL connections
     * 
     * @return Result message for this test
     */
    private String runInsecureSocketTest() {
        System.out.println("[SecurityTest] Test 1: Insecure Socket Connection");
        System.out.println("[SecurityTest] ├─ Attempting to connect with regular Socket (non-SSL)...");
        System.out.println("[SecurityTest] ├─ Expected: Connection FAILED (SSLServerSocket rejects non-SSL)");
        System.out.println("[SecurityTest] │");
        
        try {
            // Attempt to create regular Socket to SSL server
            Socket insecureSocket = new Socket(TEST_HOST, TEST_PORT);
            
            // If we reach here, something is wrong - should not accept non-SSL
            System.out.println("[SecurityTest] ├─ ✗ UNEXPECTED: Socket accepted non-SSL connection!");
            insecureSocket.close();
            
            String result = "Test 1 (Insecure Socket): UNEXPECTED_SUCCESS - Server accepted non-SSL connection";
            System.out.println("[SecurityTest] └─ Result: " + result + "\n");
            return result;
            
        } catch (SocketException e) {
            // Expected: SocketException, connection refused or reset
            String errorMsg = e.getMessage();
            System.out.println("[SecurityTest] ├─ ✓ Connection rejected (expected)");
            System.out.println("[SecurityTest] ├─ Exception: " + errorMsg);
            System.out.println("[SecurityTest] ├─ Reason: SSLServerSocket rejects non-SSL connections");
            
            String result = "Test 1 (Insecure Socket): FAILED (Expected) - Connection rejected by server: " + errorMsg;
            System.out.println("[SecurityTest] └─ Result: " + result + "\n");
            return result;
            
        } catch (Exception e) {
            // Any other exception
            String errorMsg = e.getClass().getSimpleName() + ": " + e.getMessage();
            System.out.println("[SecurityTest] ├─ ✓ Connection failed (expected)");
            System.out.println("[SecurityTest] ├─ Exception: " + errorMsg);
            
            String result = "Test 1 (Insecure Socket): FAILED (Expected) - " + errorMsg;
            System.out.println("[SecurityTest] └─ Result: " + result + "\n");
            return result;
        }
    }
    
    /**
     * Test 2: Connect with proper SSLSocket (secure)
     * This should SUCCEED because we're using proper SSL context
     * 
     * @return Result message for this test
     */
    private String runSecureSocketTest() {
        System.out.println("[SecurityTest] Test 2: Secure SSLSocket Connection");
        System.out.println("[SecurityTest] ├─ Creating SSL context...");
        
        try {
            // Get SSL socket factory with proper context
            SSLSocketFactory factory = SSLUtils.getClientSocketFactory();
            
            System.out.println("[SecurityTest] ├─ ✓ SSL context created");
            System.out.println("[SecurityTest] ├─ Attempting to connect with SSLSocket...");
            
            // Create SSL socket to server
            SSLSocket sslSocket = (SSLSocket) factory.createSocket(TEST_HOST, TEST_PORT);
            
            System.out.println("[SecurityTest] ├─ ✓ SSL socket created");
            System.out.println("[SecurityTest] ├─ Initiating SSL handshake...");
            
            // Start handshake
            sslSocket.startHandshake();
            
            System.out.println("[SecurityTest] ├─ ✓ SSL handshake completed successfully");
            System.out.println("[SecurityTest] ├─ Connected securely to server");
            
            // Get SSL session information
            String protocol = sslSocket.getSession().getProtocol();
            String cipherSuite = sslSocket.getSession().getCipherSuite();
            System.out.println("[SecurityTest] ├─ Protocol: " + protocol);
            System.out.println("[SecurityTest] ├─ Cipher Suite: " + cipherSuite);
            
            // Close socket
            sslSocket.close();
            
            String result = "Test 2 (Secure SSLSocket): SUCCESS - Connected securely using " + protocol + " with " + cipherSuite;
            System.out.println("[SecurityTest] └─ Result: " + result + "\n");
            return result;
            
        } catch (Exception e) {
            // Connection failed (unexpected)
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String errorMsg = e.getClass().getSimpleName() + ": " + e.getMessage();
            
            System.out.println("[SecurityTest] ├─ ✗ Connection failed (unexpected)");
            System.out.println("[SecurityTest] ├─ Exception: " + errorMsg);
            
            String result = "Test 2 (Secure SSLSocket): FAILED (Unexpected) - " + errorMsg;
            System.out.println("[SecurityTest] └─ Result: " + result + "\n");
            return result;
        }
    }
    
    /**
     * Format and send test results back to Hub for Dashboard display
     * 
     * @param test1Result Result from insecure socket test
     * @param test2Result Result from secure socket test
     */
    private void sendTestResultsToHub(String test1Result, String test2Result) {
        System.out.println("[SecurityTest] Sending results back to Hub...\n");
        
        try {
            // Format results as JSON for Dashboard
            String combinedResults = test1Result + " || " + test2Result;
            
            // Create result message
            String resultJson = "{\"result_from\": \"JSSE_SERVICE\", \"data\": \"" 
                    + combinedResults.replace("\"", "\\\"") + "\"}";
            
            // Send to Hub
            hubClient.sendResult(resultJson);
            
            System.out.println("[SecurityTest] ✓ Results sent to Hub");
            System.out.println("[SecurityTest] ✓ Results will be displayed on Dashboard\n");
            
        } catch (Exception e) {
            System.err.println("[SecurityTest] ✗ Error sending results to Hub: " + e.getMessage());
        }
    }
}
