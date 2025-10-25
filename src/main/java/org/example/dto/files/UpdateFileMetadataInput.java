package org.example.dto.files;

import lombok.*;

import java.util.Map;

@Data

@Builder
public class UpdateFileMetadataInput {
    private Map<String, String> metadata;
}