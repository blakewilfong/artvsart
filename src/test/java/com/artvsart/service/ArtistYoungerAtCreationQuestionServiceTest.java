package com.artvsart.service;

import com.artvsart.model.Artwork;
import com.artvsart.model.ArtworkMetadata;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArtistYoungerAtCreationQuestionServiceTest {

    private final ArtistYoungerAtCreationQuestionService service =
            new ArtistYoungerAtCreationQuestionService();

    @Test
    void acceptsArtistAgesAtLeastFiveYearsApart() {
        Artwork youngerArtist =
                artwork(
                        "Younger Artist",
                        1800,
                        1830
                );

        Artwork olderArtist =
                artwork(
                        "Older Artist",
                        1800,
                        1835
                );

        assertTrue(
                service.isEligiblePair(
                        youngerArtist,
                        olderArtist
                )
        );
    }

    @Test
    void rejectsArtistAgesLessThanFiveYearsApart() {
        Artwork first =
                artwork(
                        "First Artist",
                        1800,
                        1830
                );

        Artwork second =
                artwork(
                        "Second Artist",
                        1800,
                        1834
                );

        assertFalse(
                service.isEligiblePair(
                        first,
                        second
                )
        );
    }

    @Test
    void rejectsMissingArtistBirthYear() {
        Artwork completeArtwork =
                artwork(
                        "Known Artist",
                        1800,
                        1840
                );

        Artwork incompleteArtwork =
                artwork(
                        "Unknown Artist",
                        null,
                        1850
                );

        assertFalse(
                service.isEligiblePair(
                        completeArtwork,
                        incompleteArtwork
                )
        );
    }

    @Test
    void rejectsImplausibleArtistAge() {
        Artwork plausibleArtwork =
                artwork(
                        "Plausible Artist",
                        1800,
                        1840
                );

        Artwork implausibleArtwork =
                artwork(
                        "Implausible Artist",
                        1900,
                        1905
                );

        assertFalse(
                service.isEligiblePair(
                        plausibleArtwork,
                        implausibleArtwork
                )
        );
    }

    @Test
    void rejectsArtworkWithADateRange() {
        Artwork exactArtwork = artwork(
                "Exact Artist",
                1800,
                1840
        );
        Artwork rangedArtwork = artwork(
                "Ranged Artist",
                1800,
                1840,
                1860
        );

        assertFalse(service.isEligiblePair(
                exactArtwork,
                rangedArtwork
        ));
    }

    @Test
    void returnsArtworkCreatedByYoungerArtist() {
        Artwork createdAtSixty =
                artwork(
                        "Older Artist",
                        1800,
                        1860
                );

        Artwork createdAtTwentyFive =
                artwork(
                        "Younger Artist",
                        1825,
                        1850
                );

        Artwork answer =
                service.getCorrectArtwork(
                        createdAtSixty,
                        createdAtTwentyFive
                );

        assertSame(createdAtTwentyFive, answer);
    }

    @Test
    void calculatesArtistAgeAtCreation() {
        Artwork artwork =
                artwork(
                        "Example Artist",
                        1840,
                        1872
                );

        assertEquals(
                32,
                service.getArtistAgeAtCreation(artwork)
        );
    }

    private Artwork artwork(
            String artistName,
            Integer artistBirthYear,
            Integer artworkYear
    ) {
        return artwork(
                artistName,
                artistBirthYear,
                artworkYear,
                artworkYear
        );
    }

    private Artwork artwork(
            String artistName,
            Integer artistBirthYear,
            Integer artworkBeginYear,
            Integer artworkEndYear
    ) {
        Artwork artwork = new Artwork(
                "met",
                artistName + "-" + artworkBeginYear,
                "Example Artwork",
                artistName,
                Integer.toString(
                        artworkBeginYear == null
                                ? 0
                                : artworkBeginYear
                ),
                "image.jpg"
        );

        artwork.updateMetadata(
                new ArtworkMetadata(
                        "original-image.jpg",
                        "Example Department",
                        null,
                        artistBirthYear,
                        null,
                        artworkBeginYear,
                        artworkEndYear,
                        null,
                        null,
                        "Oil on canvas"
                )
        );

        return artwork;
    }
}
