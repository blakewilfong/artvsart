package com.artvsart.service;

import com.artvsart.model.Artwork;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArtworkTitleWordQuestionStrategyTest {

    private final ArtworkTitleWordQuestionStrategy strategy =
            new ArtworkTitleWordQuestionStrategy();

    @Test
    void selectsADistinctiveWordFromOneTitle() {
        Artwork footbridge = artwork(
                "1",
                "The Japanese Footbridge"
        );
        Artwork balance = artwork(
                "2",
                "A Woman Holding a Balance"
        );

        assertTrue(strategy.isEligiblePair(footbridge, balance, 1));
        assertEquals(
                "Footbridge",
                strategy.getQuestionParameter(
                        footbridge,
                        balance,
                        1
                )
        );
        assertSame(
                footbridge,
                strategy.getCorrectArtwork(footbridge, balance)
        );
    }

    @Test
    void ignoresCommonAndSharedTitleWords() {
        Artwork blue = artwork(
                "1",
                "The Blue Landscape with Trees"
        );
        Artwork red = artwork(
                "2",
                "A Red Landscape with Trees"
        );

        assertTrue(strategy.isEligiblePair(blue, red, 1));
        assertEquals(
                "Blue",
                strategy.getQuestionParameter(blue, red, 1)
        );
        assertSame(blue, strategy.getCorrectArtwork(blue, red));
    }

    @Test
    void rejectsTitlesWithoutAnExclusiveMeaningfulWord() {
        Artwork untitled = artwork("1", "Untitled Study");
        Artwork placeholder = artwork("2", "The Unknown Title");
        Artwork firstLandscape = artwork(
                "3",
                "Landscape with Trees"
        );
        Artwork secondLandscape = artwork(
                "4",
                "Landscape with Trees"
        );

        assertFalse(strategy.isEligiblePair(
                untitled,
                placeholder,
                1
        ));
        assertFalse(strategy.isEligiblePair(
                firstLandscape,
                secondLandscape,
                1
        ));
    }

    private Artwork artwork(String id, String title) {
        return new Artwork(
                "test",
                id,
                title,
                "Artist " + id,
                "1900",
                "image.jpg"
        );
    }
}
