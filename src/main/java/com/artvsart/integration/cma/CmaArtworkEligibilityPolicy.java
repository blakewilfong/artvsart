package com.artvsart.integration.cma;

import com.artvsart.service.ArtworkGenreClassifier;
import org.springframework.stereotype.Component;

@Component
public class CmaArtworkEligibilityPolicy {

    private final ArtworkGenreClassifier genreClassifier;

    public CmaArtworkEligibilityPolicy(
            ArtworkGenreClassifier genreClassifier
    ) {
        this.genreClassifier = genreClassifier;
    }

    public boolean isEligible(
            CmaArtworkResponse artwork,
            Integer createdAfterYear
    ) {
        if (artwork == null
                || artwork.id() == null
                || !"CC0".equalsIgnoreCase(
                artwork.shareLicenseStatus()
        )
                || !"Painting".equalsIgnoreCase(artwork.type())
                || !hasText(artwork.title())
                || !hasText(artwork.creationDate())
                || artwork.creationDateEarliest() == null
                || (createdAfterYear != null
                && artwork.creationDateEarliest() <= createdAfterYear)
                || !hasText(artwork.webImageUrl())
                || !hasText(artwork.url())
                || artwork.primaryArtist().isEmpty()
                || genreClassifier.isSketch(
                artwork.title(),
                artwork.technique(),
                artwork.description()
        )) {
            return false;
        }

        return artwork.recordType() == null
                || (!"part".equalsIgnoreCase(artwork.recordType())
                && !"component".equalsIgnoreCase(
                artwork.recordType()
        ));
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
