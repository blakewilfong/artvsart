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

        Artwork nighthawks = new Artwork(
                "artic",
                "111628",
                "Nighthawks",
                "Edward Hopper",
                "1942",
                "https://www.artic.edu/iiif/2/831a05de-d3f6-f4fa-a460-23008dd58dda/full/843,/0/default.jpg"
        );

        Artwork americanGothic = new Artwork(
                "artic",
                "6565",
                "American Gothic",
                "Grant Wood",
                "1930",
                "https://www.artic.edu/iiif/2/b272df73-a965-ac37-4172-be4e99483637/full/843,/0/default.jpg"
        );

        artworkRepository.saveAll(List.of(nighthawks, americanGothic));
    }
}