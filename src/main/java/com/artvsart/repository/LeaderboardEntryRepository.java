package com.artvsart.repository;

import com.artvsart.model.GameMode;
import com.artvsart.model.LeaderboardEntry;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface LeaderboardEntryRepository
        extends JpaRepository<LeaderboardEntry, Long> {

    Optional<LeaderboardEntry> findByGameRunId(Long gameRunId);

    List<LeaderboardEntry>
    findByGameModeAndDisplayNameIsNotNullOrderByScoreDescAchievedAtAscIdAsc(
            GameMode gameMode,
            Pageable pageable
    );

    List<LeaderboardEntry>
    findByGameModeAndDisplayNameIsNotNullAndAchievedAtGreaterThanEqualAndAchievedAtLessThanOrderByScoreDescAchievedAtAscIdAsc(
            GameMode gameMode,
            Instant start,
            Instant end,
            Pageable pageable
    );

    Optional<LeaderboardEntry>
    findFirstByGameModeAndVoterIdOrderByScoreDescAchievedAtAscIdAsc(
            GameMode gameMode,
            String voterId
    );
}
