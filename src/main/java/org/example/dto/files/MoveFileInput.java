package org.example.dto.files;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class MoveFileInput {
    private String targetBucketId;
    private String targetKey;

}