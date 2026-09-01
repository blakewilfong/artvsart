package com.artvsart.repository;

import com.artvsart.model.Matchup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MatchupRepository
        extends JpaRepository<Matchup, Long> {

    Optional<Matchup> findByDailyGameIdAndRoundNumber(
            Long dailyGameId,
            int roundNumber
    );
}