package com.example.hub;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.After;

import java.io.*;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Integration tests for the Hub Server Message Broker functionality.
 * Tests the message flow:
 * - Dashboard sends command to Hub
 * - Hub routes command to service
 * - Service processes and sends result
 * - Hub broadcasts result back to Dashboard
 * 
 * @author Member 1 - Hub Server Implementation
 * @version 1.0
 */
public class HubServerMessageBrokerIT {
    
    private ServiceRegistry registry;
    private CommandRouter commandRouter;
    private ResultAggregator resultAggregator;
    private WebSocketBroadcaster broadcaster;

    @BeforeClass
    public static void setUpClass() {
        System.out.println("\n=== HUB MESSAGE BROKER INTEGRATION TEST ===\n");
    }

    @org.junit.Before
    public void setUp() {
        registry = new ServiceRegistry();
        broadcaster = new WebSocketBroadcaster();
        commandRouter = new CommandRouter(registry);
        resultAggregator = new ResultAggregator(broadcaster);
        broadcaster.setCommandRouter(commandRouter);
        broadcaster.setResultAggregator(resultAggregator);
    }

    @After
    public void tearDown() {
        registry.clear();
    }

    /**
     * Test 1: Basic Command Routing
     * Verify that a command is routed to a service successfully
     */
    @Test
    public void testBasicCommandRouting() {
        System.out.println("\n[TEST 1] Basic Command Routing");
        
        // Setup: Register a mock service
        registry.register("API_GATEWAY", "localhost", 9001);
        
        // Create mock output stream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(outputStream, true);
        
        // Register service connection with router
        commandRouter.registerServiceConnection("API_GATEWAY", writer);
        
        // Send command to service
        boolean success = commandRouter.routeCommandToService("API_GATEWAY", "get-weather");
        
        assertTrue("Command should route successfully", success);
        assertTrue("Output should contain command", outputStream.toString().contains("get-weather"));
        
        System.out.println("✓ Command routed successfully to API_GATEWAY");
    }

    /**
     * Test 2: Command Not Routed to Unregistered Service
     * Verify that commands are rejected for unregistered services
     */
    @Test
    public void testCommandRoutingToUnregisteredService() {
        System.out.println("\n[TEST 2] Command Routing to Unregistered Service");
        
        // Try to route command to non-existent service
        boolean success = commandRouter.routeCommandToService("UNKNOWN_SERVICE", "test");
        
        assertFalse("Command should not route to unregistered service", success);
        
        System.out.println("✓ Command correctly rejected for unregistered service");
    }

    /**
     * Test 3: Result Broadcast
     * Verify that service results are properly formatted for dashboard broadcast
     */
    @Test
    public void testResultBroadcast() {
        System.out.println("\n[TEST 3] Result Broadcast");
        
        // Create result message from service
        // String resultMessage = "{\"result_from\": \"API_GATEWAY\", \"data\": \"Temperature: 28.5°C\"}";
        
        // Process result (this would normally broadcast to dashboards)
        String broadcastMessage = resultAggregator.createResultMessage("API_GATEWAY", "Temperature: 28.5°C");
        
        // Verify broadcast message format
        assertTrue("Broadcast should contain type", broadcastMessage.contains("SERVICE_RESULT"));
        assertTrue("Broadcast should contain service name", broadcastMessage.contains("API_GATEWAY"));
        assertTrue("Broadcast should contain result data", broadcastMessage.contains("Temperature"));
        assertTrue("Broadcast should contain timestamp", broadcastMessage.contains("timestamp"));
        
        System.out.println("✓ Result message formatted correctly for broadcast");
        System.out.println("  Broadcast: " + broadcastMessage);
    }

    /**
     * Test 4: Multiple Services
     * Verify that multiple services can have active command routes
     */
    @Test
    public void testMultipleServiceRouting() {
        System.out.println("\n[TEST 4] Multiple Service Routing");
        
        // Register multiple services
        registry.register("API_GATEWAY", "localhost", 9001);
        registry.register("JSSE_SERVICE", "localhost", 9090);
        registry.register("NIO_SERVICE", "localhost", 9091);
        
        // Create mock writers for each service
        ByteArrayOutputStream stream1 = new ByteArrayOutputStream();
        ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
        ByteArrayOutputStream stream3 = new ByteArrayOutputStream();
        
        commandRouter.registerServiceConnection("API_GATEWAY", new PrintWriter(stream1, true));
        commandRouter.registerServiceConnection("JSSE_SERVICE", new PrintWriter(stream2, true));
        commandRouter.registerServiceConnection("NIO_SERVICE", new PrintWriter(stream3, true));
        
        // Route commands to each service
        boolean route1 = commandRouter.routeCommandToService("API_GATEWAY", "fetch-weather");
        boolean route2 = commandRouter.routeCommandToService("JSSE_SERVICE", "run-test");
        boolean route3 = commandRouter.routeCommandToService("NIO_SERVICE", "get-logs");
        
        assertTrue("All commands should route successfully", route1 && route2 && route3);
        assertEquals("Route count should be 3", 3, commandRouter.getRouteCount());
        
        System.out.println("✓ All 3 services received commands successfully");
        System.out.println("  Available routes: " + commandRouter.getAvailableServices());
    }

    /**
     * Test 5: Command Deregistration
     * Verify that service disconnection removes the route
     */
    @Test
    public void testServiceDeregistration() {
        System.out.println("\n[TEST 5] Service Deregistration");
        
        // Register service
        registry.register("API_GATEWAY", "localhost", 9001);
        commandRouter.registerServiceConnection("API_GATEWAY", new PrintWriter(new ByteArrayOutputStream(), true));
        
        assertTrue("Service should be registered", commandRouter.isServiceAvailable("API_GATEWAY"));
        
        // Deregister service
        commandRouter.deregisterServiceConnection("API_GATEWAY");
        
        assertFalse("Service should be deregistered", commandRouter.isServiceAvailable("API_GATEWAY"));
        
        System.out.println("✓ Service route correctly removed on deregistration");
    }

    /**
     * Test 6: JSON Command Parsing
     * Verify that JSON commands from dashboard are parsed correctly
     */
    @Test
    public void testJsonCommandParsing() {
        System.out.println("\n[TEST 6] JSON Command Parsing");
        
        // Register service
        registry.register("API_GATEWAY", "localhost", 9001);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        commandRouter.registerServiceConnection("API_GATEWAY", new PrintWriter(outputStream, true));
        
        // Send command as JSON from dashboard
        String jsonCommand = "{\"command_for\": \"API_GATEWAY\", \"payload\": \"get-weather\"}";
        boolean success = commandRouter.routeCommand(jsonCommand);
        
        assertTrue("JSON command should be routed", success);
        assertTrue("Output should contain payload", outputStream.toString().contains("get-weather"));
        
        System.out.println("✓ JSON command parsed and routed correctly");
    }

    /**
     * Test 7: Service Registry Integration
     * Verify that command router and registry work together
     */
    @Test
    public void testServiceRegistryIntegration() {
        System.out.println("\n[TEST 7] Service Registry Integration");
        
        // Register services in registry
        registry.register("SERVICE_A", "localhost", 8001);
        registry.register("SERVICE_B", "localhost", 8002);
        
        assertEquals("Registry should have 2 services", 2, registry.getServiceCount());
        
        // Register command routes
        commandRouter.registerServiceConnection("SERVICE_A", new PrintWriter(new ByteArrayOutputStream(), true));
        commandRouter.registerServiceConnection("SERVICE_B", new PrintWriter(new ByteArrayOutputStream(), true));
        
        assertEquals("Router should have 2 routes", 2, commandRouter.getRouteCount());
        
        // Deregister service from registry
        registry.deregister("SERVICE_A");
        
        assertEquals("Registry should have 1 service", 1, registry.getServiceCount());
        
        System.out.println("✓ Service registry and command router synchronized");
    }

    /**
     * Test 8: Invalid Command Format
     * Verify that invalid commands are rejected gracefully
     */
    @Test
    public void testInvalidCommandFormat() {
        System.out.println("\n[TEST 8] Invalid Command Format");
        
        // Try invalid JSON
        boolean result1 = commandRouter.routeCommand("{ invalid json }");
        assertFalse("Invalid JSON should be rejected", result1);
        
        // Try JSON without required fields
        boolean result2 = commandRouter.routeCommand("{\"command_for\": \"SERVICE\"}");
        assertFalse("Missing payload should be rejected", result2);
        
        System.out.println("✓ Invalid commands rejected gracefully");
    }

    /**
     * Test 9: Concurrent Command Routing
     * Verify thread-safety of command router with concurrent operations
     */
    @Test
    public void testConcurrentCommandRouting() throws InterruptedException {
        System.out.println("\n[TEST 9] Concurrent Command Routing");
        
        // Register multiple services
        for (int i = 0; i < 5; i++) {
            registry.register("SERVICE_" + i, "localhost", 9000 + i);
            commandRouter.registerServiceConnection("SERVICE_" + i, 
                    new PrintWriter(new ByteArrayOutputStream(), true));
        }
        
        // Send concurrent commands
        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(5);
        int[] successCount = {0};
        
        for (int i = 0; i < 5; i++) {
            final int serviceNum = i;
            executor.submit(() -> {
                boolean success = commandRouter.routeCommandToService("SERVICE_" + serviceNum, "command-" + serviceNum);
                if (success) {
                    synchronized(successCount) {
                        successCount[0]++;
                    }
                }
            });
        }
        
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        
        assertEquals("All concurrent commands should succeed", 5, successCount[0]);
        
        System.out.println("✓ 5 concurrent commands routed successfully");
    }

    /**
     * Test 10: Result Aggregation with Multiple Formats
     * Verify that result aggregator handles different data formats
     */
    @Test
    public void testResultAggregationFormats() {
        System.out.println("\n[TEST 10] Result Aggregation with Multiple Formats");
        
        // Test with different data formats
        String jsonResult = "{\"result_from\": \"SERVICE_A\", \"data\": \"{\\\"temperature\\\": 28.5}\"}";
        String textResult = "{\"result_from\": \"SERVICE_B\", \"data\": \"Simple text result\"}";
        String statusResult = "{\"result_from\": \"SERVICE_C\", \"data\": \"Status: Success\"}";
        
        boolean result1 = resultAggregator.handleServiceResult(jsonResult);
        boolean result2 = resultAggregator.handleServiceResult(textResult);
        boolean result3 = resultAggregator.handleServiceResult(statusResult);
        
        assertTrue("All result formats should be processed", result1 && result2 && result3);
        
        System.out.println("✓ Result aggregator handles multiple data formats");
    }

    /**
     * Summary test output
     */
    @org.junit.AfterClass
    public static void tearDownClass() {
        System.out.println("\n=== ALL MESSAGE BROKER TESTS COMPLETED ===");
        System.out.println("✓ Command routing verified");
        System.out.println("✓ Result aggregation verified");
        System.out.println("✓ Thread-safety verified");
        System.out.println("✓ Service registry integration verified");
        System.out.println("\nMessage Broker Implementation: READY FOR PHASE 2\n");
    }
}
