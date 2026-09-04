package com.artvsart.integration.met;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MetArtworkResponse(

        @JsonProperty("objectID")
        Long objectId,

        @JsonProperty("isHighlight")
        Boolean highlight,

        @JsonProperty("isPublicDomain")
        Boolean publicDomain,

        String accessionYear,

        String primaryImage,

        String primaryImageSmall,

        String department,

        String objectName,

        String title,

        String culture,

        String period,

        String artistDisplayName,

        String artistDisplayBio,

        String artistNationality,

        String artistBeginDate,

        String artistEndDate,

        String objectDate,

        Integer objectBeginDate,

        Integer objectEndDate,

        String medium,

        String dimensions,

        String country,

        String classification,

        String creditLine,

        @JsonProperty("objectURL")
        String objectUrl,

        @JsonProperty("objectWikidata_URL")
        String objectWikidataUrl,

        List<Tag> tags
) {

    public boolean isUsable() {
        return objectId != null
                && Boolean.TRUE.equals(publicDomain)
                && hasText(title)
                && hasText(objectDate)
                && objectBeginDate != null
                && objectBeginDate != 0
                && hasText(primaryImageSmall)
                && hasText(objectUrl);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Tag(String term) {
    }
}
