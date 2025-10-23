package org.example.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.example.config.ClientConfig;
import org.example.exceptions.ApiExceptions;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class HttpClient implements AutoCloseable {
    private final CloseableHttpClient client;
    private final PoolingHttpClientConnectionManager connectionManager;
    private final ClientConfig config;
    private final ObjectMapper mapper = new ObjectMapper();
    private final RequestConfig requestConfig;
    private static final int MAX_RETRIES = 3;
    private static final long BASE_DELAY_MS = 1000L;


    public HttpClient(ClientConfig config) {
        this.config = config;

        // Configure connection pool
        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setConnectTimeout(Timeout.ofSeconds(config.getTimeoutSeconds()))
                .setSocketTimeout(Timeout.ofSeconds(config.getTimeoutSeconds()))
                .build();

        // Create connection manager with pooling
        this.connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(100); // Maximum total connections
        connectionManager.setDefaultMaxPerRoute(20); // Max connections per route
        connectionManager.setDefaultConnectionConfig(connectionConfig);

        // Configure request defaults
        this.requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofSeconds(config.getTimeoutSeconds()))
                .setResponseTimeout(Timeout.ofSeconds(config.getTimeoutSeconds()))
                .build();

        // Build HTTP client with connection pooling
        this.client = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .evictIdleConnections(TimeValue.ofSeconds(30)) // Clean up idle connections
                .build();
    }

    // Existing JSON methods
    public <T> T get(String path, Class<T> responseType) {
        return sendRequest(new HttpGet(buildUrl(path)), null, responseType);
    }

    public <T> T post(String path, Object body, Class<T> responseType) {
        return sendRequest(new HttpPost(buildUrl(path)), body, responseType);
    }

    public <T> T patch(String path, Object body, Class<T> responseType) {
        return sendRequest(new HttpPatch(buildUrl(path)), body, responseType);
    }

    public <T> T put(String path, Object body, Class<T> responseType) {
        return sendRequest(new HttpPut(buildUrl(path)), body, responseType);
    }

    public <T> T delete(String path, Class<T> responseType) {
        return sendRequest(new HttpDelete(buildUrl(path)), null, responseType);
    }

    /**
     * Download raw file bytes (for file downloads)
     */
    public byte[] getBytes(String path) throws ApiExceptions.ApiException {
        HttpGet request = new HttpGet(buildUrl(path));
        addAuthHeader(request);

        try {
            return client.execute(request, response -> {
                int status = response.getCode();
                byte[] body = EntityUtils.toByteArray(response.getEntity());

                if (status >= 200 && status < 300) {
                    return body;
                } else {
                    throw ApiExceptions.fromStatus(status, new String(body));
                }
            });
        } catch (IOException e) {
            throw new RuntimeException("I/O error during file download", e);
        }
    }

    /**
     * Upload file using multipart/form-data
     */
    public <T> T postFile(String path, File file, Class<T> responseType)
            throws ApiExceptions.ApiException {
        HttpPost request = new HttpPost(buildUrl(path));
        addAuthHeader(request);

        // Build multipart entity
        var multipartEntity = MultipartEntityBuilder.create()
                .addBinaryBody("file", file, ContentType.APPLICATION_OCTET_STREAM, file.getName())
                .build();

        request.setEntity(multipartEntity);

        try {
            return client.execute(request, response -> {
                int status = response.getCode();
                String body = EntityUtils.toString(response.getEntity());

                if (status >= 200 && status < 300) {
                    if (responseType == Void.class) return null;
                    return mapper.readValue(body, responseType);
                } else {
                    throw ApiExceptions.fromStatus(status, body);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException("I/O error during file upload", e);
        }
    }


    /**
     * Determine if a generic exception (network/IO errors) is retryable
     */
    private boolean isRetryable(Exception e) {
        // Network and IO errors are generally retryable
        // These include: SocketTimeoutException, ConnectException, UnknownHostException, etc.
        return e instanceof IOException;
    }

    private <T> T sendRequest(HttpUriRequestBase request, Object body, Class<T> responseType)
            throws ApiExceptions.ApiException {
        int attempts = 0;
        Exception lastException = null;

        while (attempts < MAX_RETRIES) {
            try {
                request.setHeader("Content-Type", "application/json");
                addAuthHeader(request);

                if (body != null) {
                    String json = mapper.writeValueAsString(body);
                    request.setEntity(new ByteArrayEntity(json.getBytes(), ContentType.APPLICATION_JSON));
                }
                return client.execute(request, response -> {
                    int status = response.getCode();
                    HttpEntity entity = response.getEntity();
                    String responseBody = null;

                    if (entity != null) {
                        responseBody = EntityUtils.toString(entity);
                    }

                    if (status >= 200 && status < 300) {
                        if (responseType == Void.class || responseBody == null || responseBody.isEmpty()) {
                            return null;
                        }
                        return mapper.readValue(responseBody, responseType);
                    } else {
                        throw ApiExceptions.fromStatus(status, responseBody);
                    }
                });


            } catch (Exception e) {
                lastException = e;
                attempts++;

                // Check if error is retryable
                boolean shouldRetry = false;
                if (e instanceof ApiExceptions.ApiException) {
                    shouldRetry = isRetryable(e);
                } else {
                    shouldRetry = isRetryable(e);
                }

                // Don't retry non-transient errors
                if (!shouldRetry) {
                    if (e instanceof RuntimeException) {
                        throw (RuntimeException) e;
                    } else {
                        throw new RuntimeException("Request failed", e);
                    }
                }

                // If max retries reached, exit loop
                if (attempts >= MAX_RETRIES) {
                    break;
                }

                // Exponential backoff: 1s, 2s, 4s
                long delayMs = BASE_DELAY_MS * (long) Math.pow(2, attempts - 1);

                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Retry interrupted", ie);
                }
            }
        }

        // Throw the last exception after exhausting retries
        if (lastException instanceof RuntimeException) {
            throw (RuntimeException) lastException;
        } else if (lastException instanceof ApiExceptions.ApiException) {
            throw (ApiExceptions.ApiException) lastException;
        } else {
            throw new RuntimeException("Request failed after " + MAX_RETRIES + " retries", lastException);
        }
    }




    private String buildUrl(String path) {
        return config.getBaseUrl() + path;
    }

    private void addAuthHeader(HttpUriRequestBase request) {
        if (config.getApiKey() != null && !config.getApiKey().isEmpty()) {
            request.setHeader("x-api-key", config.getApiKey());
        }
    }

    /**
     * Get connection pool statistics for monitoring
     */
    public String getPoolStats() {
        var stats = connectionManager.getTotalStats();
        return String.format("Connections [leased: %d, pending: %d, available: %d, max: %d]",
                stats.getLeased(), stats.getPending(), stats.getAvailable(), stats.getMax());
    }

    /**
     * Close the HTTP client and release resources
     */
    @Override
    public void close() {
        try {
            client.close();
            connectionManager.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing HTTP client", e);
        }
    }
}