package com.artvsart.config;

import com.artvsart.model.Artwork;
import com.artvsart.model.DailyGame;
import com.artvsart.model.Matchup;
import com.artvsart.repository.ArtworkRepository;
import com.artvsart.repository.DailyGameRepository;
import com.artvsart.repository.MatchupRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final int TOTAL_ROUNDS = 10;

    private final ArtworkRepository artworkRepository;
    private final DailyGameRepository dailyGameRepository;
    private final MatchupRepository matchupRepository;

    public DataInitializer(
            ArtworkRepository artworkRepository,
            DailyGameRepository dailyGameRepository,
            MatchupRepository matchupRepository
    ) {
        this.artworkRepository = artworkRepository;
        this.dailyGameRepository = dailyGameRepository;
        this.matchupRepository = matchupRepository;
    }

    @Override
    public void run(String... args) {
        List<Artwork> artworks = loadArtworkPool();

        DailyGame dailyGame = loadTodaysGame();

        List<ArtworkPair> pairs = createPairs(
                artworks,
                dailyGame.getTotalRounds()
        );

        scheduleMatchups(dailyGame, pairs);
    }

    private List<Artwork> loadArtworkPool() {
        return List.of(
                findOrCreateArtwork(
                        "27992",
                        "A Sunday on La Grande Jatte, 1884",
                        "Georges Seurat",
                        "1884-86",
                        "/images/artworks/grande-jatte.jpg"
                ),
                findOrCreateArtwork(
                        "28560",
                        "The Bedroom",
                        "Vincent van Gogh",
                        "1889",
                        "/images/artworks/the-bedroom.jpg"
                ),
                findOrCreateArtwork(
                        "14655",
                        "Two Sisters (On the Terrace)",
                        "Pierre-Auguste Renoir",
                        "1881",
                        "/images/artworks/two-sisters.jpg"
                ),
                findOrCreateArtwork(
                        "16568",
                        "Water Lilies",
                        "Claude Monet",
                        "1906",
                        "/images/artworks/water-lilies.jpg"
                ),
                findOrCreateArtwork(
                        "111442",
                        "The Child's Bath",
                        "Mary Cassatt",
                        "1893",
                        "/images/artworks/childs-bath.jpg"
                )
        );
    }

    private Artwork findOrCreateArtwork(
            String sourceArtworkId,
            String title,
            String artistName,
            String dateDisplay,
            String imageUrl
    ) {
        return artworkRepository
                .findBySourceAndSourceArtworkId(
                        "artic",
                        sourceArtworkId
                )
                .orElseGet(() -> artworkRepository.save(
                        new Artwork(
                                "artic",
                                sourceArtworkId,
                                title,
                                artistName,
                                dateDisplay,
                                imageUrl
                        )
                ));
    }

    private DailyGame loadTodaysGame() {
        LocalDate today = LocalDate.now(
                ZoneId.of("America/Chicago")
        );

        return dailyGameRepository
                .findByGameDate(today)
                .orElseGet(() -> dailyGameRepository.save(
                        new DailyGame(today, TOTAL_ROUNDS)
                ));
    }

    private List<ArtworkPair> createPairs(
            List<Artwork> artworks,
            int totalRounds
    ) {
        List<ArtworkPair> pairs = new ArrayList<>();

        for (int first = 0;
             first < artworks.size()
                     && pairs.size() < totalRounds;
             first++) {

            for (int second = first + 1;
                 second < artworks.size()
                         && pairs.size() < totalRounds;
                 second++) {

                pairs.add(new ArtworkPair(
                        artworks.get(first),
                        artworks.get(second)
                ));
            }
        }

        if (pairs.size() < totalRounds) {
            throw new IllegalStateException(
                    "Not enough artworks to create "
                            + totalRounds
                            + " unique matchups"
            );
        }

        return pairs;
    }

    private void scheduleMatchups(
            DailyGame dailyGame,
            List<ArtworkPair> pairs
    ) {
        for (int index = 0; index < pairs.size(); index++) {
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

    private record ArtworkPair(
            Artwork first,
            Artwork second
    ) {
    }
}