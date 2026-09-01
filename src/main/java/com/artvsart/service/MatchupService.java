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

    public Matchup getTodaysMatchup(int roundNumber) {
        DailyGame dailyGame = getTodaysGame();

        if (roundNumber < 1
                || roundNumber > dailyGame.getTotalRounds()) {
            throw new IllegalArgumentException(
                    "Round number is outside today's game"
            );
        }

        return matchupRepository
                .findByDailyGameIdAndRoundNumber(
                        dailyGame.getId(),
                        roundNumber
                )
                .orElseThrow(() -> new IllegalStateException(
                        "Round " + roundNumber
                                + " has not been scheduled"
                ));
    }

    public Matchup getTodaysMatchupById(Long matchupId) {
        Matchup matchup = matchupRepository
                .findById(matchupId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Matchup does not exist"
                ));

        if (!matchup.getDailyGame()
                .getGameDate()
                .equals(today())) {
            throw new IllegalArgumentException(
                    "Voting is only open for today's game"
            );
        }

        return matchup;
    }

    public DailyGame getTodaysGame() {
        LocalDate today = today();

        return dailyGameRepository
                .findByGameDate(today)
                .orElseThrow(() -> new IllegalStateException(
                        "No daily game scheduled for " + today
                ));
    }

    private LocalDate today() {
        return LocalDate.now(GAME_TIME_ZONE);
    }
}