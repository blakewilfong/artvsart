package com.artvsart.service;

import com.artvsart.model.Artwork;
import com.artvsart.model.DailyGame;
import com.artvsart.model.Matchup;
import com.artvsart.repository.ArtworkRepository;
import com.artvsart.repository.DailyGameRepository;
import com.artvsart.repository.MatchupRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Service
public class DailyGameService {

    private static final int TOTAL_ROUNDS = 10;

    private static final ZoneId GAME_TIME_ZONE =
            ZoneId.of("America/Chicago");

    private final ArtworkService artworkService;
    private final DailyGameRepository dailyGameRepository;
    private final MatchupRepository matchupRepository;

    public DailyGameService(
            ArtworkService artworkService,
            DailyGameRepository dailyGameRepository,
            MatchupRepository matchupRepository
    ) {
        this.artworkService = artworkService;
        this.dailyGameRepository = dailyGameRepository;
        this.matchupRepository = matchupRepository;
    }

    @Transactional
    public DailyGame getOrCreateTodaysGame() {
        return getOrCreateGame(getCurrentGameDate());
    }

    @Transactional
    public DailyGame getOrCreateGame(LocalDate gameDate) {
        DailyGame dailyGame = dailyGameRepository
                .findByGameDate(gameDate)
                .orElseGet(() -> dailyGameRepository.save(
                        new DailyGame(gameDate, TOTAL_ROUNDS)
                ));

        scheduleMissingMatchups(dailyGame);

        return dailyGame;
    }

    public LocalDate getCurrentGameDate() {
        return LocalDate.now(GAME_TIME_ZONE);
    }

    private void scheduleMissingMatchups(DailyGame dailyGame) {
        List<Artwork> artworks =
                artworkService.getPlayableArtworks();

        List<ArtworkPair> pairs = createPairs(artworks);

        if (pairs.size() < dailyGame.getTotalRounds()) {
            throw new IllegalStateException(
                    "Not enough artworks to create "
                            + dailyGame.getTotalRounds()
                            + " unique matchups"
            );
        }

        Collections.shuffle(
                pairs,
                new Random(dailyGame.getGameDate().toEpochDay())
        );

        for (int index = 0;
             index < dailyGame.getTotalRounds();
             index++) {

            int roundNumber = index + 1;
            ArtworkPair pair = pairs.get(index);

            if (matchupRepository
                    .findByDailyGameIdAndRoundNumber(
                            dailyGame.getId(),
                            roundNumber
                    )
                    .isEmpty()) {

                matchupRepository.save(
                        new Matchup(
                                dailyGame,
                                roundNumber,
                                pair.first(),
                                pair.second()
                        )
                );
            }
        }
    }

    private List<ArtworkPair> createPairs(
            List<Artwork> artworks
    ) {
        List<ArtworkPair> pairs = new ArrayList<>();

        for (int first = 0;
             first < artworks.size();
             first++) {

            for (int second = first + 1;
                 second < artworks.size();
                 second++) {

                pairs.add(new ArtworkPair(
                        artworks.get(first),
                        artworks.get(second)
                ));
            }
        }

        return pairs;
    }

    private record ArtworkPair(
            Artwork first,
            Artwork second
    ) {
    }
}