package org.example.dto.bucket;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Data
@ToString
@RequiredArgsConstructor
public class BucketStats {

    @JsonProperty("bucket_id")
    private String bucketId;

    @JsonProperty("total_files")
    private int totalFiles;

    @JsonProperty("total_size_bytes")
    private long totalSize;

    @JsonProperty("last_updated")
    private String lastUpdated;

}
