package com.artvsart.integration.cma;

import com.artvsart.service.ArtworkGenreClassifier;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CmaArtworkEligibilityPolicyTest {

    private final CmaArtworkEligibilityPolicy policy =
            new CmaArtworkEligibilityPolicy(
                    new ArtworkGenreClassifier()
            );

    @Test
    void acceptsCc0PaintingWithArtistDateAndImage() {
        assertTrue(policy.isEligible(
                artwork("CC0", "Painting", "object", true),
                1850
        ));
    }

    @Test
    void rejectsCopyrightedAndNonPaintingRecords() {
        assertFalse(policy.isEligible(
                artwork("Copyrighted", "Painting", "object", true),
                1850
        ));
        assertFalse(policy.isEligible(
                artwork("CC0", "Sculpture", "object", true),
                1850
        ));
    }

    @Test
    void rejectsComponentsAndPaintingsWithoutArtists() {
        assertFalse(policy.isEligible(
                artwork("CC0", "Painting", "component", true),
                1850
        ));
        assertFalse(policy.isEligible(
                artwork("CC0", "Painting", "object", false),
                1850
        ));
    }

    @Test
    void rejectsSketchesListedAsPaintings() {
        CmaArtworkResponse artwork = artwork(
                "CC0",
                "Painting",
                "object",
                true,
                "Sketch for a mural"
        );

        assertFalse(policy.isEligible(artwork, 1850));
    }

    private CmaArtworkResponse artwork(
            String license,
            String type,
            String recordType,
            boolean includeArtist
    ) {
        return artwork(
                license,
                type,
                recordType,
                includeArtist,
                "Modern painting"
        );
    }

    private CmaArtworkResponse artwork(
            String license,
            String type,
            String recordType,
            boolean includeArtist,
            String title
    ) {
        List<CmaArtworkResponse.Creator> creators = includeArtist
                ? List.of(new CmaArtworkResponse.Creator(
                1L,
                "Jane Artist (American, 1870-1940)",
                null,
                "artist",
                "1870",
                "1940"
        ))
                : List.of();

        return new CmaArtworkResponse(
                10L,
                license,
                title,
                "1900",
                1900,
                1900,
                creators,
                List.of("America"),
                "oil on canvas",
                "American Painting and Sculpture",
                type,
                "https://clevelandart.org/art/10",
                new CmaArtworkResponse.Images(
                        new CmaArtworkResponse.Image(
                                "https://example.test/web.jpg"
                        ),
                        null
                ),
                null,
                recordType
        );
    }
}
