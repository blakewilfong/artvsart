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
                && hasText(title)
                && hasText(artistDisplayName)
                && hasText(objectDate)
                && hasText(primaryImageSmall)
                && hasText(objectUrl);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}