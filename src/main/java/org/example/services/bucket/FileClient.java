package org.example.services.bucket;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.example.dto.files.*;
import org.example.http.HttpClient;

import java.io.File;
import java.util.List;

public class FileClient {
    private final HttpClient http;

    public FileClient(HttpClient http) {
        this.http = http;
    }

    /**
     * Upload a file to a bucket
     */
    public FileInfo upload(String bucketId, File file) throws JsonProcessingException {
        // assuming HttpClient has a method postFile(url, file, responseClass)
        return http.postFile("/api/v1/files/upload/" + bucketId, file, FileInfo.class);
    }

    /**
     * List all files in a bucket
     */
    public List<FileInfo> list(String bucketId) throws JsonProcessingException {
        FileListResponse response = http.get("/api/v1/files/" + bucketId, FileListResponse.class);
        return response.getFiles();
    }

    /**
     * Get metadata of a single file
     */
    public FileInfo get(String bucketId, String fileId) throws JsonProcessingException {
        return http.get("/api/v1/files/" + bucketId + "/files/" + fileId, FileInfo.class);
    }

    /**
     * Download a file (returns raw bytes)
     */
    public byte[] download(String bucketId, String fileId) throws JsonProcessingException {
        return http.getBytes("/api/v1/files/" + bucketId + "/files/" + fileId + "/download");
    }

    /**
     * Delete a file
     */
    public void delete(String bucketId, String fileId) throws JsonProcessingException {
        http.delete("/api/v1/files/" + bucketId + "/files/" + fileId, Void.class);
    }

    /**
     * Update file metadata
     */
    public FileInfo updateMetadata(String bucketId, String fileId, UpdateFileMetadataInput input) throws JsonProcessingException {
        return http.patch("/api/v1/files/" + bucketId + "/files/" + fileId, input, FileInfo.class);
    }

    /**
     * Copy a file to another location/bucket
     */
    public FileInfo copy(String bucketId, String fileId, CopyFileInput input) throws JsonProcessingException {
        return http.post("/api/v1/files/" + bucketId + "/files/" + fileId + "/copy", input, FileInfo.class);
    }

    /**
     * Move a file to another location/bucket
     */
    public FileInfo move(String bucketId, String fileId, MoveFileInput input) throws JsonProcessingException {
        return http.post("/api/v1/files/" + bucketId + "/files/" + fileId + "/move", input, FileInfo.class);
    }
}

