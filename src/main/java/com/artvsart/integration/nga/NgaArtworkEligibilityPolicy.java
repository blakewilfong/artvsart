package com.artvsart.integration.nga;

import com.artvsart.service.ArtworkGenreClassifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
public class NgaArtworkEligibilityPolicy {

    private final ArtworkGenreClassifier genreClassifier;

    public NgaArtworkEligibilityPolicy(
            ArtworkGenreClassifier genreClassifier
    ) {
        this.genreClassifier = genreClassifier;
    }

    private static final List<String> ALLOWED_TYPES =
            List.of("painting", "collage");

    private static final List<String> EXCLUDED_TERMS =
            List.of(
                    "photograph",
                    "sculpture",
                    "furniture",
                    "decorative",
                    "coin",
                    "medal"
            );

    public boolean isEligible(
            String title,
            String displayDate,
            Integer beginYear,
            String classification,
            String subClassification,
            String visualClassification,
            String medium,
            boolean virtualObject
    ) {
        if (!hasText(title)
                || !hasText(displayDate)
                || beginYear == null
                || virtualObject) {
            return false;
        }

        String type = normalize(
                classification,
                subClassification,
                visualClassification
        );

        String description = normalize(type, medium);

        if (genreClassifier.isSketch(
                title,
                classification,
                subClassification,
                visualClassification,
                medium
        )) {
            return false;
        }

        if (EXCLUDED_TERMS.stream()
                .anyMatch(description::contains)) {
            return false;
        }

        return ALLOWED_TYPES.stream()
                .anyMatch(type::contains);
    }

    private String normalize(String... values) {
        StringBuilder normalized = new StringBuilder();

        for (String value : values) {
            if (value != null && !value.isBlank()) {
                normalized.append(' ')
                        .append(value.trim().toLowerCase(Locale.ROOT));
            }
        }

        return normalized.toString();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
