package com.artvsart.service;

import com.artvsart.model.DailyGame;
import com.artvsart.model.Matchup;
import com.artvsart.repository.MatchupRepository;
import org.springframework.stereotype.Service;

@Service
public class MatchupService {

    private final DailyGameService dailyGameService;
    private final MatchupRepository matchupRepository;

    public MatchupService(
            DailyGameService dailyGameService,
            MatchupRepository matchupRepository
    ) {
        this.dailyGameService = dailyGameService;
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
                .equals(dailyGameService.getCurrentGameDate())) {
            throw new IllegalArgumentException(
                    "Voting is only open for today's game"
            );
        }

        return matchup;
    }

    public DailyGame getTodaysGame() {
        return dailyGameService.getOrCreateTodaysGame();
    }
}