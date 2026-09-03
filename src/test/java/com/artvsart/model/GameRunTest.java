package com.artvsart.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameRunTest {

    @Test
    void startsStreakRunAtRoundOneWithZeroCorrectAnswers() {
        GameRun run = GameRun.startStreak("voter-1");

        assertEquals(GameMode.STREAK, run.getGameMode());
        assertEquals(1, run.getRoundNumber());
        assertEquals(0, run.getCorrectAnswers());
        assertTrue(run.isActive());
        assertNotNull(run.getStartedAt());
    }

    @Test
    void correctAnswerAdvancesRoundAndStreak() {
        GameRun run = GameRun.startStreak("voter-1");

        run.recordStreakAnswer(true);

        assertEquals(2, run.getRoundNumber());
        assertEquals(1, run.getCorrectAnswers());
        assertTrue(run.isActive());
    }

    @Test
    void incorrectAnswerCompletesRun() {
        GameRun run = GameRun.startStreak("voter-1");

        run.recordStreakAnswer(false);

        assertEquals(1, run.getRoundNumber());
        assertEquals(0, run.getCorrectAnswers());
        assertFalse(run.isActive());
        assertNotNull(run.getCompletedAt());
    }

    @Test
    void completedRunCannotAcceptAnotherAnswer() {
        GameRun run = GameRun.startStreak("voter-1");

        run.recordStreakAnswer(false);

        assertThrows(
                IllegalStateException.class,
                () -> run.recordStreakAnswer(true)
        );
    }
}