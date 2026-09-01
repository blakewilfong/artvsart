package com.artvsart.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ArtworkStatsTest {

    @Test
    void artworkWithNoHistoryStartsAtFiftyPercent() {
        ArtworkStats stats = new ArtworkStats(0, 0);

        assertEquals(
                50.0,
                stats.selectionPercentage()
        );
    }

    @Test
    void selectionIncreasesSelectionsAndPresentations() {
        ArtworkStats stats = new ArtworkStats(4, 10);

        ArtworkStats updated = stats.afterSelection();

        assertEquals(5, updated.selections());
        assertEquals(11, updated.presentations());
        assertEquals(
                45.45,
                updated.selectionPercentage(),
                0.01
        );
    }

    @Test
    void nonSelectionOnlyIncreasesPresentations() {
        ArtworkStats stats = new ArtworkStats(4, 10);

        ArtworkStats updated = stats.afterNonSelection();

        assertEquals(4, updated.selections());
        assertEquals(11, updated.presentations());
        assertEquals(
                36.36,
                updated.selectionPercentage(),
                0.01
        );
    }
}