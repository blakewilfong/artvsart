package com.artvsart.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "game_runs")
public class GameRun {

    public static final int STARTING_POINTS = 100;
    public static final int BASE_MINIMUM_WAGER = 5;
    public static final int WAGER_INCREASE_PER_ROUND = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GameMode gameMode;

    @Column(name = "voter_id", nullable = false, length = 36)
    private String voterId;

    @Column(nullable = false)
    private int roundNumber;

    @Column(nullable = false)
    private int correctAnswers;

    private Integer pointBalance;

    private Integer highestPointBalance;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false, updatable = false)
    private Instant startedAt;

    private Instant completedAt;

    protected GameRun() {
    }

    private GameRun(
            GameMode gameMode,
            String voterId,
            Integer pointBalance
    ) {
        if (gameMode == null) {
            throw new IllegalArgumentException(
                    "A game mode is required"
            );
        }

        if (voterId == null || voterId.isBlank()) {
            throw new IllegalArgumentException(
                    "A voter ID is required"
            );
        }

        this.gameMode = gameMode;
        this.voterId = voterId;
        this.roundNumber = 1;
        this.correctAnswers = 0;
        this.pointBalance = pointBalance;
        this.highestPointBalance = pointBalance;
        this.active = true;
        this.startedAt = Instant.now();
    }

    public static GameRun startStreak(String voterId) {
        return new GameRun(
                GameMode.STREAK,
                voterId,
                null
        );
    }

    public static GameRun startWager(String voterId) {
        return new GameRun(
                GameMode.WAGER,
                voterId,
                STARTING_POINTS
        );
    }

    public void recordStreakAnswer(boolean correct) {
        requireActiveMode(GameMode.STREAK);

        if (!correct) {
            completeRun();
            return;
        }

        correctAnswers++;
        roundNumber++;
    }

    public void recordWagerAnswer(
            boolean correct,
            int wager
    ) {
        requireActiveMode(GameMode.WAGER);
        validateWager(wager);

        if (correct) {
            pointBalance += wager;
            correctAnswers++;
        } else {
            pointBalance -= wager;
        }

        highestPointBalance = Math.max(
                highestPointBalance,
                pointBalance
        );

        if (pointBalance == 0) {
            completeRun();
            return;
        }

        roundNumber++;
    }

    public int getMinimumWager() {
        requireMode(GameMode.WAGER);

        long calculatedMinimum =
                (long) BASE_MINIMUM_WAGER
                        + (long) (roundNumber - 1)
                        * WAGER_INCREASE_PER_ROUND;

        return (int) Math.min(
                pointBalance,
                calculatedMinimum
        );
    }

    private void validateWager(int wager) {
        int minimumWager = getMinimumWager();

        if (wager < minimumWager) {
            throw new IllegalArgumentException(
                    "Wager must be at least "
                            + minimumWager
            );
        }

        if (wager > pointBalance) {
            throw new IllegalArgumentException(
                    "Wager cannot exceed the current balance"
            );
        }
    }

    private void requireActiveMode(GameMode requiredMode) {
        requireMode(requiredMode);

        if (!active) {
            throw new IllegalStateException(
                    "Run is already complete"
            );
        }
    }

    private void requireMode(GameMode requiredMode) {
        if (gameMode != requiredMode) {
            throw new IllegalStateException(
                    "Run is not a "
                            + requiredMode
                            + " run"
            );
        }
    }

    private void completeRun() {
        active = false;
        completedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public String getVoterId() {
        return voterId;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    }

    public int getPointBalance() {
        requireMode(GameMode.WAGER);

        return pointBalance;
    }

    public int getHighestPointBalance() {
        requireMode(GameMode.WAGER);

        return highestPointBalance;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }
}