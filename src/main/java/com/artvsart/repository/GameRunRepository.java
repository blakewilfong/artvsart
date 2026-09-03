package com.artvsart.repository;

import com.artvsart.model.GameMode;
import com.artvsart.model.GameRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}