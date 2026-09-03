package com.artvsart.service;

import com.artvsart.model.Artwork;
import com.artvsart.model.ArtworkMetadata;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArtistNationalityQuestionStrategyTest {

    private final ArtistNationalityQuestionStrategy strategy =
            new ArtistNationalityQuestionStrategy();

    @Test
    void selectsExactlyOneKnownNationality() {
        Artwork french = artwork("French", "Oil on canvas", 1870);
        Artwork dutch = artwork("Dutch", "Oil on canvas", 1880);

        assertTrue(strategy.isEligiblePair(french, dutch, 1));
        assertEquals(
                "Dutch",
                strategy.getQuestionParameter(french, dutch, 1)
        );
        assertSame(dutch, strategy.getCorrectArtwork(french, dutch));
    }

    @Test
    void rejectsUnknownOrMatchingNationalities() {
        Artwork unknown = artwork("Unknown", "Oil", 1870);
        Artwork french = artwork("French", "Oil", 1880);
        Artwork anotherFrench = artwork("French", "Tempera", 1890);

        assertFalse(strategy.isEligiblePair(unknown, french, 1));
        assertFalse(strategy.isEligiblePair(french, anotherFrench, 1));
    }

    private Artwork artwork(
            String nationality,
            String medium,
            int year
    ) {
        Artwork artwork = new Artwork(
                "test", Integer.toString(year), "Title", "Artist",
                Integer.toString(year), "image.jpg"
        );
        artwork.updateMetadata(new ArtworkMetadata(
                null, null, nationality, null, null,
                year, year, null, null, medium
        ));
        return artwork;
    }
}
