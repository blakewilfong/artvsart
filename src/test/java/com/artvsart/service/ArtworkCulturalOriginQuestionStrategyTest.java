package com.artvsart.service;

import com.artvsart.model.Artwork;
import com.artvsart.model.ArtworkMetadata;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArtworkCulturalOriginQuestionStrategyTest {

    private final ArtworkCulturalOriginQuestionStrategy strategy =
            new ArtworkCulturalOriginQuestionStrategy();

    @Test
    void selectsExactlyOneKnownCulturalOrigin() {
        Artwork japanese = artwork("1", "Japanese", "Japan");
        Artwork french = artwork("2", "French", "France");

        assertTrue(strategy.isEligiblePair(japanese, french, 1));
        assertEquals(
                "CULTURE|French",
                strategy.getQuestionParameter(japanese, french, 1)
        );
        assertSame(
                french,
                strategy.getCorrectArtwork(japanese, french)
        );
    }

    @Test
    void fallsBackToCountryWhenBothCulturesAreMissing() {
        Artwork japanese = artwork("1", null, "Japan");
        Artwork french = artwork("2", null, "France");

        assertTrue(strategy.isEligiblePair(japanese, french, 1));
        assertEquals(
                "COUNTRY|France",
                strategy.getQuestionParameter(japanese, french, 1)
        );
        assertSame(
                french,
                strategy.getCorrectArtwork(japanese, french)
        );
    }

    @Test
    void rejectsUnknownOrMatchingCulturalOrigins() {
        Artwork unknown = artwork("1", "Unknown", null);
        Artwork japanese = artwork("2", "Japanese", "Japan");
        Artwork anotherJapanese = artwork(
                "3",
                "japanese",
                "Japan"
        );

        assertFalse(strategy.isEligiblePair(unknown, japanese, 1));
        assertFalse(strategy.isEligiblePair(
                japanese,
                anotherJapanese,
                1
        ));
    }

    @Test
    void rejectsMixedOrPlaceholderOriginMetadata() {
        Artwork cultureOnly = artwork("1", "Japanese", null);
        Artwork countryOnly = artwork("2", null, "France");
        Artwork placeholder = artwork(
                "3",
                "Not recorded",
                "Unidentified"
        );

        assertFalse(strategy.isEligiblePair(
                cultureOnly,
                countryOnly,
                1
        ));
        assertFalse(strategy.isEligiblePair(
                cultureOnly,
                placeholder,
                1
        ));
    }

    private Artwork artwork(
            String id,
            String culture,
            String country
    ) {
        Artwork artwork = new Artwork(
                "test", id, "Title", "Artist",
                "1880", "image.jpg"
        );
        artwork.updateMetadata(new ArtworkMetadata(
                null, null, null, null, null,
                1880, 1880, culture, country, "Oil"
        ));
        return artwork;
    }
}
