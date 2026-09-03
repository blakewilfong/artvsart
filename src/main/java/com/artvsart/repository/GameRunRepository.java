package com.artvsart.repository;

import com.artvsart.model.GameMode;
import com.artvsart.model.GameRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GameRunRepository
        extends JpaRepository<GameRun, Long> {

    Optional<GameRun>
    findFirstByVoterIdAndGameModeAndActiveTrueOrderByStartedAtDesc(
            String voterId,
            GameMode gameMode
    );

    @Query("""
            SELECT COALESCE(MAX(run.correctAnswers), 0)
            FROM GameRun run
            WHERE run.gameMode = :gameMode
            """)
    int findHighScoreByGameMode(
            @Param("gameMode") GameMode gameMode
    );

    @Query("""
            SELECT COALESCE(MAX(run.highestPointBalance), 0)
            FROM GameRun run
            WHERE run.gameMode = :gameMode
            """)
    int findHighestPointBalanceByGameMode(
            @Param("gameMode") GameMode gameMode
    );

    @Query("""
            SELECT run
            FROM GameRun run
            WHERE run.gameMode = :gameMode
              AND run.active = false
              AND run.completedAt IS NOT NULL
              AND NOT EXISTS (
                  SELECT entry.id
                  FROM LeaderboardEntry entry
                  WHERE entry.gameRun = run
              )
            ORDER BY run.completedAt ASC, run.id ASC
            """)
    List<GameRun> findCompletedRunsWithoutLeaderboardEntry(
            @Param("gameMode") GameMode gameMode
    );
}
