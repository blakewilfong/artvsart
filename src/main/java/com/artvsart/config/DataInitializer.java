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
        if (artworkRepository.count() == 0) {
            Artwork grandeJatte = new Artwork(
                    "artic",
                    "27992",
                    "A Sunday on La Grande Jatte, 1884",
                    "Georges Seurat",
                    "1884-86",
                    "/images/artworks/grande-jatte.jpg"
            );

            Artwork theBedroom = new Artwork(
                    "artic",
                    "28560",
                    "The Bedroom",
                    "Vincent van Gogh",
                    "1889",
                    "/images/artworks/the-bedroom.jpg"
            );

            artworkRepository.saveAll(
                    List.of(grandeJatte, theBedroom)
            );
        }

        List<Artwork> artworks = artworkRepository.findAll();

        if (artworks.size() < 2) {
            throw new IllegalStateException(
                    "At least two artworks are required"
            );
        }

        LocalDate today = LocalDate.now(
                ZoneId.of("America/Chicago")
        );

        DailyGame dailyGame = dailyGameRepository
                .findByGameDate(today)
                .orElseGet(() -> dailyGameRepository.save(
                        new DailyGame(today, TOTAL_ROUNDS)
                ));

        for (int round = 1; round <= TOTAL_ROUNDS; round++) {
            if (matchupRepository
                    .findByDailyGameIdAndRoundNumber(
                            dailyGame.getId(),
                            round
                    )
                    .isEmpty()) {

                matchupRepository.save(
                        new Matchup(
                                dailyGame,
                                round,
                                artworks.get(0),
                                artworks.get(1)
                        )
                );
            }
        }
    }
}