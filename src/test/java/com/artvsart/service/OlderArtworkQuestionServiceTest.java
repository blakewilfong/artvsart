package com.artvsart.service;

import com.artvsart.model.Artwork;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OlderArtworkQuestionServiceTest {

    private final OlderArtworkQuestionService service =
            new OlderArtworkQuestionService();

    @Test
    void acceptsArtworksAtLeastTwentyFiveYearsApart() {
        Artwork older = artwork(1L, 1800);
        Artwork newer = artwork(2L, 1825);

        assertTrue(service.isEligiblePair(older, newer));
    }

    @Test
    void rejectsArtworksLessThanTwentyFiveYearsApart() {
        Artwork first = artwork(1L, 1800);
        Artwork second = artwork(2L, 1824);

        assertFalse(service.isEligiblePair(first, second));
    }

    @Test
    void rejectsArtworkWithoutKnownDate() {
        Artwork datedArtwork = artwork(1L, 1800);
        Artwork undatedArtwork = artwork(2L, null);

        assertFalse(
                service.isEligiblePair(
                        datedArtwork,
                        undatedArtwork
                )
        );
    }

    @Test
    void rejectsArtworkWithADateRange() {
        Artwork exactArtwork = artwork(1L, 1855);
        Artwork rangedArtwork = artwork(2L, 1800, 1900);

        assertFalse(service.isEligiblePair(
                exactArtwork,
                rangedArtwork
        ));
    }

    @Test
    void returnsArtworkWithEarlierBeginningYear() {
        Artwork newer = artwork(1L, 1900);
        Artwork older = artwork(2L, 1750);

        Artwork answer = service.getCorrectArtwork(
                newer,
                older
        );

        assertSame(older, answer);
    }

    @Test
    void evaluatesSelectedArtworkId() {
        Artwork older = artwork(1L, 1700);
        Artwork newer = artwork(2L, 1850);

        assertTrue(
                service.isCorrect(
                        1L,
                        older,
                        newer
                )
        );

        assertFalse(
                service.isCorrect(
                        2L,
                        older,
                        newer
                )
        );
    }

    private Artwork artwork(Long id, Integer year) {
        return artwork(id, year, year);
    }

    private Artwork artwork(
            Long id,
            Integer beginYear,
            Integer endYear
    ) {
        Artwork artwork = mock(Artwork.class);

        when(artwork.getId()).thenReturn(id);
        when(artwork.findSingleCreationYear()).thenReturn(
                beginYear != null && beginYear.equals(endYear)
                        ? Optional.of(beginYear)
                        : Optional.empty()
        );

        return artwork;
    }
}
