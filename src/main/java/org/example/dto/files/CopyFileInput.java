package org.example.dto.files;


import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
public class CopyFileInput {
    private String targetBucketId;
    private String targetKey;
}

