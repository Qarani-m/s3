package org.example.dto.bucket;


import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Data
@ToString
@RequiredArgsConstructor
public class VersioningInput {
    private boolean enabled;

}
