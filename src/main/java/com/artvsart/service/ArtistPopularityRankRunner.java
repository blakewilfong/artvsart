package com.artvsart.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(4)
public class ArtistPopularityRankRunner implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(
            ArtistPopularityRankRunner.class
    );

    private final ArtistPopularityRankingService rankingService;

    public ArtistPopularityRankRunner(
            ArtistPopularityRankingService rankingService
    ) {
        this.rankingService = rankingService;
    }

    @Override
    public void run(ApplicationArguments args) {
        int rankedArtworkCount = rankingService.rankArtists();

        LOGGER.info(
                "Updated artist popularity ranks for {} artworks",
                rankedArtworkCount
        );
    }
}
