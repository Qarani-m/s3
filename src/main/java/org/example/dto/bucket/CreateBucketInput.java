package org.example.dto.bucket;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Data
@ToString
@RequiredArgsConstructor
public class CreateBucketInput {
    @JsonProperty("name")
    private String name;
    @JsonProperty("owner_id")
    private String ownerId;

}
