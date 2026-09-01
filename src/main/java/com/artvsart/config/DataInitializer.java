package com.artvsart.config;

import com.artvsart.model.Artwork;
import com.artvsart.repository.ArtworkRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ArtworkRepository artworkRepository;

    public DataInitializer(ArtworkRepository artworkRepository) {
        this.artworkRepository = artworkRepository;
    }

    @Override
    public void run(String... args) {
        if (artworkRepository.count() > 0) {
            return;
        }

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

        artworkRepository.saveAll(List.of(grandeJatte, theBedroom));
    }
}