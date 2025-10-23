package org.example.dto.bucket;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePolicyInput {

    /** Optional version to track schema or revision */
    private String version;

    /** "Allow" or "Deny" */
    private String effect;

    /** Actions affected by this policy, e.g. ["upload", "delete", "list"] */
    private List<String> actions;

    /** Resources affected, e.g. ["bucket/*", "bucket/photos/*"] */
    private List<String> resources;

    /** Principals this applies to, e.g. ["user:123", "group:admins", "public"] */
    private List<String> principals;

    /** Optional advanced conditions, like time/IP restrictions */
    private Map<String, Object> conditions;

    /** Convenience flags for simplified public access use-cases */
    private Boolean publicRead;
    private Boolean publicWrite;
}
