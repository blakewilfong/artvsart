package com.artvsart.service;

import com.artvsart.model.Artwork;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ArtistLastNameQuestionStrategyTest {

    private final ArtistLastNameQuestionStrategy strategy =
            new ArtistLastNameQuestionStrategy();

    @ParameterizedTest
    @ValueSource(strings = {
            "Paul C\u00e9zanne",
            "Paul Ce\u0301zanne",
            "PAUL C\u00c9ZANNE",
            "C\u00e9zanne, Paul"
    })
    void rejectsEquivalentAccentedSurnames(String artistName) {
        Artwork plain = artwork("1", "Paul Cezanne");
        Artwork accented = artwork("2", artistName);

        assertFalse(strategy.isEligiblePair(plain, accented, 1));
        assertFalse(strategy.isEligiblePair(accented, plain, 1));
        assertThrows(IllegalArgumentException.class,
                () -> strategy.getCorrectArtwork(plain, accented));
        assertThrows(IllegalArgumentException.class,
                () -> strategy.getQuestionParameter(plain, accented, 1));
    }

    @Test
    void preservesAccentsWhenSelectingBetweenDifferentArtists() {
        Artwork cezanne = artwork("1", "Paul C\u00e9zanne");
        Artwork monet = artwork("2", "Claude Monet");

        assertTrue(strategy.isEligiblePair(cezanne, monet, 1));
        assertEquals("C\u00e9zanne",
                strategy.getQuestionParameter(cezanne, monet, 1));
        assertEquals("C\u00e9zanne",
                strategy.getQuestionParameter(monet, cezanne, 1));
        assertSame(cezanne, strategy.getCorrectArtwork(cezanne, monet));
        assertSame(cezanne, strategy.getCorrectArtwork(monet, cezanne));
        assertEquals("Paul C\u00e9zanne", cezanne.getArtistName());
    }

    @Test
    void identifiesOneArtworkByTheArtistsLastName() {
        Artwork monet = artwork("1", "Claude Monet");
        Artwork renoir = artwork("2", "Pierre-Auguste Renoir");

        assertTrue(strategy.isEligiblePair(monet, renoir, 1));
        assertEquals(
                "Monet",
                strategy.getQuestionParameter(monet, renoir, 1)
        );
        assertSame(
                monet,
                strategy.getCorrectArtwork(monet, renoir)
        );
    }

    @Test
    void preservesLowercaseSurnameParticles() {
        Artwork vanGogh = artwork("1", "Vincent van Gogh");
        Artwork daVinci = artwork("2", "Leonardo da Vinci");

        assertTrue(strategy.isEligiblePair(vanGogh, daVinci, 1));
        assertEquals(
                "da Vinci",
                strategy.getQuestionParameter(vanGogh, daVinci, 1)
        );
        assertSame(
                daVinci,
                strategy.getCorrectArtwork(vanGogh, daVinci)
        );
    }

    @Test
    void rejectsMatchingUnknownOrAttributedArtists() {
        Artwork pierreRenoir = artwork("1", "Pierre Renoir");
        Artwork jeanRenoir = artwork("2", "Jean Renoir");
        Artwork unknown = artwork("3", "Unknown Artist");
        Artwork anonymous = artwork("5", "Anonymous Artist");
        Artwork attributed = artwork(
                "4",
                "Attributed to Claude Monet"
        );

        assertFalse(strategy.isEligiblePair(
                pierreRenoir,
                jeanRenoir,
                1
        ));
        assertFalse(strategy.isEligiblePair(
                pierreRenoir,
                unknown,
                1
        ));
        assertFalse(strategy.isEligiblePair(
                pierreRenoir,
                anonymous,
                1
        ));
        assertFalse(strategy.isEligiblePair(
                pierreRenoir,
                attributed,
                1
        ));
    }

    private Artwork artwork(String id, String artist) {
        return new Artwork(
                "test",
                id,
                "Title " + id,
                artist,
                "1900",
                "image.jpg"
        );
    }
}
