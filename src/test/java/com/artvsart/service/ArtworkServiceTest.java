package com.artvsart.service;

import com.artvsart.model.Artwork;
import com.artvsart.model.ArtworkGenre;
import com.artvsart.repository.ArtworkRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ArtworkServiceTest {

    @Test
    void loadsAllConfiguredPlayableSources() {
        ArtworkRepository repository = mock(ArtworkRepository.class);
        ArtworkService service = new ArtworkService(
                repository,
                "met, nga,met",
                new BalancedPoolSelector()
        );

        service.getPlayableArtworks();

        verify(repository).findAllBySourceInOrderByIdAsc(
                List.of("met", "nga")
        );
    }

    @Test
    void rejectsEmptySourceConfiguration() {
        ArtworkRepository repository = mock(ArtworkRepository.class);

        assertThrows(
                IllegalArgumentException.class,
                () -> new ArtworkService(
                        repository,
                        " , ",
                        new BalancedPoolSelector()
                )
        );
    }

    @Test
    void balancesQuestionCandidatesBySourceAndGenre() {
        ArtworkRepository repository = mock(ArtworkRepository.class);
        ArtworkService service = new ArtworkService(
                repository,
                "met,nga",
                new BalancedPoolSelector()
        );
        List<Artwork> artworks = List.of(
                artwork("met", "1", ArtworkGenre.PORTRAIT),
                artwork("met", "2", ArtworkGenre.PORTRAIT),
                artwork("met", "3", ArtworkGenre.LANDSCAPE),
                artwork("met", "4", ArtworkGenre.LANDSCAPE),
                artwork("nga", "5", ArtworkGenre.PORTRAIT),
                artwork("nga", "6", ArtworkGenre.PORTRAIT),
                artwork("nga", "7", ArtworkGenre.LANDSCAPE),
                artwork("nga", "8", ArtworkGenre.LANDSCAPE)
        );

        when(repository.findAllBySourceInOrderByIdAsc(
                List.of("met", "nga")
        )).thenReturn(artworks);

        List<Artwork> selected =
                service.getBalancedQuestionCandidates(4);

        assertEquals(4, selected.size());
        assertEquals(
                4,
                selected.stream()
                        .map(artwork -> artwork.getSource()
                                + ":" + artwork.getGenre())
                        .distinct()
                        .count()
        );
    }

    private Artwork artwork(
            String source,
            String id,
            ArtworkGenre genre
    ) {
        Artwork artwork = new Artwork(
                source,
                id,
                "Title",
                "Artist",
                "1900",
                "image.jpg"
        );
        artwork.classifyGenre(genre);
        return artwork;
    }
}
