package org.example.dto.bucket;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the response returned after updating or retrieving a bucket policy.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PolicyResponse {
    /** The ID of the bucket whose policy is being returned. */
    private String bucketId;

    /** The JSON policy document as returned by the backend. */
    private String policy;

    /** Optional metadata (if backend returns it). */
    private String name;
    private String createdAt;
    private String updatedAt;
    private Integer policyVersion;
}
