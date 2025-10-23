package org.example.dto.bucket;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class LifecycleRuleInput {

    @JsonProperty("id")
    private String id;

    @JsonProperty("prefix")
    private String prefix;

    @JsonProperty("status")
    private String status; // "Enabled" or "Disabled"

    @JsonProperty("expiration_days")
    private Integer expirationDays;

    @JsonProperty("transition_days")
    private Integer transitionDays;

    @JsonProperty("transition_storage_class")
    private String transitionStorageClass;

    public LifecycleRuleInput() {}

    public LifecycleRuleInput(String id, String prefix, String status,
                              Integer expirationDays, Integer transitionDays,
                              String transitionStorageClass) {
        this.id = id;
        this.prefix = prefix;
        this.status = status;
        this.expirationDays = expirationDays;
        this.transitionDays = transitionDays;
        this.transitionStorageClass = transitionStorageClass;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPrefix() { return prefix; }
    public void setPrefix(String prefix) { this.prefix = prefix; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getExpirationDays() { return expirationDays; }
    public void setExpirationDays(Integer expirationDays) { this.expirationDays = expirationDays; }

    public Integer getTransitionDays() { return transitionDays; }
    public void setTransitionDays(Integer transitionDays) { this.transitionDays = transitionDays; }

    public String getTransitionStorageClass() { return transitionStorageClass; }
    public void setTransitionStorageClass(String transitionStorageClass) { this.transitionStorageClass = transitionStorageClass; }
}
