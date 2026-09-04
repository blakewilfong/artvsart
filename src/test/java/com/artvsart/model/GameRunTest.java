package com.artvsart.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameRunTest {

    @Test
    void startsStreakRunAtRoundOneWithZeroCorrectAnswers() {
        GameRun run = GameRun.startStreak("voter-1");

        assertEquals(GameMode.STREAK, run.getGameMode());
        assertEquals(1, run.getRoundNumber());
        assertEquals(0, run.getCorrectAnswers());
        assertEquals(3, run.getRerollsRemaining());
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
    void spendsRerollsWithoutAdvancingTheRound() {
        GameRun run = GameRun.startStreak("voter-1");

        run.spendReroll();
        run.spendReroll();
        run.spendReroll();

        assertEquals(0, run.getRerollsRemaining());
        assertEquals(1, run.getRoundNumber());
        assertThrows(
                IllegalStateException.class,
                run::spendReroll
        );
    }

    @Test
    void earnsAnUnlimitedRerollEveryThreeCompletedRounds() {
        GameRun run = GameRun.startStreak("voter-1");

        run.spendReroll();
        run.spendReroll();
        run.spendReroll();

        for (int answer = 0; answer < 9; answer++) {
            run.recordStreakAnswer(true);
        }

        assertEquals(3, run.getRerollsRemaining());
        assertEquals(10, run.getRoundNumber());
    }

    @Test
    void startsWagerRunWithOneHundredPoints() {
        GameRun run = GameRun.startWager("voter-1");

        assertEquals(GameMode.WAGER, run.getGameMode());
        assertEquals(1, run.getRoundNumber());
        assertEquals(100, run.getPointBalance());
        assertEquals(100, run.getHighestPointBalance());
        assertEquals(3, run.getRerollsRemaining());
        assertEquals(5, run.getMinimumWager());
        assertEquals(0, run.getRakePercentage());
        assertEquals(6, run.getNextRakeIncreaseRound());
        assertEquals(5, run.getNextRakePercentage());
        assertTrue(run.isActive());
    }

    @Test
    void correctWagerAddsFullProfitBeforeRakeBegins() {
        GameRun run = GameRun.startWager("voter-1");

        run.recordWagerAnswer(
                true,
                20
        );

        assertEquals(120, run.getPointBalance());
        assertEquals(120, run.getHighestPointBalance());
        assertEquals(1, run.getCorrectAnswers());
        assertEquals(2, run.getRoundNumber());
    }

    @Test
    void incorrectWagerSubtractsEntireWager() {
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
    void minimumWagerCompoundsByTenPercentEachRound() {
        GameRun run = GameRun.startWager("voter-1");

        assertEquals(5, run.getMinimumWager());

        run.recordWagerAnswer(
                true,
                run.getMinimumWager()
        );

        assertEquals(6, run.getMinimumWager());

        run.recordWagerAnswer(
                true,
                run.getMinimumWager()
        );

        assertEquals(7, run.getMinimumWager());

        advanceWithCorrectAnswers(run, 7);

        assertEquals(10, run.getRoundNumber());
        assertEquals(12, run.getMinimumWager());
    }

    @Test
    void minimumWagerCannotExceedCurrentBalance() {
        GameRun run = GameRun.startWager("voter-1");

        advanceWithCorrectAnswers(run, 9);

        int wagerLeavingFivePoints =
                run.getPointBalance() - 5;

        run.recordWagerAnswer(
                false,
                wagerLeavingFivePoints
        );

        assertEquals(5, run.getPointBalance());
        assertEquals(5, run.getMinimumWager());
        assertTrue(run.isActive());
    }

    @Test
    void rakeIncreasesEveryFiveRounds() {
        GameRun run = GameRun.startWager("voter-1");

        assertEquals(0, run.getRakePercentage());

        advanceWithCorrectAnswers(run, 5);
        assertEquals(6, run.getRoundNumber());
        assertEquals(5, run.getRakePercentage());

        advanceWithCorrectAnswers(run, 5);
        assertEquals(11, run.getRoundNumber());
        assertEquals(10, run.getRakePercentage());

        advanceWithCorrectAnswers(run, 5);
        assertEquals(16, run.getRoundNumber());
        assertEquals(15, run.getRakePercentage());

        advanceWithCorrectAnswers(run, 5);
        assertEquals(21, run.getRoundNumber());
        assertEquals(20, run.getRakePercentage());

        advanceWithCorrectAnswers(run, 5);
        assertEquals(26, run.getRoundNumber());
        assertEquals(25, run.getRakePercentage());
    }

    @Test
    void rakeStopsIncreasingAtTwentyFivePercent() {
        GameRun run = GameRun.startWager("voter-1");

        advanceWithCorrectAnswers(run, 30);

        assertEquals(31, run.getRoundNumber());
        assertEquals(25, run.getRakePercentage());
        assertNull(run.getNextRakeIncreaseRound());
        assertNull(run.getNextRakePercentage());
    }

    @Test
    void correctWagerAddsProfitAfterRake() {
        GameRun run = GameRun.startWager("voter-1");

        advanceWithCorrectAnswers(run, 5);

        int balanceBeforeAnswer =
                run.getPointBalance();

        run.recordWagerAnswer(
                true,
                20
        );

        assertEquals(
                balanceBeforeAnswer + 19,
                run.getPointBalance()
        );

        assertEquals(6, run.getCorrectAnswers());
        assertEquals(7, run.getRoundNumber());
    }

    @Test
    void calculatesProfitAfterHouseRake() {
        GameRun run = GameRun.startWager("voter-1");

        assertEquals(
                34,
                run.calculateProfitForRound(
                        40,
                        16
                )
        );
    }

    @Test
    void identifiesRoundBeforeRakeIncrease() {
        GameRun run = GameRun.startWager("voter-1");

        advanceWithCorrectAnswers(run, 4);

        assertEquals(5, run.getRoundNumber());
        assertTrue(run.isRakeIncreaseNextRound());
        assertEquals(6, run.getNextRakeIncreaseRound());
        assertEquals(5, run.getNextRakePercentage());
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

    @Test
    void abandoningARunCompletesItWithoutChangingTheScore() {
        GameRun run = GameRun.startStreak("voter-1");
        run.recordStreakAnswer(true);

        run.abandon();

        assertFalse(run.isActive());
        assertEquals(1, run.getCorrectAnswers());
        assertNotNull(run.getCompletedAt());
    }

    @Test
    void wagerRunEarnsRerollAfterThirdCompletedRound() {
        GameRun run = GameRun.startWager("voter-1");

        run.recordWagerAnswer(true, 5);
        run.recordWagerAnswer(true, 6);
        run.recordWagerAnswer(false, 7);

        assertEquals(4, run.getRerollsRemaining());
        assertEquals(4, run.getRoundNumber());
    }

    @Test
    void completedRunCannotSpendReroll() {
        GameRun run = GameRun.startWager("voter-1");
        run.recordWagerAnswer(false, 100);

        assertThrows(
                IllegalStateException.class,
                run::spendReroll
        );
    }

    private void advanceWithCorrectAnswers(
            GameRun run,
            int numberOfAnswers
    ) {
        for (int answer = 0;
             answer < numberOfAnswers;
             answer++) {
            run.recordWagerAnswer(
                    true,
                    run.getMinimumWager()
            );
        }
    }
}
