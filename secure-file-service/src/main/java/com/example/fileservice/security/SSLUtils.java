package com.example.fileservice.security;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;

/**
 * SSL Utilities for creating SSL contexts and sockets
 * Member 3 - JSSE (Java Secure Socket Extension)
 */
public class SSLUtils {
    
    private static final String KEYSTORE_PATH = "keystore/fileservice.keystore";
    private static final String KEYSTORE_PASSWORD = "password";
    private static final String KEY_PASSWORD = "password";
    private static final String PROTOCOL = "TLSv1.2";
    
    /**
     * Create SSLContext with keystore configuration
     */
    public static SSLContext createSSLContext() throws IOException {
        try {
            // Load KeyStore
            KeyStore keyStore = KeyStore.getInstance("JKS");
            try (FileInputStream fis = new FileInputStream(KEYSTORE_PATH)) {
                keyStore.load(fis, KEYSTORE_PASSWORD.toCharArray());
            }
            
            // Initialize KeyManagerFactory
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(
                KeyManagerFactory.getDefaultAlgorithm()
            );
            kmf.init(keyStore, KEY_PASSWORD.toCharArray());
            
            // Create SSL Context
            SSLContext sslContext = SSLContext.getInstance(PROTOCOL);
            sslContext.init(kmf.getKeyManagers(), null, new SecureRandom());
            
            System.out.println("[SSL] SSLContext created successfully");
            System.out.println("[SSL] Protocol: " + PROTOCOL);
            System.out.println("[SSL] Keystore: " + KEYSTORE_PATH);
            
            return sslContext;
            
        } catch (KeyStoreException | NoSuchAlgorithmException | 
                 CertificateException | UnrecoverableKeyException | 
                 KeyManagementException e) {
            System.err.println("[SSL ERROR] Failed to create SSLContext: " + e.getMessage());
            throw new IOException("SSL initialization failed", e);
        }
    }
    
    /**
     * Create SSLContext for client (trusts all certificates - for demo only)
     */
    public static SSLContext createClientSSLContext() throws IOException {
        try {
            // Create trust manager that trusts all certificates (DEMO ONLY!)
            TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
            };
            
            // Create SSL Context
            SSLContext sslContext = SSLContext.getInstance(PROTOCOL);
            sslContext.init(null, trustAllCerts, new SecureRandom());
            
            System.out.println("[SSL CLIENT] SSLContext created (trust all)");
            
            return sslContext;
            
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            System.err.println("[SSL ERROR] Failed to create client SSLContext: " + e.getMessage());
            throw new IOException("SSL client initialization failed", e);
        }
    }
    
    /**
     * Get SSL server socket factory
     */
    public static SSLServerSocketFactory getServerSocketFactory() throws IOException {
        SSLContext context = createSSLContext();
        return context.getServerSocketFactory();
    }
    
    /**
     * Get SSL client socket factory
     */
    public static SSLSocketFactory getClientSocketFactory() throws IOException {
        SSLContext context = createClientSSLContext();
        return context.getSocketFactory();
    }
    
    /**
     * Print SSL session information
     */
    public static void printSessionInfo(SSLSocket socket) {
        try {
            SSLSession session = socket.getSession();
            System.out.println("[SSL INFO] =====================================");
            System.out.println("[SSL INFO] Protocol: " + session.getProtocol());
            System.out.println("[SSL INFO] Cipher Suite: " + session.getCipherSuite());
            System.out.println("[SSL INFO] Peer Host: " + session.getPeerHost());
            System.out.println("[SSL INFO] Peer Port: " + session.getPeerPort());
            System.out.println("[SSL INFO] =====================================");
        } catch (Exception e) {
            System.err.println("[SSL ERROR] Could not print session info: " + e.getMessage());
        }
    }
}
