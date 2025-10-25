package org.example.dto.files;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
public class MoveFileInput {
    @JsonProperty("destination_bucket")
    private String targetBucketId;
    @JsonProperty("new_key")
    private String targetKey;

}