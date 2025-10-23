package org.example.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.config.ClientConfig;
import org.example.exceptions.*;

import java.io.*;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.time.Duration;

/**
 * Generic HTTP client wrapper used across all SDK services.
 */
public class HttpClient {
    private final java.net.http.HttpClient client;
    private final ClientConfig config;
    private final ObjectMapper mapper = new ObjectMapper();

    public HttpClient(ClientConfig config) {
        this.config = config;
        this.client = java.net.http.HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(config.getTimeoutSeconds()))
                .build();
    }

    // Existing JSON methods
    public <T> T get(String path, Class<T> responseType) throws JsonProcessingException {
        return sendRequest("GET", path, null, responseType);
    }

    public <T> T post(String path, Object body, Class<T> responseType) throws JsonProcessingException {
        return sendRequest("POST", path, body, responseType);
    }

    public <T> T patch(String path, Object body, Class<T> responseType) throws JsonProcessingException {
        return sendRequest("PATCH", path, body, responseType);
    }

    public <T> T put(String path, Object body, Class<T> responseType) throws JsonProcessingException {
        return sendRequest("PUT", path, body, responseType);
    }

    public <T> T delete(String path, Class<T> responseType) throws JsonProcessingException {
        return sendRequest("DELETE", path, null, responseType);
    }

    /**
     * Download raw file bytes (for file downloads)
     */
    public byte[] getBytes(String path) throws ApiExceptions.ApiException {
        try {
            String url = config.getBaseUrl() + path;
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(config.getTimeoutSeconds()));

            if (config.getApiKey() != null && !config.getApiKey().isEmpty()) {
                builder.header("Authorization", "Bearer " + config.getApiKey());
            }

            HttpRequest request = builder.GET().build();
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

            int status = response.statusCode();
            if (status >= 200 && status < 300) {
                return response.body();
            } else {
                throw ApiExceptions.fromStatus(status, new String(response.body()));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Download interrupted", e);
        } catch (IOException e) {
            throw new RuntimeException("I/O error during file download", e);
        }
    }

    /**
     * Upload file using multipart/form-data
     */
    public <T> T postFile(String path, File file, Class<T> responseType)
            throws ApiExceptions.ApiException, JsonProcessingException {
        try {
            String url = config.getBaseUrl() + path;
            String boundary = "----Boundary" + System.currentTimeMillis();

            // Build multipart request body
            var byteArrayOutputStream = new ByteArrayOutputStream();
            var writer = new PrintWriter(new OutputStreamWriter(byteArrayOutputStream));

            // File part
            writer.append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"")
                    .append(file.getName()).append("\"\r\n");
            writer.append("Content-Type: application/octet-stream\r\n\r\n");
            writer.flush();

            Files.copy(file.toPath(), byteArrayOutputStream);
            byteArrayOutputStream.write("\r\n".getBytes());

            writer.append("--").append(boundary).append("--").append("\r\n");
            writer.close();

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(config.getTimeoutSeconds()))
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary);

            if (config.getApiKey() != null && !config.getApiKey().isEmpty()) {
                builder.header("Authorization", "Bearer " + config.getApiKey());
            }

            HttpRequest request = builder
                    .POST(HttpRequest.BodyPublishers.ofByteArray(byteArrayOutputStream.toByteArray()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();

            if (status >= 200 && status < 300) {
                if (responseType == Void.class) return null;
                return mapper.readValue(response.body(), responseType);
            } else {
                throw ApiExceptions.fromStatus(status, response.body());
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Upload interrupted", e);
        } catch (IOException e) {
            throw new RuntimeException("I/O error during file upload", e);
        }
    }

    // --- existing sendRequest() (unchanged) ---
    private <T> T sendRequest(String method, String path, Object body, Class<T> responseType)
            throws ApiExceptions.ApiException, JsonProcessingException {
        try {
            String url = config.getBaseUrl() + path;
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(config.getTimeoutSeconds()))
                    .header("Content-Type", "application/json");

            if (config.getApiKey() != null && !config.getApiKey().isEmpty()) {
                builder.header("x-api-key", config.getApiKey());
            }

            if (body != null) {
                String json = mapper.writeValueAsString(body);
                builder.method(method, HttpRequest.BodyPublishers.ofString(json));
            } else {
                builder.method(method, HttpRequest.BodyPublishers.noBody());
            }

            HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            String responseBody = response.body();

            if (status >= 200 && status < 300) {
                if (responseType == Void.class) return null;
                return mapper.readValue(responseBody, responseType);
            } else {
                throw ApiExceptions.fromStatus(status, responseBody);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Request interrupted", e);
        } catch (IOException e) {
            throw new RuntimeException("I/O error during request", e);
        }
    }
}
