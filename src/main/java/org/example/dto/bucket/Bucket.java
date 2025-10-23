package org.example.dto.bucket;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Data
@ToString
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Bucket {
    @JsonProperty("bucket_id")
    private String bucketId;

    @JsonProperty("owner_id")
    private String ownerId;

    private String name;

    @JsonProperty("created_at")
    private String createdAt;

}

