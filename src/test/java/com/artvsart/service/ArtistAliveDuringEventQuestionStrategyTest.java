package com.artvsart.service;

import com.artvsart.model.Artwork;
import com.artvsart.model.ArtworkMetadata;
import com.artvsart.model.HistoricalEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArtistAliveDuringEventQuestionStrategyTest {

    private final ArtistAliveDuringEventQuestionStrategy strategy =
            new ArtistAliveDuringEventQuestionStrategy();

    @Test
    void selectsAnEventWhenExactlyOneArtistWasAlive() {
        Artwork earlierArtist = artwork("1", 1700, 1800);
        Artwork laterArtist = artwork("2", 1900, 1980);

        assertTrue(strategy.isEligiblePair(
                earlierArtist,
                laterArtist,
                1
        ));

        HistoricalEvent event = HistoricalEvent.valueOf(
                strategy.getQuestionParameter(
                        earlierArtist,
                        laterArtist,
                        1
                )
        );
        Artwork correct = strategy.getCorrectArtwork(
                earlierArtist,
                laterArtist
        );

        assertTrue(wasAlive(correct, event.getYear()));
        assertFalse(wasAlive(
                correct == earlierArtist
                        ? laterArtist
                        : earlierArtist,
                event.getYear()
        ));
    }

    @Test
    void producesTheSameQuestionWhenArtworkOrderChanges() {
        Artwork earlierArtist = artwork("1", 1700, 1800);
        Artwork laterArtist = artwork("2", 1900, 1980);

        assertSame(
                strategy.getCorrectArtwork(
                        earlierArtist,
                        laterArtist
                ),
                strategy.getCorrectArtwork(
                        laterArtist,
                        earlierArtist
                )
        );
        assertEquals(
                strategy.getQuestionParameter(
                        earlierArtist,
                        laterArtist,
                        1
                ),
                strategy.getQuestionParameter(
                        laterArtist,
                        earlierArtist,
                        1
                )
        );
    }

    @Test
    void rejectsMissingInvalidOrIndistinguishableLifespans() {
        Artwork complete = artwork("1", 1700, 1800);
        Artwork missingDeath = artwork("2", 1900, null);
        Artwork invalid = artwork("3", 1900, 1800);
        Artwork sameLifespan = artwork("4", 1700, 1800);
        Artwork placeholderYears = artwork("5", 0, 0);

        assertFalse(strategy.isEligiblePair(
                complete,
                missingDeath,
                1
        ));
        assertFalse(strategy.isEligiblePair(complete, invalid, 1));
        assertFalse(strategy.isEligiblePair(
                complete,
                sameLifespan,
                1
        ));
        assertFalse(strategy.isEligiblePair(
                complete,
                placeholderYears,
                1
        ));
    }

    private boolean wasAlive(Artwork artwork, int year) {
        return artwork.getArtistBeginYear() <= year
                && year <= artwork.getArtistEndYear();
    }

    private Artwork artwork(
            String id,
            Integer birthYear,
            Integer deathYear
    ) {
        Artwork artwork = new Artwork(
                "test", id, "Title", "Artist",
                "1750", "image.jpg"
        );
        artwork.updateMetadata(new ArtworkMetadata(
                null, null, null, birthYear, deathYear,
                1750, 1750, null, null, "Oil"
        ));
        return artwork;
    }
}
