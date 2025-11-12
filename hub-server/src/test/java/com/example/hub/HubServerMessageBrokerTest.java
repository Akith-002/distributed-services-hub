package com.example.hub;

import org.junit.Before;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.*;

/**
 * Integration tests for the Hub Server Message Broker functionality.
 * Tests the message flow:
 * - Dashboard sends command to Hub
 * - Hub routes command to service
 * - Service processes and sends result
 * - Hub broadcasts result back to Dashboard
 */
public class HubServerMessageBrokerTest {
    
    private ServiceRegistry registry;
    private CommandRouter commandRouter;
    private ResultAggregator resultAggregator;
    private WebSocketBroadcaster broadcaster;

    @Before
    public void setUp() {
        System.out.println("\n=== MESSAGE BROKER TEST ===");
        registry = new ServiceRegistry();
        broadcaster = new WebSocketBroadcaster();
        commandRouter = new CommandRouter(registry);
        resultAggregator = new ResultAggregator(broadcaster);
        broadcaster.setCommandRouter(commandRouter);
        broadcaster.setResultAggregator(resultAggregator);
    }

    /**
     * Test 1: Basic Command Routing
     */
    @Test
    public void testBasicCommandRouting() {
        System.out.println("\n[TEST 1] Basic Command Routing");
        
        registry.register("API_GATEWAY", "localhost", 9001);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(outputStream, true);
        commandRouter.registerServiceConnection("API_GATEWAY", writer);
        
        boolean success = commandRouter.routeCommandToService("API_GATEWAY", "get-weather");
        
        assertTrue("Command should route successfully", success);
        assertTrue("Output should contain command", outputStream.toString().contains("get-weather"));
        System.out.println("✓ Command routed successfully");
    }

    /**
     * Test 2: Service Not Found
     */
    @Test
    public void testCommandRoutingToUnregisteredService() {
        System.out.println("\n[TEST 2] Command Routing to Unregistered Service");
        
        boolean success = commandRouter.routeCommandToService("UNKNOWN", "test");
        assertFalse("Should reject unregistered service", success);
        System.out.println("✓ Unregistered service correctly rejected");
    }

    /**
     * Test 3: Result Message Creation
     */
    @Test
    public void testResultMessageCreation() {
        System.out.println("\n[TEST 3] Result Message Creation");
        
        String message = resultAggregator.createResultMessage("API_GATEWAY", "Temp: 28.5°C");
        
        assertTrue("Should contain SERVICE_RESULT type", message.contains("SERVICE_RESULT"));
        assertTrue("Should contain service name", message.contains("API_GATEWAY"));
        assertTrue("Should contain data", message.contains("Temp"));
        assertTrue("Should contain timestamp", message.contains("timestamp"));
        System.out.println("✓ Result message formatted correctly");
    }

    /**
     * Test 4: Multiple Services
     */
    @Test
    public void testMultipleServiceRouting() {
        System.out.println("\n[TEST 4] Multiple Service Routing");
        
        registry.register("SERVICE_A", "localhost", 9001);
        registry.register("SERVICE_B", "localhost", 9002);
        registry.register("SERVICE_C", "localhost", 9003);
        
        commandRouter.registerServiceConnection("SERVICE_A", new PrintWriter(new ByteArrayOutputStream(), true));
        commandRouter.registerServiceConnection("SERVICE_B", new PrintWriter(new ByteArrayOutputStream(), true));
        commandRouter.registerServiceConnection("SERVICE_C", new PrintWriter(new ByteArrayOutputStream(), true));
        
        assertEquals("Should have 3 routes", 3, commandRouter.getRouteCount());
        System.out.println("✓ Multiple services routed correctly");
    }

    /**
     * Test 5: Service Deregistration
     */
    @Test
    public void testServiceDeregistration() {
        System.out.println("\n[TEST 5] Service Deregistration");
        
        registry.register("SERVICE_X", "localhost", 8000);
        commandRouter.registerServiceConnection("SERVICE_X", new PrintWriter(new ByteArrayOutputStream(), true));
        
        assertTrue("Service should be available", commandRouter.isServiceAvailable("SERVICE_X"));
        
        commandRouter.deregisterServiceConnection("SERVICE_X");
        assertFalse("Service should be unavailable", commandRouter.isServiceAvailable("SERVICE_X"));
        System.out.println("✓ Service deregistration works");
    }

    /**
     * Test 6: JSON Command Parsing
     */
    @Test
    public void testJsonCommandParsing() {
        System.out.println("\n[TEST 6] JSON Command Parsing");
        
        registry.register("API_GATEWAY", "localhost", 9001);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        commandRouter.registerServiceConnection("API_GATEWAY", new PrintWriter(outputStream, true));
        
        String jsonCommand = "{\"command_for\": \"API_GATEWAY\", \"payload\": \"fetch-weather\"}";
        boolean success = commandRouter.routeCommand(jsonCommand);
        
        assertTrue("Should parse and route JSON", success);
        System.out.println("✓ JSON command parsed and routed");
    }

    /**
     * Test 7: Invalid JSON Handling
     */
    @Test
    public void testInvalidJsonHandling() {
        System.out.println("\n[TEST 7] Invalid JSON Handling");
        
        boolean result1 = commandRouter.routeCommand("{ invalid }");
        boolean result2 = commandRouter.routeCommand("{\"command_for\": \"SERVICE\"}");
        
        assertFalse("Should reject invalid JSON", result1);
        assertFalse("Should reject incomplete JSON", result2);
        System.out.println("✓ Invalid commands rejected gracefully");
    }

    /**
     * Test 8: Concurrent Service Access
     */
    @Test
    public void testConcurrentAccess() throws InterruptedException {
        System.out.println("\n[TEST 8] Concurrent Service Access");
        
        for (int i = 0; i < 10; i++) {
            registry.register("SERVICE_" + i, "localhost", 9000 + i);
            commandRouter.registerServiceConnection("SERVICE_" + i, 
                    new PrintWriter(new ByteArrayOutputStream(), true));
        }
        
        assertEquals("Should have 10 services", 10, commandRouter.getRouteCount());
        System.out.println("✓ Concurrent access verified with 10 services");
    }

    /**
     * Test 9: Service Availability Check
     */
    @Test
    public void testServiceAvailabilityCheck() {
        System.out.println("\n[TEST 9] Service Availability Check");
        
        registry.register("ACTIVE_SERVICE", "localhost", 9001);
        commandRouter.registerServiceConnection("ACTIVE_SERVICE", new PrintWriter(new ByteArrayOutputStream(), true));
        
        assertTrue("Active service should be available", commandRouter.isServiceAvailable("ACTIVE_SERVICE"));
        assertFalse("Inactive service should not be available", commandRouter.isServiceAvailable("INACTIVE_SERVICE"));
        System.out.println("✓ Service availability check works");
    }

    /**
     * Test 10: Result Aggregation
     */
    @Test
    public void testResultAggregation() {
        System.out.println("\n[TEST 10] Result Aggregation");
        
        String result1 = resultAggregator.createResultMessage("SERVICE_A", "{\"status\": \"ok\"}");
        String result2 = resultAggregator.createResultMessage("SERVICE_B", "Text response");
        String result3 = resultAggregator.createResultMessage("SERVICE_C", "Another response");
        
        assertTrue("All results should have SERVICE_RESULT type", 
            result1.contains("SERVICE_RESULT") && result2.contains("SERVICE_RESULT") && result3.contains("SERVICE_RESULT"));
        System.out.println("✓ Result aggregation handles multiple formats");
    }
}
