package com.artvsart.integration.met;

import com.artvsart.model.Artwork;
import com.artvsart.model.ArtworkGenre;
import com.artvsart.repository.ArtworkRepository;
import com.artvsart.service.ArtworkGenreClassifier;
import com.artvsart.service.BalancedPoolSelector;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MetArtworkImportServiceTest {

    @Test
    void storesTheGenreDerivedFromMetSubjectTags() {
        MetArtworkClient client = mock(MetArtworkClient.class);
        ArtworkRepository repository = mock(ArtworkRepository.class);
        ArtworkGenreClassifier classifier =
                new ArtworkGenreClassifier();
        MetArtworkImportService service = new MetArtworkImportService(
                client,
                repository,
                new MetArtworkEligibilityPolicy(classifier),
                classifier,
                new BalancedPoolSelector(),
                0
        );

        when(client.fetchArtwork(10L)).thenReturn(artwork());
        when(repository.findBySourceAndSourceArtworkId("met", "10"))
                .thenReturn(Optional.empty());
        when(repository.save(any(Artwork.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Artwork imported = service.importArtwork(10L).orElseThrow();

        assertEquals(ArtworkGenre.LANDSCAPE, imported.getGenre());
    }

    private MetArtworkResponse artwork() {
        return new MetArtworkResponse(
                10L,
                false,
                true,
                "1900",
                "https://example.test/original.jpg",
                "https://example.test/display.jpg",
                "European Paintings",
                "Painting",
                "River",
                null,
                null,
                "Jane Artist",
                null,
                "French",
                "1860",
                "1930",
                "1900",
                1900,
                1900,
                "Oil on canvas",
                null,
                "France",
                "Paintings",
                null,
                "https://example.test/artwork",
                null,
                List.of(new MetArtworkResponse.Tag("Landscapes"))
        );
    }
}
