package org.example.dto.bucket;


import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Data
@ToString
@RequiredArgsConstructor
public class UpdateBucketInput {
    private String name;
}
