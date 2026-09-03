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
    void correctStreakAnswerAdvancesRoundAndScore() {
        GameRun run = GameRun.startStreak("voter-1");

        run.recordStreakAnswer(true);

        assertEquals(2, run.getRoundNumber());
        assertEquals(1, run.getCorrectAnswers());
        assertTrue(run.isActive());
    }

    @Test
    void incorrectStreakAnswerCompletesRun() {
        GameRun run = GameRun.startStreak("voter-1");

        run.recordStreakAnswer(false);

        assertEquals(1, run.getRoundNumber());
        assertEquals(0, run.getCorrectAnswers());
        assertFalse(run.isActive());
        assertNotNull(run.getCompletedAt());
    }

    @Test
    void completedStreakRunCannotAcceptAnotherAnswer() {
        GameRun run = GameRun.startStreak("voter-1");

        run.recordStreakAnswer(false);

        assertThrows(
                IllegalStateException.class,
                () -> run.recordStreakAnswer(true)
        );
    }

    @Test
    void startsWagerRunWithOneHundredPoints() {
        GameRun run = GameRun.startWager("voter-1");

        assertEquals(GameMode.WAGER, run.getGameMode());
        assertEquals(1, run.getRoundNumber());
        assertEquals(100, run.getPointBalance());
        assertEquals(100, run.getHighestPointBalance());
        assertEquals(5, run.getMinimumWager());
        assertTrue(run.isActive());
    }

    @Test
    void correctWagerAddsPoints() {
        GameRun run = GameRun.startWager("voter-1");

        run.recordWagerAnswer(
                true,
                20
        );

        assertEquals(120, run.getPointBalance());
        assertEquals(120, run.getHighestPointBalance());
        assertEquals(1, run.getCorrectAnswers());
        assertEquals(2, run.getRoundNumber());
        assertEquals(10, run.getMinimumWager());
    }

    @Test
    void incorrectWagerSubtractsPoints() {
        GameRun run = GameRun.startWager("voter-1");

        run.recordWagerAnswer(
                false,
                20
        );

        assertEquals(80, run.getPointBalance());
        assertEquals(100, run.getHighestPointBalance());
        assertEquals(0, run.getCorrectAnswers());
        assertEquals(2, run.getRoundNumber());
        assertTrue(run.isActive());
    }

    @Test
    void minimumWagerIncreasesEachRound() {
        GameRun run = GameRun.startWager("voter-1");

        assertEquals(5, run.getMinimumWager());

        run.recordWagerAnswer(
                true,
                5
        );

        assertEquals(10, run.getMinimumWager());

        run.recordWagerAnswer(
                true,
                10
        );

        assertEquals(15, run.getMinimumWager());
    }

    @Test
    void rejectsWagerBelowMinimum() {
        GameRun run = GameRun.startWager("voter-1");

        assertThrows(
                IllegalArgumentException.class,
                () -> run.recordWagerAnswer(
                        true,
                        4
                )
        );
    }

    @Test
    void rejectsWagerAboveBalance() {
        GameRun run = GameRun.startWager("voter-1");

        assertThrows(
                IllegalArgumentException.class,
                () -> run.recordWagerAnswer(
                        true,
                        101
                )
        );
    }

    @Test
    void losingEntireBalanceCompletesWagerRun() {
        GameRun run = GameRun.startWager("voter-1");

        run.recordWagerAnswer(
                false,
                100
        );

        assertEquals(0, run.getPointBalance());
        assertFalse(run.isActive());
        assertNotNull(run.getCompletedAt());
    }
}