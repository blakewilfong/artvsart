package com.artvsart.integration.met;

import com.artvsart.service.ArtworkGenreClassifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
public class MetArtworkEligibilityPolicy {

    private final ArtworkGenreClassifier genreClassifier;

    public MetArtworkEligibilityPolicy(
            ArtworkGenreClassifier genreClassifier
    ) {
        this.genreClassifier = genreClassifier;
    }

    private static final List<String>
            ALLOWED_CLASSIFICATION_TERMS =
            List.of(
                    "painting",
                    "drawing",
                    "print",
                    "collage"
            );

    private static final List<String>
            ALLOWED_OBJECT_NAME_TERMS =
            List.of(
                    "painting",
                    "drawing",
                    "print",
                    "collage",
                    "album leaf",
                    "hanging scroll",
                    "handscroll"
            );

    private static final List<String>
            EXCLUDED_TERMS =
            List.of(
                    "photograph",
                    "printing block",
                    "printing plate"
            );

    public boolean isEligible(
            MetArtworkResponse artwork
    ) {
        if (artwork == null || !artwork.isUsable()) {
            return false;
        }

        String classification =
                normalize(artwork.classification());

        String objectName =
                normalize(artwork.objectName());

        if (genreClassifier.isSketch(
                artwork.title(),
                artwork.classification(),
                artwork.objectName(),
                artwork.medium()
        )) {
            return false;
        }

        if (containsAny(
                classification,
                EXCLUDED_TERMS
        ) || containsAny(
                objectName,
                EXCLUDED_TERMS
        )) {
            return false;
        }

        return containsAny(
                classification,
                ALLOWED_CLASSIFICATION_TERMS
        ) || containsAny(
                objectName,
                ALLOWED_OBJECT_NAME_TERMS
        );
    }

    private boolean containsAny(
            String value,
            List<String> terms
    ) {
        return terms.stream()
                .anyMatch(value::contains);
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }

        return value
                .trim()
                .toLowerCase(Locale.ROOT);
    }
}
