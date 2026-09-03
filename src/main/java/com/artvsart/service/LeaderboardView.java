package com.artvsart.service;

import java.time.LocalDate;
import java.util.List;

public record LeaderboardView(
        long entryId,
        int finalScore,
        int dailyHighScore,
        int allTimeHighScore,
        boolean newDailyRecord,
        boolean newAllTimeRecord,
        boolean nameEligible,
        boolean named,
        List<LeaderboardRow> dailyScores,
        List<LeaderboardRow> allTimeScores,
        PlayerBest playerBest
) {
    public record LeaderboardRow(
            int rank,
            String playerName,
            int score,
            LocalDate achievedOn,
            boolean currentPlayer,
            boolean currentRun
    ) {
    }

    public record PlayerBest(
            int score,
            LocalDate achievedOn
    ) {
    }
}
