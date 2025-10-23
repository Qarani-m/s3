package org.example.config;


public class ClientConfig {
    private final String baseUrl;
    private final String apiKey; // optional
    private final int timeoutSeconds;

    public ClientConfig( String apiKey, int timeoutSeconds) {
        this.baseUrl = "http://localhost:8080";
        this.apiKey = apiKey;
        this.timeoutSeconds = timeoutSeconds;
    }

    public String getBaseUrl() { return baseUrl; }
    public String getApiKey() { return apiKey; }
    public int getTimeoutSeconds() { return timeoutSeconds; }
}
