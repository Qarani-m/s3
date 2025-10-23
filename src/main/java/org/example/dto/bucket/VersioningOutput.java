package org.example.dto.bucket;


import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;


@Data
@ToString
@Getter
@RequiredArgsConstructor
public class VersioningOutput {
    private String status;
    private boolean enabled;
}
