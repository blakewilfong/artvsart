package com.artvsart.integration.met;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MetSearchResponse(

        int total,

        @JsonProperty("objectIDs")
        List<Long> objectIds
) {

    public MetSearchResponse {
        objectIds = objectIds == null
                ? List.of()
                : List.copyOf(objectIds);
    }
}