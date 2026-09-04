package com.artvsart.service;

import com.artvsart.model.Artwork;
import com.artvsart.model.GameRun;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StreakDifficultyStrategyTest {

    private final StreakDifficultyPolicy policy =
            new StreakDifficultyPolicy();

    @Test
    void shiftsArtworkYearPairsAfterEachCorrectAnswer() {
        OlderArtworkQuestionStrategy strategy =
                new OlderArtworkQuestionStrategy(
                        new OlderArtworkQuestionService(),
                        policy
                );

        Artwork first = artwork(1L, "First", null, 1700);
        Artwork second = artwork(2L, "Second", null, 2031);
        GameRun run = GameRun.startStreak("player");

        assertFalse(strategy.isEligiblePair(first, second, run));

        run.recordStreakAnswer(true);

        assertTrue(strategy.isEligiblePair(first, second, run));
    }

    @Test
    void leavesWagerDifficultyBandsUnchanged() {
        OlderArtworkQuestionStrategy strategy =
                new OlderArtworkQuestionStrategy(
                        new OlderArtworkQuestionService(),
                        policy
                );

        Artwork first = artwork(1L, "First", null, 1700);
        Artwork second = artwork(2L, "Second", null, 1950);
        GameRun run = GameRun.startWager("player");

        assertTrue(strategy.isEligiblePair(first, second, run));
    }

    @Test
    void appliesTheCurveToArtistBirthYearPairs() {
        ArtistBornEarlierQuestionStrategy strategy =
                new ArtistBornEarlierQuestionStrategy(
                        new ArtistBornEarlierQuestionService(),
                        policy
                );

        Artwork first = artwork(1L, "First", 1800, 1850);
        Artwork second = artwork(2L, "Second", 1880, 1930);
        GameRun run = GameRun.startStreak("player");

        assertFalse(strategy.isEligiblePair(first, second, run));

        run.recordStreakAnswer(true);
        run.recordStreakAnswer(true);

        assertTrue(strategy.isEligiblePair(first, second, run));
    }

    @Test
    void appliesTheCurveToArtistAgePairs() {
        ArtistYoungerAtCreationQuestionStrategy strategy =
                new ArtistYoungerAtCreationQuestionStrategy(
                        new ArtistYoungerAtCreationQuestionService(),
                        policy
                );

        Artwork younger = artwork(1L, "Younger", 1880, 1900);
        Artwork older = artwork(2L, "Older", 1850, 1900);
        GameRun run = GameRun.startStreak("player");

        assertFalse(strategy.isEligiblePair(younger, older, run));

        for (int answer = 0; answer < 4; answer++) {
            run.recordStreakAnswer(true);
        }

        assertTrue(strategy.isEligiblePair(younger, older, run));
    }

    private Artwork artwork(
            Long id,
            String artistName,
            Integer artistBirthYear,
            Integer artworkYear
    ) {
        Artwork artwork = mock(Artwork.class);

        when(artwork.getId()).thenReturn(id);
        when(artwork.getArtistName()).thenReturn(artistName);
        when(artwork.getArtistBeginYear()).thenReturn(artistBirthYear);
        when(artwork.getObjectBeginYear()).thenReturn(artworkYear);

        return artwork;
    }
}
