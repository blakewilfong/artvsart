package com.artvsart.config;

import com.artvsart.model.Artwork;
import com.artvsart.repository.ArtworkRepository;
import com.artvsart.service.DailyGameService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ArtworkRepository artworkRepository;
    private final DailyGameService dailyGameService;

    public DataInitializer(
            ArtworkRepository artworkRepository,
            DailyGameService dailyGameService
    ) {
        this.artworkRepository = artworkRepository;
        this.dailyGameService = dailyGameService;
    }

    @Override
    public void run(String... args) {
        seedArtworkPool();
        dailyGameService.getOrCreateTodaysGame();
    }

    private void seedArtworkPool() {
        findOrCreateArtwork(
                "27992",
                "A Sunday on La Grande Jatte, 1884",
                "Georges Seurat",
                "1884-86",
                "/images/artworks/grande-jatte.jpg"
        );

        findOrCreateArtwork(
                "28560",
                "The Bedroom",
                "Vincent van Gogh",
                "1889",
                "/images/artworks/the-bedroom.jpg"
        );

        findOrCreateArtwork(
                "14655",
                "Two Sisters (On the Terrace)",
                "Pierre-Auguste Renoir",
                "1881",
                "/images/artworks/two-sisters.jpg"
        );

        findOrCreateArtwork(
                "16568",
                "Water Lilies",
                "Claude Monet",
                "1906",
                "/images/artworks/water-lilies.jpg"
        );

        findOrCreateArtwork(
                "111442",
                "The Child's Bath",
                "Mary Cassatt",
                "1893",
                "/images/artworks/childs-bath.jpg"
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
}