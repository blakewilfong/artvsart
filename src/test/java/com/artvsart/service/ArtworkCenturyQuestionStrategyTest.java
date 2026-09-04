package com.artvsart.service;

import com.artvsart.model.Artwork;
import com.artvsart.model.ArtworkMetadata;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArtworkCenturyQuestionStrategyTest {

    private final ArtworkCenturyQuestionStrategy strategy =
            new ArtworkCenturyQuestionStrategy(
                    new StreakDifficultyPolicy()
            );

    @Test
    void asksForTheCenturyOfOneArtworkWithoutFavoringPosition() {
        Artwork eighteenthCentury = artwork("1", 1750, 1750);
        Artwork nineteenthCentury = artwork("2", 1850, 1850);

        assertTrue(strategy.isEligiblePair(
                eighteenthCentury,
                nineteenthCentury,
                1
        ));

        Artwork correct = strategy.getCorrectArtwork(
                eighteenthCentury,
                nineteenthCentury
        );
        String parameter = strategy.getQuestionParameter(
                eighteenthCentury,
                nineteenthCentury,
                1
        );

        assertEquals(
                correct == eighteenthCentury
                        ? "18th century"
                        : "19th century",
                parameter
        );
        assertSame(
                correct,
                strategy.getCorrectArtwork(
                        nineteenthCentury,
                        eighteenthCentury
                )
        );
    }

    @Test
    void formatsBceCenturies() {
        Artwork bce = artwork("1", -550, -550);
        Artwork ce = artwork("2", 150, 150);
        Artwork correct = strategy.getCorrectArtwork(bce, ce);

        assertEquals(
                correct == bce
                        ? "6th century BCE"
                        : "2nd century",
                strategy.getQuestionParameter(bce, ce, 1)
        );
    }

    @Test
    void rejectsMissingAmbiguousOrMatchingCenturies() {
        Artwork known = artwork("1", 1850, 1850);
        Artwork missingEnd = artwork("2", 1750, null);
        Artwork crossesBoundary = artwork("3", 1890, 1910);
        Artwork sameCentury = artwork("4", 1870, 1890);
        Artwork yearZero = artwork("5", 0, 0);

        assertFalse(strategy.isEligiblePair(known, missingEnd, 1));
        assertFalse(strategy.isEligiblePair(
                known,
                crossesBoundary,
                1
        ));
        assertFalse(strategy.isEligiblePair(known, sameCentury, 1));
        assertFalse(strategy.isEligiblePair(known, yearZero, 1));
    }

    private Artwork artwork(
            String id,
            Integer beginYear,
            Integer endYear
    ) {
        Artwork artwork = new Artwork(
                "test", id, "Title", "Artist",
                beginYear == null
                        ? "Unknown"
                        : Integer.toString(beginYear),
                "image.jpg"
        );
        artwork.updateMetadata(new ArtworkMetadata(
                null, null, null, null, null,
                beginYear, endYear, null, null, "Oil"
        ));
        return artwork;
    }
}
