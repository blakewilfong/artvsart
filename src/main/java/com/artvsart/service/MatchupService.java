package com.artvsart.service;

import com.artvsart.model.DailyGame;
import com.artvsart.model.Matchup;
import com.artvsart.repository.DailyGameRepository;
import com.artvsart.repository.MatchupRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;

@Service
public class MatchupService {

    private static final ZoneId GAME_TIME_ZONE =
            ZoneId.of("America/Chicago");

    private final DailyGameRepository dailyGameRepository;
    private final MatchupRepository matchupRepository;

    public MatchupService(
            DailyGameRepository dailyGameRepository,
            MatchupRepository matchupRepository
    ) {
        this.dailyGameRepository = dailyGameRepository;
        this.matchupRepository = matchupRepository;
    }

    public Matchup getTodaysMatchup() {
        LocalDate today = LocalDate.now(GAME_TIME_ZONE);

        DailyGame dailyGame = dailyGameRepository
                .findByGameDate(today)
                .orElseThrow(() -> new IllegalStateException(
                        "No daily game scheduled for " + today
                ));

        return matchupRepository
                .findByDailyGameIdAndRoundNumber(
                        dailyGame.getId(),
                        1
                )
                .orElseThrow(() -> new IllegalStateException(
                        "Round 1 has not been scheduled"
                ));
    }
}