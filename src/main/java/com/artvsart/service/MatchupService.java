package com.artvsart.service;

import com.artvsart.model.Matchup;
import com.artvsart.repository.MatchupRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;

@Service
public class MatchupService {

    private static final ZoneId GAME_TIME_ZONE =
            ZoneId.of("America/Chicago");

    private final MatchupRepository matchupRepository;

    public MatchupService(MatchupRepository matchupRepository) {
        this.matchupRepository = matchupRepository;
    }

    public Matchup getTodaysMatchup() {
        LocalDate today = LocalDate.now(GAME_TIME_ZONE);

        return matchupRepository.findByMatchupDate(today)
                .orElseThrow(() -> new IllegalStateException(
                        "No matchup scheduled for " + today
                ));
    }
}