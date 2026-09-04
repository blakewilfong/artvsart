package com.artvsart.integration.met;

import com.artvsart.service.ArtworkGenreClassifier;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MetArtworkEligibilityPolicyTest {

    private final MetArtworkEligibilityPolicy policy =
            new MetArtworkEligibilityPolicy(
                    new ArtworkGenreClassifier()
            );

    @Test
    void acceptsPaintingClassification() {
        MetArtworkResponse artwork = createArtwork(
                "Painting",
                "Paintings",
                "Oil on canvas"
        );

        assertTrue(policy.isEligible(artwork));
    }

    @Test
    void acceptsDrawingClassification() {
        MetArtworkResponse artwork = createArtwork(
                "Drawing",
                "Drawings",
                "Graphite on paper"
        );

        assertTrue(policy.isEligible(artwork));
    }

    @Test
    void acceptsAsianHangingScroll() {
        MetArtworkResponse artwork = createArtwork(
                "Hanging scroll",
                null,
                "Ink and color on silk"
        );

        assertTrue(policy.isEligible(artwork));
    }

    @Test
    void rejectsSculptureWithPaintedSurface() {
        MetArtworkResponse artwork = createArtwork(
                "Sculpture",
                "Sculpture",
                "Painted wood"
        );

        assertFalse(policy.isEligible(artwork));
    }

    @Test
    void rejectsPhotographicPrint() {
        MetArtworkResponse artwork = createArtwork(
                "Photograph",
                "Photographic Prints",
                "Gelatin silver print"
        );

        assertFalse(policy.isEligible(artwork));
    }

    @Test
    void rejectsSketchesEvenWhenTheyAreDrawings() {
        MetArtworkResponse artwork = createArtwork(
                "Sketch",
                "Drawings",
                "Graphite on paper"
        );

        assertFalse(policy.isEligible(artwork));
    }

    @Test
    void rejectsArtworkWithoutRequiredApiData() {
        MetArtworkResponse artwork =
                new MetArtworkResponse(
                        1L,
                        null,
                        true,
                        null,
                        "https://example.com/original.jpg",
                        null,
                        "European Paintings",
                        "Painting",
                        "Example Painting",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        "1900",
                        1900,
                        1900,
                        "Oil on canvas",
                        null,
                        null,
                        "Paintings",
                        null,
                        "https://example.com/artwork",
                        null,
                        List.of()
                );

        assertFalse(policy.isEligible(artwork));
    }

    private MetArtworkResponse createArtwork(
            String objectName,
            String classification,
            String medium
    ) {
        return new MetArtworkResponse(
                1L,
                null,
                true,
                null,
                "https://example.com/original.jpg",
                "https://example.com/small.jpg",
                "Example Department",
                objectName,
                "Example Artwork",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "1900",
                1900,
                1900,
                medium,
                null,
                null,
                classification,
                null,
                "https://example.com/artwork",
                null,
                List.of()
        );
    }
}
