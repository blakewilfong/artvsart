package com.artvsart.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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
                "French",
                "France",
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
                "French",
                QuestionType.ARTWORK_CULTURE.getAnswerCaption(
                        artwork,
                        "CULTURE|French"
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
        assertEquals(
                "1830–1900",
                QuestionType.ARTIST_ALIVE_DURING_EVENT
                        .getAnswerCaption(
                                artwork,
                                HistoricalEvent
                                        .FIRST_IMPRESSIONIST_EXHIBITION
                                        .name()
                        )
        );
        assertEquals(
                "Created 1888",
                QuestionType.ARTWORK_CENTURY.getAnswerCaption(
                        artwork,
                        "19th century"
                )
        );
        assertEquals(
                "Artist",
                QuestionType.ARTIST_LAST_NAME.getAnswerCaption(
                        artwork,
                        "Artist"
                )
        );
        assertEquals(
                "Example",
                QuestionType.ARTWORK_TITLE_WORD.getAnswerCaption(
                        artwork,
                        "Example"
                )
        );
        assertEquals(
                "Which artwork was created closer in time to the Mongol sack of Baghdad?",
                QuestionType.BEFORE_HISTORICAL_EVENT.getPrompt(
                        HistoricalEvent.MONGOLS_SACKED_BAGHDAD.name()
                )
        );
        assertEquals(
                "Which artwork was created closer in time to the opening of the first Impressionist exhibition?",
                QuestionType.BEFORE_HISTORICAL_EVENT.getPrompt(
                        HistoricalEvent.FIRST_IMPRESSIONIST_EXHIBITION.name()
                )
        );
        assertEquals(
                "Which artist was alive for the beginning of the Haitian Revolution?",
                QuestionType.ARTIST_ALIVE_DURING_EVENT.getPrompt(
                        HistoricalEvent.HAITIAN_REVOLUTION_BEGAN.name()
                )
        );
        assertEquals(
                "Which artwork is associated with French culture?",
                QuestionType.ARTWORK_CULTURE.getPrompt(
                        "CULTURE|French"
                )
        );
        assertEquals(
                "Which artwork was created in the 19th century?",
                QuestionType.ARTWORK_CENTURY.getPrompt(
                        "19th century"
                )
        );
        assertEquals(
                "Which artwork is by Monet?",
                QuestionType.ARTIST_LAST_NAME.getPrompt("Monet")
        );
        assertEquals(
                "Which artwork has \"Footbridge\" in its title?",
                QuestionType.ARTWORK_TITLE_WORD.getPrompt(
                        "Footbridge"
                )
        );
        assertEquals(
                "Closer",
                QuestionType.BEFORE_HISTORICAL_EVENT
                        .getCorrectAnswerLabel()
        );
        assertEquals(
                "Farther away",
                QuestionType.BEFORE_HISTORICAL_EVENT
                        .getIncorrectAnswerLabel()
        );
        assertEquals(
                "1874",
                QuestionType.BEFORE_HISTORICAL_EVENT
                        .getAnswerContext(
                                HistoricalEvent
                                        .FIRST_IMPRESSIONIST_EXHIBITION
                                        .name()
                        )
        );
        assertEquals(
                "1789",
                QuestionType.ARTIST_ALIVE_DURING_EVENT
                        .getAnswerContext(
                                HistoricalEvent.FRENCH_REVOLUTION.name()
                        )
        );
        assertNull(
                QuestionType.OLDER_ARTWORK
                        .getAnswerContext(null)
        );
    }
}
