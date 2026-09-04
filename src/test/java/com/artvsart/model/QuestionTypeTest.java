package com.artvsart.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QuestionTypeTest {

    @Test
    void usesConciseAnswerCaptionsForEveryPlayableQuestionType() {
        Artwork artwork = new Artwork(
                "met",
                "1",
                "Example",
                "Artist",
                "1888",
                "https://example.test/image.jpg"
        );
        artwork.updateMetadata(new ArtworkMetadata(
                null,
                null,
                "French",
                1830,
                1900,
                1888,
                1888,
                null,
                null,
                "Oil on canvas"
        ));
        artwork.replaceStyles(List.of(new Artwork.StyleDefinition(
                ArtworkStyleType.STYLE,
                "Impressionism",
                "nga"
        )));

        assertEquals(
                "Created 1888",
                QuestionType.OLDER_ARTWORK.getAnswerCaption(
                        artwork,
                        null
                )
        );
        assertEquals(
                "Born 1830",
                QuestionType.ARTIST_BORN_EARLIER.getAnswerCaption(
                        artwork,
                        null
                )
        );
        assertEquals(
                "Age 58",
                QuestionType.ARTIST_YOUNGER_AT_CREATION
                        .getAnswerCaption(artwork, null)
        );
        assertEquals(
                "French",
                QuestionType.ARTIST_NATIONALITY.getAnswerCaption(
                        artwork,
                        "French"
                )
        );
        assertEquals(
                "Oil on canvas",
                QuestionType.ARTWORK_MEDIUM.getAnswerCaption(
                        artwork,
                        ArtworkMediumCategory.OIL.name()
                )
        );
        assertEquals(
                "Impressionism",
                QuestionType.ARTWORK_STYLE.getAnswerCaption(
                        artwork,
                        "Impressionism"
                )
        );
        assertEquals(
                "Created 1888",
                QuestionType.BEFORE_HISTORICAL_EVENT
                        .getAnswerCaption(
                                artwork,
                                HistoricalEvent.EIFFEL_TOWER_OPENED.name()
                        )
        );
    }
}
