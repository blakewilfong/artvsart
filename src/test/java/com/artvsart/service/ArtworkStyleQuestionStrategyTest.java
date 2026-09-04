package com.artvsart.service;

import com.artvsart.model.Artwork;
import com.artvsart.model.ArtworkStyleType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArtworkStyleQuestionStrategyTest {

    private final ArtworkStyleQuestionStrategy strategy =
            new ArtworkStyleQuestionStrategy();

    @Test
    void selectsAnExclusiveStyle() {
        Artwork impressionist = artwork("1", "Impressionism");
        Artwork realist = artwork("2", "Realism");

        assertTrue(strategy.isEligiblePair(impressionist, realist, 1));
        assertEquals(
                "Impressionism",
                strategy.getQuestionParameter(
                        impressionist,
                        realist,
                        1
                )
        );
        assertSame(
                impressionist,
                strategy.getCorrectArtwork(impressionist, realist)
        );
    }

    @Test
    void rejectsPairWithTheSameStyle() {
        Artwork first = artwork("1", "Impressionism");
        Artwork second = artwork("2", "impressionism");

        assertFalse(strategy.isEligiblePair(first, second, 1));
    }

    @Test
    void rejectsPairWhenEitherArtworkHasNoRecordedStyle() {
        Artwork impressionist = artwork("1", "Impressionism");
        Artwork missing = new Artwork(
                "nga",
                "2",
                "Title",
                "Artist",
                "1870",
                "image.jpg"
        );

        assertFalse(strategy.isEligiblePair(
                impressionist,
                missing,
                1
        ));
    }

    private Artwork artwork(String id, String style) {
        Artwork artwork = new Artwork(
                "nga", id, "Title", "Artist", "1870", "image.jpg"
        );
        artwork.replaceStyles(List.of(new Artwork.StyleDefinition(
                ArtworkStyleType.STYLE,
                style,
                "nga"
        )));
        return artwork;
    }
}
