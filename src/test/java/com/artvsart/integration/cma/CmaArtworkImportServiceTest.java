package com.artvsart.integration.cma;

import com.artvsart.model.Artwork;
import com.artvsart.repository.ArtworkRepository;
import com.artvsart.service.ArtworkGenreClassifier;
import com.artvsart.service.BalancedPoolSelector;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CmaArtworkImportServiceTest {

    @Test
    void importsAllErasAndMoreThanFiveWorksPerArtistAcrossPages() {
        CmaArtworkClient client = mock(CmaArtworkClient.class);
        ArtworkRepository repository = mock(ArtworkRepository.class);
        CmaArtworkImportService service = new CmaArtworkImportService(
                client, repository,
                new CmaArtworkEligibilityPolicy(new ArtworkGenreClassifier()),
                new ArtworkGenreClassifier(), new BalancedPoolSelector()
        );
        Artwork existing = new Artwork(
                "cma", "1", "Existing", "Artist A", "1700", "image.jpg"
        );
        when(repository.findAllBySourceOrderByIdAsc("cma"))
                .thenReturn(List.of(existing));
        when(client.searchOpenAccessPaintings(null, 0, 1000))
                .thenReturn(new CmaArtworkSearchResponse(
                        new CmaArtworkSearchResponse.Info(7),
                        java.util.stream.LongStream.rangeClosed(1, 3)
                                .mapToObj(id -> artwork(id, 1700, "Artist A", "French"))
                                .toList()
                ));
        when(client.searchOpenAccessPaintings(null, 3, 1000))
                .thenReturn(new CmaArtworkSearchResponse(
                        new CmaArtworkSearchResponse.Info(7),
                        java.util.stream.LongStream.rangeClosed(4, 7)
                                .mapToObj(id -> artwork(id, 1700, "Artist A", "French"))
                                .toList()
                ));

        assertEquals(6, service.importModernPaintingPool(0, null, 0));
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Artwork>> saved = ArgumentCaptor.forClass(List.class);
        verify(repository).saveAll(saved.capture());
        assertEquals(6, saved.getValue().size());
        org.junit.jupiter.api.Assertions.assertTrue(saved.getValue().stream()
                .noneMatch(value -> value.getSourceArtworkId().equals("1")));
    }

    @Test
    void importsNewestPaintingsWithoutOverloadingOneArtist() {
        CmaArtworkClient client = mock(CmaArtworkClient.class);
        ArtworkRepository repository = mock(ArtworkRepository.class);
        CmaArtworkImportService service = new CmaArtworkImportService(
                client,
                repository,
                new CmaArtworkEligibilityPolicy(
                        new ArtworkGenreClassifier()
                ),
                new ArtworkGenreClassifier(),
                new BalancedPoolSelector()
        );

        when(repository.findAllBySourceOrderByIdAsc("cma"))
                .thenReturn(List.of());
        when(client.searchOpenAccessPaintings(1850, 0, 1000))
                .thenReturn(new CmaArtworkSearchResponse(
                        new CmaArtworkSearchResponse.Info(5),
                        List.of(
                                artwork(1L, 1930, "Artist A", "American"),
                                artwork(2L, 1940, "Artist A", "American"),
                                artwork(3L, 1920, "Artist A", "American"),
                                artwork(4L, 1950, "Artist A", "American"),
                                artwork(5L, 1910, "Artist B", "French")
                        )
                ));
        when(repository.saveAll(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertEquals(4, service.importModernPaintingPool(
                4,
                1850,
                3
        ));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Artwork>> saved =
                ArgumentCaptor.forClass(List.class);
        verify(repository).saveAll(saved.capture());

        List<Artwork> artworks = saved.getValue();
        assertEquals(List.of("4", "2", "1", "5"), artworks.stream()
                .map(Artwork::getSourceArtworkId)
                .toList());
        assertEquals("cma", artworks.getFirst().getSource());
        assertEquals("Artist A", artworks.getFirst().getArtistName());
        assertEquals("American", artworks.getFirst().getArtistNationality());
        assertEquals(1900, artworks.getFirst().getArtistBeginYear());
        assertEquals(1950, artworks.getFirst().getObjectBeginYear());
        assertEquals("oil on canvas", artworks.getFirst().getMedium());
        assertEquals(
                "https://example.test/4-print.jpg",
                artworks.getFirst().getOriginalImageUrl()
        );
    }

    @Test
    void skipsApiCallWhenCmaPoolAlreadyMeetsTarget() {
        CmaArtworkClient client = mock(CmaArtworkClient.class);
        ArtworkRepository repository = mock(ArtworkRepository.class);
        Artwork existing = new Artwork(
                "cma",
                "1",
                "Existing",
                "Artist",
                "1900",
                "https://example.test/image.jpg"
        );

        when(repository.findAllBySourceOrderByIdAsc("cma"))
                .thenReturn(List.of(existing));

        CmaArtworkImportService service = new CmaArtworkImportService(
                client,
                repository,
                new CmaArtworkEligibilityPolicy(
                        new ArtworkGenreClassifier()
                ),
                new ArtworkGenreClassifier(),
                new BalancedPoolSelector()
        );

        assertEquals(0, service.importModernPaintingPool(
                1,
                1850,
                3
        ));
        verify(client, never()).searchOpenAccessPaintings(
                anyInt(),
                anyInt(),
                anyInt()
        );
        verify(repository, never()).saveAll(any());
    }

    private CmaArtworkResponse artwork(
            long id,
            int year,
            String artist,
            String nationality
    ) {
        return new CmaArtworkResponse(
                id,
                "CC0",
                "Artwork " + id,
                Integer.toString(year),
                year,
                year,
                List.of(new CmaArtworkResponse.Creator(
                        id,
                        artist + " (" + nationality + ", 1900-1980)",
                        null,
                        "artist",
                        "1900",
                        "1980"
                )),
                List.of("Example culture"),
                "oil on canvas",
                "Modern Art",
                "Painting",
                "https://clevelandart.org/art/" + id,
                new CmaArtworkResponse.Images(
                        new CmaArtworkResponse.Image(
                                "https://example.test/" + id + "-web.jpg"
                        ),
                        new CmaArtworkResponse.Image(
                                "https://example.test/" + id + "-print.jpg"
                        )
                ),
                null,
                "object"
        );
    }
}
