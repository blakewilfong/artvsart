package com.artvsart.integration.cma;

import org.springframework.stereotype.Component;

@Component
public class CmaArtworkEligibilityPolicy {

    public boolean isEligible(
            CmaArtworkResponse artwork,
            int createdAfterYear
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
                || artwork.creationDateEarliest() <= createdAfterYear
                || !hasText(artwork.webImageUrl())
                || !hasText(artwork.url())
                || artwork.primaryArtist().isEmpty()) {
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
