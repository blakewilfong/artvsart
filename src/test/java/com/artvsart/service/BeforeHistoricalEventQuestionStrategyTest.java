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
    void selectsAnEventBetweenTheArtworkDatesAndChoosesTheCloserWork() {
        Artwork older = artwork("1", 1700);
        Artwork newer = artwork("2", 1800);

        assertTrue(strategy.isEligiblePair(older, newer, 1));
        assertEquals(
                "SEVEN_YEARS_WAR_BEGAN",
                strategy.getQuestionParameter(older, newer, 1)
        );
        assertSame(newer, strategy.getCorrectArtwork(older, newer));
    }

    @Test
    void canChooseTheOlderArtworkWhenItIsCloserToTheEvent() {
        Artwork older = artwork("1", 1700);
        Artwork newer = artwork("2", 1770);

        assertTrue(strategy.isEligiblePair(older, newer, 1));
        assertEquals(
                "FLYING_SHUTTLE_PATENTED",
                strategy.getQuestionParameter(older, newer, 1)
        );
        assertSame(older, strategy.getCorrectArtwork(older, newer));
    }

    @Test
    void skipsAnEventWhenTheArtworksAreEquallyClose() {
        Artwork older = artwork("1", 1200);
        Artwork newer = artwork("2", 1316);

        assertTrue(strategy.isEligiblePair(older, newer, 1));
        assertEquals(
                "BATTLE_OF_AIN_JALUT",
                strategy.getQuestionParameter(older, newer, 1)
        );
        assertSame(newer, strategy.getCorrectArtwork(older, newer));
    }

    @Test
    void rejectsMatchingArtworkDates() {
        Artwork first = artwork("1", 1800);
        Artwork second = artwork("2", 1800);

        assertFalse(strategy.isEligiblePair(first, second, 1));
    }

    @Test
    void rejectsPairWhenEitherArtworkDateIsMissing() {
        Artwork dated = artwork("1", 1800);
        Artwork missing = artwork("2", null);

        assertFalse(strategy.isEligiblePair(dated, missing, 1));
    }

    @Test
    void rejectsArtworkDateRanges() {
        Artwork exact = artwork("1", 1855);
        Artwork range = artwork("2", 1800, 1900);

        assertFalse(strategy.isEligiblePair(exact, range, 1));
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

    private Artwork artwork(String id, Integer year) {
        return artwork(id, year, year);
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
