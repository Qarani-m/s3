package org.example.services.bucket;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.example.dto.bucket.*;
import org.example.http.HttpClient;


import java.util.List;

public class BucketClient {
    private final HttpClient http;

    public BucketClient(HttpClient http) {
        this.http = http;
    }

    public Bucket create(CreateBucketInput input) throws JsonProcessingException {
        return http.post("/api/v1/buckets", input, Bucket.class);
    }

    public List<Bucket> list() throws  JsonProcessingException {
        BucketListResponse response = http.get("/api/v1/buckets", BucketListResponse.class);
        return response.getBuckets();
    }

    public Bucket get(String bucketId) throws JsonProcessingException {
        return http.get("/api/v1/buckets/" + bucketId, Bucket.class);
    }

    public Bucket update(String bucketId, UpdateBucketInput input) throws JsonProcessingException {
        return http.patch("/api/v1/buckets/" + bucketId, input, Bucket.class);
    }
    public BucketStats stats(String bucketId) throws JsonProcessingException {
        return http.get("/api/v1/buckets/" + bucketId + "/stats", BucketStats.class);
    }

    public void delete(String bucketId) throws JsonProcessingException {
        http.delete("/api/v1/buckets/" + bucketId, Void.class);
    }

    public PolicyResponse  getPolicy(String bucketId) throws JsonProcessingException {
        return http.get("/api/v1/buckets/" + bucketId + "/policy", PolicyResponse .class);
    }

    public void updatePolicy(String bucketId, UpdatePolicyInput input) throws JsonProcessingException {
        http.put("/api/v1/buckets/" + bucketId + "/policy", input, Void.class);
    }

    public VersioningOutput getVersioning(String bucketId) throws JsonProcessingException {
        return http.get("/api/v1/buckets/" + bucketId + "/versioning", VersioningOutput.class);
    }

    public void setVersioning(String bucketId, VersioningInput input) throws JsonProcessingException {
        http.put("/api/v1/buckets/" + bucketId + "/versioning", input, Void.class);
    }

    public void setLifecycle(String bucketId, LifecycleInput input) throws JsonProcessingException {
        http.put("/api/v1/buckets/" + bucketId + "/lifecycle", input, Void.class);
    }

}