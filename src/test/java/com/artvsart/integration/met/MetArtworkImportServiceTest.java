package com.artvsart.integration.met;

import com.artvsart.model.Artwork;
import com.artvsart.model.ArtworkGenre;
import com.artvsart.repository.ArtworkRepository;
import com.artvsart.service.ArtworkGenreClassifier;
import com.artvsart.service.BalancedPoolSelector;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;

class MetArtworkImportServiceTest {

    @Test
    void importsBeyondTheOldScanCapAndSkipsExistingArtworks() {
        MetArtworkClient client = mock(MetArtworkClient.class);
        ArtworkRepository repository = mock(ArtworkRepository.class);
        ArtworkGenreClassifier classifier = new ArtworkGenreClassifier();
        MetArtworkImportService service = new MetArtworkImportService(
                client, repository, new MetArtworkEligibilityPolicy(classifier),
                classifier, new BalancedPoolSelector(), 0
        );
        Artwork existing = new Artwork(
                "met", "1", "Existing", "Artist", "1900", "image.jpg"
        );
        when(repository.countBySource("met")).thenReturn(1L);
        when(repository.findAllBySourceOrderByIdAsc("met"))
                .thenReturn(List.of(existing));
        when(client.searchPaintings()).thenReturn(new MetSearchResponse(
                1503, LongStream.rangeClosed(1, 1503).boxed().toList()
        ));
        when(client.fetchArtwork(anyLong())).thenAnswer(
                invocation -> artwork(invocation.getArgument(0))
        );
        List<Artwork> saved = new ArrayList<>();
        when(repository.saveAll(any())).thenAnswer(invocation -> {
            List<Artwork> batch = invocation.getArgument(0);
            saved.addAll(batch);
            return batch;
        });

        assertEquals(1502, service.importPaintingPool(0));
        assertEquals(1502, saved.size());
        assertEquals(1502, saved.stream()
                .map(Artwork::getSourceArtworkId).distinct().count());
        verify(client, never()).fetchArtwork(1L);
        verify(client, never()).searchDepartmentPaintings(anyInt());
    }

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
        return artwork(10L);
    }

    private MetArtworkResponse artwork(long id) {
        return new MetArtworkResponse(
                id,
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
