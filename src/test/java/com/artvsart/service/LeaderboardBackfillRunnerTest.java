package com.artvsart.service;

import com.artvsart.model.GameMode;
import com.artvsart.model.GameRun;
import com.artvsart.repository.GameRunRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LeaderboardBackfillRunnerTest {

    @Test
    void recordsHistoricalRunsUsingTheirModeScoreProvider()
            throws Exception {
        GameRunRepository runRepository =
                mock(GameRunRepository.class);
        LeaderboardService leaderboardService =
                mock(LeaderboardService.class);
        LeaderboardScoreProvider scoreProvider =
                mock(LeaderboardScoreProvider.class);
        GameRun run = mock(GameRun.class);

        when(scoreProvider.getGameMode())
                .thenReturn(GameMode.STREAK);
        when(scoreProvider.getScore(run)).thenReturn(8);
        when(runRepository
                .findCompletedRunsWithoutLeaderboardEntry(
                        GameMode.STREAK
                ))
                .thenReturn(List.of(run));

        LeaderboardBackfillRunner runner =
                new LeaderboardBackfillRunner(
                        runRepository,
                        leaderboardService,
                        List.of(scoreProvider)
                );

        runner.run(null);

        verify(leaderboardService)
                .recordCompletedRun(run, 8);
    }
}
