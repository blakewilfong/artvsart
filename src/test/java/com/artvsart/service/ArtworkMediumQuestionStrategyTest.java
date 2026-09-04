package com.artvsart.service;

import com.artvsart.model.Artwork;
import com.artvsart.model.ArtworkMetadata;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArtworkMediumQuestionStrategyTest {

    private final ArtworkMediumQuestionStrategy strategy =
            new ArtworkMediumQuestionStrategy();

    @Test
    void selectsAnExclusiveNormalizedMedium() {
        Artwork oil = artwork("Oil on canvas", 1870);
        Artwork tempera = artwork("Tempera on panel", 1880);

        assertTrue(strategy.isEligiblePair(oil, tempera, 1));
        assertEquals(
                "OIL",
                strategy.getQuestionParameter(oil, tempera, 1)
        );
        assertSame(oil, strategy.getCorrectArtwork(oil, tempera));
    }

    @Test
    void rejectsPairWithoutAnExclusiveSupportedMedium() {
        Artwork first = artwork("Oil on canvas", 1870);
        Artwork second = artwork("Oil on wood", 1880);

        assertFalse(strategy.isEligiblePair(first, second, 1));
    }

    @Test
    void rejectsPairWhenEitherMediumIsMissing() {
        Artwork oil = artwork("Oil on canvas", 1870);
        Artwork missing = artwork(null, 1880);

        assertFalse(strategy.isEligiblePair(oil, missing, 1));
    }

    private Artwork artwork(String medium, int year) {
        Artwork artwork = new Artwork(
                "test", Integer.toString(year), "Title", "Artist",
                Integer.toString(year), "image.jpg"
        );
        artwork.updateMetadata(new ArtworkMetadata(
                null, null, null, null, null,
                year, year, null, null, medium
        ));
        return artwork;
    }
}
