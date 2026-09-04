package com.artvsart.service;

import com.artvsart.model.Artwork;
import com.artvsart.repository.ArtworkRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ArtistPopularityRankingServiceTest {

    @Test
    void ranksRecognizableArtistsBeforeObscureArtists() {
        ArtworkRepository repository = mock(ArtworkRepository.class);
        Artwork vanGogh = artwork("met", "Vincent van Gogh");
        Artwork vanGoghAlias = artwork(
                "nga",
                "Vincent Willem van Gogh"
        );
        Artwork monet = artwork("cma", "Claude Monet");
        Artwork obscure = artwork("met", "Example Obscure Artist");
        Artwork unknown = artwork("nga", "Unidentified artist");
        unknown.rankArtistPopularity(1);

        when(repository.findAll()).thenReturn(List.of(
                obscure,
                unknown,
                monet,
                vanGoghAlias,
                vanGogh
        ));

        ArtistPopularityRankingService service =
                new ArtistPopularityRankingService(
                        repository,
                        new ArtistPopularityCatalog()
                );

        assertEquals(5, service.rankArtists());
        assertEquals(1, vanGogh.getArtistPopularityRank());
        assertEquals(1, vanGoghAlias.getArtistPopularityRank());
        assertEquals(2, monet.getArtistPopularityRank());
        assertEquals(31, obscure.getArtistPopularityRank());
        assertNull(unknown.getArtistPopularityRank());
        verify(repository).saveAll(anyList());
    }

    @Test
    void avoidsWritingRanksThatHaveNotChanged() {
        ArtworkRepository repository = mock(ArtworkRepository.class);
        Artwork vanGogh = artwork("met", "Vincent van Gogh");
        vanGogh.rankArtistPopularity(1);

        when(repository.findAll()).thenReturn(List.of(vanGogh));

        ArtistPopularityRankingService service =
                new ArtistPopularityRankingService(
                        repository,
                        new ArtistPopularityCatalog()
                );

        assertEquals(0, service.rankArtists());
    }

    private Artwork artwork(String source, String artistName) {
        return new Artwork(
                source,
                source + "-" + artistName,
                "Example",
                artistName,
                "1900",
                "https://example.com/image.jpg"
        );
    }
}
