package com.artvsart.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ArtworkTest {

    @Test
    void updatesAllArtworkMetadata() {
        Artwork artwork = new Artwork(
                "met",
                "123",
                "Example Artwork",
                "Example Artist",
                "1875",
                "small-image.jpg",
                "https://example.com/artwork",
                "CC0"
        );

        ArtworkMetadata metadata = new ArtworkMetadata(
                "original-image.jpg",
                "European Paintings",
                "French",
                1840,
                1900,
                1875,
                1876,
                "French",
                "France",
                "Oil on canvas"
        );

        artwork.updateMetadata(metadata);

        assertEquals(
                "original-image.jpg",
                artwork.getOriginalImageUrl()
        );
        assertEquals(
                "European Paintings",
                artwork.getDepartment()
        );
        assertEquals(
                "French",
                artwork.getArtistNationality()
        );
        assertEquals(
                1840,
                artwork.getArtistBeginYear()
        );
        assertEquals(
                1900,
                artwork.getArtistEndYear()
        );
        assertEquals(
                1875,
                artwork.getObjectBeginYear()
        );
        assertEquals(
                1876,
                artwork.getObjectEndYear()
        );
        assertEquals(
                "French",
                artwork.getCulture()
        );
        assertEquals(
                "France",
                artwork.getCountry()
        );
        assertEquals(
                "Oil on canvas",
                artwork.getMedium()
        );
    }
}