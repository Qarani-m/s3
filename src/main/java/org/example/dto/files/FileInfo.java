package org.example.dto.files;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileInfo {
    @JsonProperty("file_id")
    private String id;

    @JsonProperty("key")
    private String key;

    @JsonProperty("size")
    private long size;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("contentType")
    private String contentType;

    @JsonProperty("bucketId")
    private String bucketId;

    @JsonProperty("updatedAt")
    private String updatedAt;

    @JsonProperty("metadata")
    private Map<String, String> metadata;

    @JsonProperty("version")
    private int version;

    @JsonProperty("mimeType")
    private String mimeType;

}

