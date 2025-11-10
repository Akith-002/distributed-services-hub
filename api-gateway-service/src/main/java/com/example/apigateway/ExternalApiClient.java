package com.example.apigateway;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * ExternalApiClient - Handles HTTP calls to external APIs using HttpURLConnection
 * 
 * This class demonstrates the use of HttpURLConnection (Lesson 5 - Network Programming)
 * for making HTTP requests to external services and parsing JSON responses.
 * 
 * Features:
 * - Uses HttpURLConnection (NOT Retrofit, OkHttp, or other libraries)
 * - Handles timeouts and connection errors
 * - Parses JSON responses
 * - Implements proper resource cleanup
 */
public class ExternalApiClient {
    
    // Open-Meteo Free Weather API (no API key required)
    private static final String WEATHER_API_BASE = "https://api.open-meteo.com/v1/forecast";
    private static final int CONNECT_TIMEOUT = 5000; // 5 seconds
    private static final int READ_TIMEOUT = 5000;    // 5 seconds
    
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
    
    /**
     * Fetch weather data for a given location
     * Uses HttpURLConnection to call Open-Meteo API
     * 
     * @param latitude Location latitude
     * @param longitude Location longitude
     * @param locationName Human-readable location name (for display)
     * @return WeatherData object containing temperature and condition
     * @throws IOException if HTTP connection fails
     */
    public WeatherData fetchWeather(double latitude, double longitude, String locationName) throws IOException {
        System.out.println("[ExternalApiClient] Fetching weather for: " + locationName + 
                " (lat=" + latitude + ", lon=" + longitude + ")");
        
        try {
            // Build URL with parameters
            String urlString = String.format(
                "%s?latitude=%.4f&longitude=%.4f&current=temperature_2m,weather_code&temperature_unit=celsius",
                WEATHER_API_BASE, latitude, longitude
            );
            
            URL url = new URL(urlString);
            
            // Create HttpURLConnection
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            
            try {
                // Configure connection
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(CONNECT_TIMEOUT);
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setRequestProperty("User-Agent", USER_AGENT);
                conn.setRequestProperty("Accept", "application/json");
                
                System.out.println("[ExternalApiClient] Sending GET request to: " + urlString);
                
                // Get response code
                int responseCode = conn.getResponseCode();
                System.out.println("[ExternalApiClient] Response Code: " + responseCode);
                
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    throw new IOException("HTTP Error: " + responseCode);
                }
                
                // Read response
                String jsonResponse = readResponse(conn);
                System.out.println("[ExternalApiClient] Response received: " + jsonResponse.substring(0, Math.min(100, jsonResponse.length())) + "...");
                
                // Parse JSON
                WeatherData weatherData = parseWeatherResponse(jsonResponse, locationName);
                System.out.println("[ExternalApiClient] ✓ Successfully fetched weather data");
                
                return weatherData;
                
            } finally {
                conn.disconnect();
            }
            
        } catch (IOException e) {
            System.err.println("[ExternalApiClient] Error fetching weather: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Fetch weather by city name using geocoding
     * 
     * @param cityName City name (e.g., "Colombo", "New York")
     * @return WeatherData object
     * @throws IOException if HTTP connection fails or city not found
     */
    public WeatherData fetchWeatherByCity(String cityName) throws IOException {
        System.out.println("[ExternalApiClient] Fetching coordinates for city: " + cityName);
        
        // Common city coordinates (hardcoded for demo, can be extended)
        CityCoordinates coords = getCityCoordinates(cityName);
        if (coords == null) {
            throw new IOException("City not found: " + cityName);
        }
        
        return fetchWeather(coords.latitude, coords.longitude, cityName);
    }
    
    /**
     * Get coordinates for known cities (demo data)
     */
    private CityCoordinates getCityCoordinates(String cityName) {
        // This would normally call a geocoding API, but for demo we use hardcoded values
        switch (cityName.toLowerCase().trim()) {
            case "colombo":
                return new CityCoordinates(6.9271, 80.7789, "Colombo, Sri Lanka");
            case "new york":
            case "nyc":
                return new CityCoordinates(40.7128, -74.0060, "New York, USA");
            case "london":
                return new CityCoordinates(51.5074, -0.1278, "London, UK");
            case "tokyo":
                return new CityCoordinates(35.6762, 139.6503, "Tokyo, Japan");
            case "sydney":
                return new CityCoordinates(-33.8688, 151.2093, "Sydney, Australia");
            case "paris":
                return new CityCoordinates(48.8566, 2.3522, "Paris, France");
            case "dubai":
                return new CityCoordinates(25.2048, 55.2708, "Dubai, UAE");
            case "singapore":
                return new CityCoordinates(1.3521, 103.8198, "Singapore");
            default:
                return null;
        }
    }
    
    /**
     * Read response from HttpURLConnection
     */
    private String readResponse(HttpURLConnection conn) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
    
    /**
     * Parse weather JSON response
     */
    private WeatherData parseWeatherResponse(String jsonResponse, String locationName) throws IOException {
        try {
            JsonObject json = JsonParser.parseString(jsonResponse).getAsJsonObject();
            
            // Extract current weather data
            JsonObject current = json.getAsJsonObject("current");
            double temperature = current.get("temperature_2m").getAsDouble();
            int weatherCode = current.get("weather_code").getAsInt();
            
            // Get weather description from code
            String condition = getWeatherDescription(weatherCode);
            
            return new WeatherData(locationName, temperature, condition, weatherCode);
            
        } catch (Exception e) {
            throw new IOException("Failed to parse weather response: " + e.getMessage(), e);
        }
    }
    
    /**
     * Convert WMO weather code to human-readable description
     */
    private String getWeatherDescription(int code) {
        // WMO Weather interpretation codes
        if (code == 0) return "Clear sky";
        if (code == 1 || code == 2) return "Mainly clear";
        if (code == 3) return "Overcast";
        if (code == 45 || code == 48) return "Foggy";
        if (code >= 51 && code <= 67) return "Drizzle";
        if (code >= 71 && code <= 77) return "Snow";
        if (code >= 80 && code <= 82) return "Rain showers";
        if (code >= 85 && code <= 86) return "Snow showers";
        if (code == 80) return "Slight rain";
        if (code == 81) return "Moderate rain";
        if (code == 82) return "Heavy rain";
        if (code >= 95 && code <= 99) return "Thunderstorm";
        return "Unknown";
    }
    
    /**
     * WeatherData - Data class for weather information
     */
    public static class WeatherData {
        public String location;
        public double temperature;
        public String condition;
        public int weatherCode;
        public long timestamp;
        
        public WeatherData(String location, double temperature, String condition, int weatherCode) {
            this.location = location;
            this.temperature = temperature;
            this.condition = condition;
            this.weatherCode = weatherCode;
            this.timestamp = System.currentTimeMillis();
        }
        
        public WeatherData() {
            this.timestamp = System.currentTimeMillis();
        }
        
        @Override
        public String toString() {
            return String.format("Weather{location='%s', temp=%.1f°C, condition='%s', code=%d}",
                    location, temperature, condition, weatherCode);
        }
    }
    
    /**
     * CityCoordinates - Helper class for storing city coordinates
     */
    private static class CityCoordinates {
        double latitude;
        double longitude;
        String displayName;
        
        CityCoordinates(double latitude, double longitude, String displayName) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.displayName = displayName;
        }
    }
}
