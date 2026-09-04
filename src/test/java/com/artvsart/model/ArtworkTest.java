package com.artvsart.model;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class ArtworkTest {

    @Test
    void findsOnlyOneUnambiguousCreationYear() {
        Artwork exact = artworkFrom("met");
        exact.updateMetadata(new ArtworkMetadata(
                null, null, null, null, null,
                1855, 1855, null, null, "Oil"
        ));
        Artwork range = artworkFrom("nga");
        range.updateMetadata(new ArtworkMetadata(
                null, null, null, null, null,
                1800, 1900, null, null, "Oil"
        ));

        assertEquals(
                Optional.of(1855),
                exact.findSingleCreationYear()
        );
        assertEquals(Optional.empty(), range.findSingleCreationYear());
    }

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

    @Test
    void providesTheMetSourceCredit() {
        Artwork artwork = artworkFrom("met");

        assertEquals(
                "Source: The Metropolitan Museum of Art",
                artwork.getSourceCredit()
        );
    }

    @Test
    void providesNgaRequestedSourceCredit() {
        Artwork artwork = artworkFrom("nga");

        assertEquals(
                "Courtesy National Gallery of Art, Washington",
                artwork.getSourceCredit()
        );
    }

    @Test
    void providesTheClevelandMuseumSourceCredit() {
        Artwork artwork = artworkFrom("cma");

        assertEquals(
                "Source: The Cleveland Museum of Art",
                artwork.getSourceCredit()
        );
    }

    @Test
    void retainsMatchingStyleEntitiesDuringSourceRefresh() {
        Artwork artwork = artworkFrom("nga");
        artwork.replaceStylesFromSource(
                "nga",
                List.of(new Artwork.StyleDefinition(
                        ArtworkStyleType.STYLE,
                        "Gothic",
                        "nga"
                ))
        );
        ArtworkStyle originalStyle = artwork.getStyles().getFirst();

        artwork.replaceStylesFromSource(
                "NGA",
                List.of(new Artwork.StyleDefinition(
                        ArtworkStyleType.STYLE,
                        "GOTHIC",
                        "nga"
                ))
        );

        assertEquals(1, artwork.getStyles().size());
        assertSame(originalStyle, artwork.getStyles().getFirst());
        assertEquals("GOTHIC", originalStyle.getDisplayLabel());
    }

    private Artwork artworkFrom(String source) {
        return new Artwork(
                source,
                "123",
                "Example Artwork",
                "Example Artist",
                "1875",
                "small-image.jpg",
                "https://example.com/artwork",
                "CC0"
        );
    }
}
