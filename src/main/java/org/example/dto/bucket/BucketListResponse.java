package org.example.dto.bucket;


import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@ToString
@RequiredArgsConstructor
public class BucketListResponse {
    private int count;
    private List<Bucket> buckets;
}
