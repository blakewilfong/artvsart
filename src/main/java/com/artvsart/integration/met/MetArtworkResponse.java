package com.artvsart.integration.met;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MetArtworkResponse(

        @JsonProperty("objectID")
        Long objectId,

        @JsonProperty("isPublicDomain")
        boolean publicDomain,

        String primaryImageSmall,

        String title,

        String artistDisplayName,

        String objectDate,

        @JsonProperty("objectURL")
        String objectUrl
) {

    public boolean isUsable() {
        return objectId != null
                && publicDomain
                && title != null
                && !title.isBlank()
                && primaryImageSmall != null
                && !primaryImageSmall.isBlank();
    }
}