package org.example.dto.bucket;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;


@Data
@ToString
@RequiredArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LifecycleInput {

    @JsonProperty("rules")
    private List<LifecycleRuleInput> rules;

    public LifecycleInput(List<LifecycleRuleInput> rule) {
        this.rules=rule;
    }
}
