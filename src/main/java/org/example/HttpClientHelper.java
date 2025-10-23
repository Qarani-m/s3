package org.example;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.exceptions.*;

import javax.naming.ServiceUnavailableException;
import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class HttpClientHelper {
    public final HttpClient client;
    private final String baseUrl;
    private final String apiKey;
    private final ObjectMapper mapper = new ObjectMapper();
    private final int maxRetries = 3;

    public HttpClientHelper(String baseUrl, String apiKey, Duration timeout) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length()-1) : baseUrl;
        this.apiKey = apiKey;
        this.client = HttpClient.newBuilder()
                .connectTimeout(timeout)
                .version(HttpClient.Version.HTTP_1_1)
                .build();
    }

    // Sync
    public <T> T sendRequest(String method, String path, Object body, Map<String,String> queryParams, Class<T> responseClass) {
        HttpRequest request = buildRequest(method, path, body, queryParams);
        int attempt = 0;
        while (true) {
            attempt++;
            try {
                HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString());
                return handleResponse(resp, responseClass);
            } catch (IOException | InterruptedException e) {
                if (attempt >= maxRetries) throw new RuntimeException(e);
            }
        }
    }

    // Async
    public <T> CompletableFuture<T> sendRequestAsync(String method, String path, Object body, Map<String,String> queryParams, Class<T> responseClass) {
        HttpRequest request = buildRequest(method, path, body, queryParams);
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(resp -> handleResponse(resp, responseClass));
    }

    private HttpRequest buildRequest(String method, String path, Object body, Map<String,String> queryParams) {
        String url = baseUrl + path + buildQuery(queryParams);
        HttpRequest.Builder b = HttpRequest.newBuilder().uri(URI.create(url)).header("Accept", "application/json");
        if (apiKey != null && !apiKey.isBlank()) b.header("x-api-key", apiKey);
        if (body != null) {
            try {
                String json = mapper.writeValueAsString(body);
                b.header("Content-Type", "application/json");
                b.method(method, HttpRequest.BodyPublishers.ofString(json));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            if (method.equalsIgnoreCase("GET") || method.equalsIgnoreCase("DELETE"))
                b.method(method, HttpRequest.BodyPublishers.noBody());
            else
                b.method(method, HttpRequest.BodyPublishers.ofString(""));
        }
        return b.build();
    }

    private String buildQuery(Map<String,String> qp) {
        if (qp == null || qp.isEmpty()) return "";
        StringBuilder sb = new StringBuilder("?");
        qp.forEach((k,v)-> sb.append(k).append("=").append(encode(v)).append("&"));
        sb.setLength(sb.length()-1);
        return sb.toString();
    }
    private String encode(String s) {
        return s == null ? "" : java.net.URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8);
    }

    private <T> T handleResponse(HttpResponse<String> resp, Class<T> responseClass) {
        int code = resp.statusCode();
        String body = resp.body();
        try {
            if (code >= 200 && code < 300) {
                System.out.println("---");
                if (responseClass == Void.class) return null;
                return mapper.readValue(body, responseClass);
            } else {
               throw ApiExceptions.fromStatus(code, body);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
