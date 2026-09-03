package com.artvsart.service;

import com.artvsart.repository.ArtworkRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ArtworkServiceTest {

    @Test
    void loadsAllConfiguredPlayableSources() {
        ArtworkRepository repository = mock(ArtworkRepository.class);
        ArtworkService service = new ArtworkService(
                repository,
                "met, nga,met"
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
                () -> new ArtworkService(repository, " , ")
        );
    }
}
