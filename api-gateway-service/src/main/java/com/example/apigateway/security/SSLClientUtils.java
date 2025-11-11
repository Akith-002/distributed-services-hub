package com.example.apigateway.security;

import javax.net.ssl.*;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * SSL Client Utilities for connecting to Secure Services
 * 
 * This utility provides SSL context configuration for the API Gateway
 * to securely connect to the Secure File Service.
 * 
 * For development/demo purposes, this creates a TrustManager that accepts
 * self-signed certificates. In production, use proper certificate validation.
 */
public class SSLClientUtils {
    
    private static final String PROTOCOL = "TLSv1.2";
    private static SSLContext sslContext = null;
    
    /**
     * Get SSL socket factory that trusts self-signed certificates
     * 
     * WARNING: This is for development/demo only!
     * In production, use proper certificate validation with a truststore.
     */
    public static SSLSocketFactory getSSLSocketFactory() throws IOException {
        if (sslContext == null) {
            sslContext = createClientSSLContext();
        }
        return sslContext.getSocketFactory();
    }
    
    /**
     * Create SSLContext that accepts self-signed certificates
     * 
     * This is implemented as a trust-all approach suitable for development
     * and demonstration purposes where the server uses self-signed certificates.
     */
    private static SSLContext createClientSSLContext() throws IOException {
        try {
            // Create a trust manager that accepts all certificates
            // This is a custom X509TrustManager implementation
            TrustManager[] trustAllCerts = new TrustManager[] {
                new javax.net.ssl.X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    
                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs,
                            String authType) {
                        // Accept all client certificates
                    }
                    
                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs,
                            String authType) {
                        // Accept all server certificates (including self-signed)
                        // This bypasses the PKIX path building error
                    }
                }
            };
            
            // Create and initialize SSL Context
            SSLContext sslContext = SSLContext.getInstance(PROTOCOL);
            sslContext.init(null, trustAllCerts, new SecureRandom());
            
            System.out.println("[SSL CLIENT] ✓ SSL Context initialized (trust-all for self-signed certs)");
            System.out.println("[SSL CLIENT]   Protocol: " + PROTOCOL);
            System.out.println("[SSL CLIENT]   WARNING: This accepts ALL certificates (demo only!)");
            
            return sslContext;
            
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            System.err.println("[SSL CLIENT ERROR] Failed to create SSLContext: " + e.getMessage());
            throw new IOException("SSL client initialization failed", e);
        }
    }
    
    /**
     * Disable hostname verification (for self-signed certs)
     * 
     * WARNING: This is insecure and should only be used for development/testing!
     */
    public static void disableHostnameVerification() {
        // Create a hostname verifier that accepts all hostnames
        HostnameVerifier allHostsValid = (hostname, session) -> {
            System.out.println("[SSL CLIENT] Hostname verification disabled for: " + hostname);
            return true;
        };
        
        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    }
}
