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

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false, updatable = false)
    private Instant startedAt;

    private Instant completedAt;

    protected GameRun() {
    }

    private GameRun(
            GameMode gameMode,
            String voterId
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
        this.active = true;
        this.startedAt = Instant.now();
    }

    public static GameRun startStreak(String voterId) {
        return new GameRun(
                GameMode.STREAK,
                voterId
        );
    }

    public void recordStreakAnswer(boolean correct) {
        requireActiveStreakRun();

        if (!correct) {
            active = false;
            completedAt = Instant.now();
            return;
        }

        correctAnswers++;
        roundNumber++;
    }

    private void requireActiveStreakRun() {
        if (gameMode != GameMode.STREAK) {
            throw new IllegalStateException(
                    "Run is not a Streak Mode run"
            );
        }

        if (!active) {
            throw new IllegalStateException(
                    "Run is already complete"
            );
        }
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