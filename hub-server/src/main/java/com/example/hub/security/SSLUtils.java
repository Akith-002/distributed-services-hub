package com.example.hub.security;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Utility class for SSL/TLS configuration and management.
 * Provides methods to load keystores, create SSL contexts, and configure trust managers.
 * 
 * Adapted from original implementation for use in Hub Server.
 * 
 * @author Member 1 - Hub Server Implementation
 * @version 1.0
 */
public class SSLUtils {

    /**
     * Loads a KeyStore from file system.
     * 
     * @param keystorePath Path to the keystore file
     * @param password Password for the keystore
     * @return Loaded KeyStore instance
     * @throws KeyStoreException If keystore cannot be initialized
     * @throws IOException If file cannot be read
     * @throws NoSuchAlgorithmException If keystore algorithm is not available
     * @throws CertificateException If certificates cannot be loaded
     */
    public static KeyStore loadKeyStore(String keystorePath, char[] password)
            throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        
        KeyStore keyStore = KeyStore.getInstance("JKS");
        try (FileInputStream fis = new FileInputStream(keystorePath)) {
            keyStore.load(fis, password);
        }
        System.out.println("KeyStore loaded from: " + keystorePath);
        return keyStore;
    }

    /**
     * Creates an SSLContext for server using provided KeyStore.
     * Configures both KeyManager and TrustManager for mutual TLS support.
     * 
     * @param keyStore KeyStore containing server certificate and private key
     * @param password Password for the keystore
     * @return Configured SSLContext with TLSv1.3
     * @throws Exception If SSL context cannot be initialized
     */
    public static SSLContext createServerSSLContext(KeyStore keyStore, char[] password) throws Exception {
        // Initialize KeyManagerFactory with keystore
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, password);

        // Initialize TrustManagerFactory with keystore (for mutual TLS, optional)
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(keyStore);

        // Create and initialize SSLContext
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());
        
        System.out.println("Server SSLContext initialized with TLS");
        return sslContext;
    }

    /**
     * Creates an SSLContext for client with custom TrustStore.
     * Use this for production with proper certificate validation.
     * 
     * @param trustStorePath Path to client truststore
     * @param password Password for the truststore
     * @return Configured SSLContext for client
     * @throws Exception If SSL context cannot be initialized
     */
    public static SSLContext createClientSSLContext(String trustStorePath, char[] password) throws Exception {
        KeyStore trustStore = loadKeyStore(trustStorePath, password);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tmf.getTrustManagers(), new SecureRandom());
        
        System.out.println("Client SSLContext initialized with custom TrustStore");
        return sslContext;
    }

    /**
     * Gets the configured keystore path from system properties.
     * 
     * @return Keystore path or default value
     */
    public static String getKeystorePath() {
        return System.getProperty("javax.net.ssl.keyStore", "keystore/server.keystore");
    }

    /**
     * Gets the configured keystore password from system properties.
     * 
     * @return Keystore password or default value
     */
    public static String getKeystorePassword() {
        return System.getProperty("javax.net.ssl.keyStorePassword", "changeit");
    }
}
