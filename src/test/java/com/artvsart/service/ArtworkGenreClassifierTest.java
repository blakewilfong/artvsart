package com.artvsart.service;

import com.artvsart.model.ArtworkGenre;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArtworkGenreClassifierTest {

    private final ArtworkGenreClassifier classifier =
            new ArtworkGenreClassifier();

    @Test
    void classifiesTheBroadGenresUsedForBalancing() {
        assertEquals(
                ArtworkGenre.PORTRAIT,
                classifier.classify(List.of("Self-Portrait"))
        );
        assertEquals(
                ArtworkGenre.LANDSCAPE,
                classifier.classify("River landscape")
        );
        assertEquals(
                ArtworkGenre.STILL_LIFE,
                classifier.classify("Still Life with Apples")
        );
        assertEquals(
                ArtworkGenre.ABSTRACT,
                classifier.classify("Non-representational")
        );
        assertEquals(
                ArtworkGenre.DAILY_LIFE,
                classifier.classify("Scene of daily life")
        );
        assertEquals(
                ArtworkGenre.MYTHOLOGY,
                classifier.classify("Classical mythology")
        );
        assertEquals(
                ArtworkGenre.RELIGIOUS,
                classifier.classify("Madonna and Child")
        );
        assertEquals(
                ArtworkGenre.OTHER,
                classifier.classify("Untitled composition")
        );
    }

    @Test
    void classifiesMuseumSubjectTermsWithoutGenericGenreLabels() {
        assertEquals(
                ArtworkGenre.STILL_LIFE,
                classifier.classify("Fruits and Flowers")
        );
        assertEquals(
                ArtworkGenre.LANDSCAPE,
                classifier.classify("Maine Coast")
        );
        assertEquals(
                ArtworkGenre.DAILY_LIFE,
                classifier.classify("Café Scene")
        );
        assertEquals(
                ArtworkGenre.MYTHOLOGY,
                classifier.classify("Ariadne")
        );
        assertEquals(
                ArtworkGenre.RELIGIOUS,
                classifier.classify("Christ Appearing to His Mother")
        );
    }

    @Test
    void detectsSketchesAcrossMuseumMetadata() {
        assertTrue(classifier.isSketch(
                "Study",
                "Graphite sketch on paper"
        ));
        assertFalse(classifier.isSketch(
                "Finished drawing",
                "Graphite on paper"
        ));
    }
}
