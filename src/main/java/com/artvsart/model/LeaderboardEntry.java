package com.artvsart.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(
        name = "leaderboard_entries",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_leaderboard_game_run",
                columnNames = "game_run_id"
        )
)
public class LeaderboardEntry {

    public static final int MAXIMUM_NAME_LENGTH = 24;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_run_id", nullable = false)
    private GameRun gameRun;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GameMode gameMode;

    @Column(name = "voter_id", nullable = false, length = 36)
    private String voterId;

    @Column(nullable = false)
    private int score;

    @Column(length = MAXIMUM_NAME_LENGTH)
    private String displayName;

    @Column(nullable = false, updatable = false)
    private Instant achievedAt;

    @Column(nullable = false)
    private boolean dailyTopTenAtAchievement;

    @Column(nullable = false)
    private boolean allTimeTopTenAtAchievement;

    @Column(nullable = false)
    private boolean dailyRecordAtAchievement;

    @Column(nullable = false)
    private boolean allTimeRecordAtAchievement;

    protected LeaderboardEntry() {
    }

    public LeaderboardEntry(GameRun gameRun, int score) {
        if (gameRun == null || gameRun.isActive()
                || gameRun.getCompletedAt() == null) {
            throw new IllegalArgumentException(
                    "A completed game run is required"
            );
        }

        if (score < 0) {
            throw new IllegalArgumentException(
                    "A leaderboard score cannot be negative"
            );
        }

        this.gameRun = gameRun;
        this.gameMode = gameRun.getGameMode();
        this.voterId = gameRun.getVoterId();
        this.score = score;
        this.achievedAt = gameRun.getCompletedAt();
    }

    public void recordQualification(
            int dailyRank,
            int allTimeRank
    ) {
        dailyTopTenAtAchievement = isTopTen(dailyRank);
        allTimeTopTenAtAchievement = isTopTen(allTimeRank);
        dailyRecordAtAchievement = dailyRank == 1;
        allTimeRecordAtAchievement = allTimeRank == 1;
    }

    public void name(String displayName) {
        if (!isNameEligible()) {
            throw new IllegalStateException(
                    "This score did not qualify for a leaderboard name"
            );
        }

        String normalizedName = normalizeName(displayName);
        this.displayName = normalizedName;
    }

    private boolean isTopTen(int rank) {
        return rank >= 1 && rank <= 10;
    }

    private String normalizeName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("A name is required");
        }

        String normalized = name.trim().replaceAll("\\s+", " ");

        if (normalized.isBlank()) {
            throw new IllegalArgumentException("A name is required");
        }

        if (normalized.length() > MAXIMUM_NAME_LENGTH) {
            throw new IllegalArgumentException(
                    "Name must be " + MAXIMUM_NAME_LENGTH
                            + " characters or fewer"
            );
        }

        if (normalized.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException(
                    "Name cannot contain control characters"
            );
        }

        return normalized;
    }

    public Long getId() {
        return id;
    }

    public GameRun getGameRun() {
        return gameRun;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public String getVoterId() {
        return voterId;
    }

    public int getScore() {
        return score;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Instant getAchievedAt() {
        return achievedAt;
    }

    public boolean isNameEligible() {
        return dailyTopTenAtAchievement
                || allTimeTopTenAtAchievement;
    }

    public boolean isDailyRecordAtAchievement() {
        return dailyRecordAtAchievement;
    }

    public boolean isAllTimeRecordAtAchievement() {
        return allTimeRecordAtAchievement;
    }
}
