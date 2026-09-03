package com.artvsart.service;

import com.artvsart.model.Artwork;
import com.artvsart.model.ArtworkMetadata;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArtistBornEarlierQuestionServiceTest {

    private final ArtistBornEarlierQuestionService service =
            new ArtistBornEarlierQuestionService();

    @Test
    void acceptsArtistsAtLeastFiveYearsApart() {
        Artwork earlierArtist =
                artwork("Earlier Artist", 1800);

        Artwork laterArtist =
                artwork("Later Artist", 1805);

        assertTrue(
                service.isEligiblePair(
                        earlierArtist,
                        laterArtist
                )
        );
    }

    @Test
    void rejectsArtistsLessThanFiveYearsApart() {
        Artwork first =
                artwork("First Artist", 1800);

        Artwork second =
                artwork("Second Artist", 1804);

        assertFalse(
                service.isEligiblePair(
                        first,
                        second
                )
        );
    }

    @Test
    void rejectsUnknownBirthYear() {
        Artwork knownArtist =
                artwork("Known Artist", 1800);

        Artwork unknownArtist =
                artwork("Unknown Artist", null);

        assertFalse(
                service.isEligiblePair(
                        knownArtist,
                        unknownArtist
                )
        );
    }

    @Test
    void rejectsTwoWorksBySameArtist() {
        Artwork first =
                artwork("Claude Monet", 1840);

        Artwork second =
                artwork("Claude Monet", 1840);

        assertFalse(
                service.isEligiblePair(
                        first,
                        second
                )
        );
    }

    @Test
    void returnsArtworkByEarlierBornArtist() {
        Artwork laterArtist =
                artwork("Later Artist", 1900);

        Artwork earlierArtist =
                artwork("Earlier Artist", 1750);

        Artwork answer =
                service.getCorrectArtwork(
                        laterArtist,
                        earlierArtist
                );

        assertSame(earlierArtist, answer);
    }

    private Artwork artwork(
            String artistName,
            Integer artistBirthYear
    ) {
        Artwork artwork = new Artwork(
                "met",
                artistName + "-" + artistBirthYear,
                "Example Artwork",
                artistName,
                "1900",
                "image.jpg"
        );

        artwork.updateMetadata(
                new ArtworkMetadata(
                        "original-image.jpg",
                        "Example Department",
                        null,
                        artistBirthYear,
                        null,
                        1900,
                        1900,
                        null,
                        null,
                        "Oil on canvas"
                )
        );

        return artwork;
    }
}