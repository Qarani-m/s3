package org.example;


import org.example.config.ClientConfig;
import org.example.http.HttpClient;
import org.example.services.bucket.BucketClient;
import org.example.services.bucket.ObjectClient;

public class S3CloneClient {
    private final HttpClient httpClient;
    private final BucketClient bucketClient;


    public S3CloneClient(ClientConfig config) {
        this.httpClient = new HttpClient(config);

        this.bucketClient = new BucketClient(httpClient);
    }

    public BucketClient buckets() {
        return bucketClient;
    }
}
