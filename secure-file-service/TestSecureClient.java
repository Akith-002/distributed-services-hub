import javax.net.ssl.*;
import java.io.*;
import java.net.*;
import java.security.cert.X509Certificate;

/**
 * Test client for Secure File Service
 * Tests SSL connection to the SSLServerSocket on port 9090
 */
public class TestSecureClient {
    
    private static final String HOST = "localhost";
    private static final int PORT = 9090;
    
    public static void main(String[] args) {
        System.out.println("🔐 Secure File Service - SSL Connection Test\n");
        
        try {
            // Create a trust manager that trusts all certificates (for testing only!)
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {}
                    public void checkServerTrusted(X509Certificate[] chain, String authType) {}
                    public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                }
            };
            
            // Initialize SSL context
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            
            // Create SSL socket factory
            SSLSocketFactory factory = sslContext.getSocketFactory();
            
            System.out.println("Connecting to " + HOST + ":" + PORT + " via SSL...");
            
            // Connect to the SSL server
            SSLSocket socket = (SSLSocket) factory.createSocket(HOST, PORT);
            
            // Get SSL session information
            SSLSession session = socket.getSession();
            
            System.out.println("✅ SSL Connection Established!\n");
            System.out.println("SSL Session Information:");
            System.out.println("  Protocol: " + session.getProtocol());
            System.out.println("  Cipher Suite: " + session.getCipherSuite());
            System.out.println("  Peer Host: " + session.getPeerHost());
            System.out.println("  Peer Port: " + session.getPeerPort());
            
            // Try to communicate with the server
            System.out.println("\n📤 Sending test message to server...");
            
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            // Send a test message
            out.println("HELLO");
            
            // Wait for response (with timeout)
            socket.setSoTimeout(2000); // 2 second timeout
            
            try {
                String response = in.readLine();
                if (response != null) {
                    System.out.println("📥 Server Response: " + response);
                } else {
                    System.out.println("⚠️  No response from server (this is normal for socket-based service)");
                }
            } catch (SocketTimeoutException e) {
                System.out.println("⚠️  No immediate response (server may be waiting for specific protocol)");
            }
            
            // Close connection
            socket.close();
            
            System.out.println("\n✅ Test completed successfully!");
            System.out.println("\n📋 Summary:");
            System.out.println("  ✅ SSL connection established");
            System.out.println("  ✅ TLS protocol: " + session.getProtocol());
            System.out.println("  ✅ Encryption active: " + session.getCipherSuite());
            System.out.println("  ✅ Secure File Service is running and accepting SSL connections!");
            
        } catch (UnknownHostException e) {
            System.err.println("❌ ERROR: Unknown host - " + HOST);
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("❌ ERROR: Cannot connect to server at " + HOST + ":" + PORT);
            System.err.println("   Make sure Secure File Service is running!");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ ERROR: SSL setup failed");
            e.printStackTrace();
        }
    }
}
