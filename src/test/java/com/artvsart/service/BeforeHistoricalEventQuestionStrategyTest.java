package com.artvsart.service;

import com.artvsart.model.Artwork;
import com.artvsart.model.ArtworkMetadata;
import com.artvsart.model.HistoricalEvent;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BeforeHistoricalEventQuestionStrategyTest {

    private final BeforeHistoricalEventQuestionStrategy strategy =
            new BeforeHistoricalEventQuestionStrategy();

    @Test
    void selectsAnEventBetweenTheArtworkDates() {
        Artwork older = artwork("1", 1700);
        Artwork newer = artwork("2", 1800);

        assertTrue(strategy.isEligiblePair(older, newer, 1));
        assertEquals(
                "SEVEN_YEARS_WAR_BEGAN",
                strategy.getQuestionParameter(older, newer, 1)
        );
        assertSame(older, strategy.getCorrectArtwork(older, newer));
    }

    @Test
    void rejectsMatchingArtworkDates() {
        Artwork first = artwork("1", 1800);
        Artwork second = artwork("2", 1800);

        assertFalse(strategy.isEligiblePair(first, second, 1));
    }

    @Test
    void coversEveryArtworkDecadeInTheCurrentDatabaseRange() {
        Set<Integer> eventDecades = Arrays.stream(
                        HistoricalEvent.values()
                )
                .map(event -> decadeOf(event.getYear()))
                .collect(Collectors.toSet());

        IntStream.iterate(
                        1080,
                        decade -> decade <= 1920,
                        decade -> decade + 10
                )
                .forEach(decade -> assertTrue(
                        eventDecades.contains(decade),
                        () -> "Missing historical event for the "
                                + decade + "s"
                ));
    }

    private int decadeOf(int year) {
        return Math.floorDiv(year, 10) * 10;
    }

    private Artwork artwork(String id, int year) {
        Artwork artwork = new Artwork(
                "test", id, "Title", "Artist",
                Integer.toString(year), "image.jpg"
        );
        artwork.updateMetadata(new ArtworkMetadata(
                null, null, null, null, null,
                year, year, null, null, "Oil"
        ));
        return artwork;
    }
}
