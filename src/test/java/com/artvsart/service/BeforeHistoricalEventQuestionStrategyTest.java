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
            new BeforeHistoricalEventQuestionStrategy(
                    new StreakDifficultyPolicy()
            );

    @Test
    void selectsAnEventBetweenTheArtworkDatesAndChoosesTheCloserWork() {
        Artwork older = artwork("1", 1600);
        Artwork newer = artwork("2", 1800);

        assertTrue(strategy.isEligiblePair(older, newer, 1));
        assertValidSelection(older, newer, 1);
    }

    @Test
    void canChooseTheNewerArtworkWhenItIsCloserToTheEvent() {
        Artwork older = artwork("1", 1600);
        Artwork newer = artwork("2", 1810);

        assertTrue(strategy.isEligiblePair(older, newer, 1));
        assertValidSelection(older, newer, 1);
    }

    @Test
    void skipsAnEventWhenTheArtworksAreEquallyClose() {
        Artwork older = artwork("1", 1200);
        Artwork newer = artwork("2", 1316);

        assertTrue(strategy.isEligiblePair(older, newer, 20));
        assertValidSelection(older, newer, 20);
    }

    @Test
    void rejectsNearlyEqualDistancesFromTheOnlyEvent() {
        Artwork older = artwork("1", 1753);
        Artwork newer = artwork("2", 1758);

        assertFalse(strategy.isEligiblePair(older, newer, 100));
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

    @Test
    void selectsVariedEventsWithoutBreakingDifficultyOrAnswers() {
        Set<String> selected = new java.util.HashSet<>();
        for (int round : new int[]{1, 10, 11, 20, 21, 40}) {
            for (int i = 0; i < 100; i++) {
                Artwork first = artwork("left-" + i, 1500);
                Artwork second = artwork("right-" + i, 1900);
                assertTrue(strategy.isEligiblePair(first, second, round));
                assertValidSelection(first, second, round);
                selected.add(strategy.getQuestionParameter(first, second, round));
            }
        }
        assertTrue(selected.size() > 10);
    }

    private void assertValidSelection(Artwork first, Artwork second, int round) {
        HistoricalEvent event = HistoricalEvent.valueOf(
                strategy.getQuestionParameter(first, second, round));
        long firstDistance = Math.abs((long) first.findSingleCreationYear().orElseThrow() - event.getYear());
        long secondDistance = Math.abs((long) second.findSingleCreationYear().orElseThrow() - event.getYear());
        assertTrue(new StreakDifficultyPolicy().isHistoricalEventDistanceEligible(
                Math.min(firstDistance, secondDistance), Math.max(firstDistance, secondDistance), round));
        assertSame(firstDistance < secondDistance ? first : second,
                strategy.getCorrectArtwork(first, second, round));
        assertEquals(event.name(), strategy.getQuestionParameter(second, first, round));
        assertSame(strategy.getCorrectArtwork(first, second, round),
                strategy.getCorrectArtwork(second, first, round));
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
