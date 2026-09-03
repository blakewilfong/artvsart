package com.artvsart.service;

import com.artvsart.model.GameRun;
import com.artvsart.repository.GameRunRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LeaderboardBackfillRunner implements ApplicationRunner {

    private final GameRunRepository gameRunRepository;
    private final LeaderboardService leaderboardService;
    private final List<LeaderboardScoreProvider> scoreProviders;

    public LeaderboardBackfillRunner(
            GameRunRepository gameRunRepository,
            LeaderboardService leaderboardService,
            List<LeaderboardScoreProvider> scoreProviders
    ) {
        this.gameRunRepository = gameRunRepository;
        this.leaderboardService = leaderboardService;
        this.scoreProviders = scoreProviders;
    }

    @Override
    public void run(ApplicationArguments args) {
        for (LeaderboardScoreProvider provider : scoreProviders) {
            List<GameRun> missingRuns = gameRunRepository
                    .findCompletedRunsWithoutLeaderboardEntry(
                            provider.getGameMode()
                    );

            for (GameRun run : missingRuns) {
                leaderboardService.recordCompletedRun(
                        run,
                        provider.getScore(run)
                );
            }
        }
    }
}
