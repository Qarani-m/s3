package org.example;

import org.example.config.ClientConfig;
import org.example.http.HttpClient;
import org.example.services.bucket.BucketClient;

import java.time.Duration;

/**
 * Root entry point for your S3-clone SDK.
 * Provides access to different service areas (buckets, objects, policies, etc.)
 */
public class S3Client {
    private final HttpClient http;
    private final BucketClient bucketService;

    private S3Client(Builder b) {
        ClientConfig cfg = new ClientConfig(b.baseUrl, b.apiKey, (int) b.timeout.getSeconds());
        this.http = new HttpClient(cfg);
        this.bucketService = new BucketClient(http);
    }

    public BucketClient bucket() {
        return bucketService;
    }

    public static class Builder {
        private String baseUrl;
        private String apiKey;
        private Duration timeout = Duration.ofSeconds(30);

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public Builder timeout(Duration t) {
            this.timeout = t;
            return this;
        }

        public S3Client build() {
            if (baseUrl == null || baseUrl.isEmpty())
                throw new IllegalArgumentException("Base URL is required");
            return new S3Client(this);
        }
    }
}
