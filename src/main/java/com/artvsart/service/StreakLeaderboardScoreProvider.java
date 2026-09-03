package com.artvsart.service;

import com.artvsart.model.GameMode;
import com.artvsart.model.GameRun;
import org.springframework.stereotype.Component;

@Component
public class StreakLeaderboardScoreProvider
        implements LeaderboardScoreProvider {

    @Override
    public GameMode getGameMode() {
        return GameMode.STREAK;
    }

    @Override
    public int getScore(GameRun run) {
        if (run.getGameMode() != GameMode.STREAK) {
            throw new IllegalArgumentException(
                    "A Streak run is required"
            );
        }

        return run.getCorrectAnswers();
    }
}
