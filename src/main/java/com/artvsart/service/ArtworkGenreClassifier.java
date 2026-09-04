package com.artvsart.service;

import com.artvsart.model.ArtworkGenre;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

@Component
public class ArtworkGenreClassifier {

    public ArtworkGenre classify(String... descriptions) {
        return classify(Arrays.asList(descriptions));
    }

    public ArtworkGenre classify(
            Collection<String> descriptions
    ) {
        String text = normalize(descriptions);

        if (containsAny(text, "still life", "nature morte")) {
            return ArtworkGenre.STILL_LIFE;
        }
        if (containsAny(
                text,
                "portrait",
                "self-portrait",
                "self portrait",
                "likeness"
        )) {
            return ArtworkGenre.PORTRAIT;
        }
        if (containsAny(
                text,
                "landscape",
                "seascape",
                "cityscape",
                "topographical"
        )) {
            return ArtworkGenre.LANDSCAPE;
        }
        if (containsAny(
                text,
                "non-representational",
                "nonrepresentational",
                "abstract",
                "abstraction"
        )) {
            return ArtworkGenre.ABSTRACT;
        }
        if (containsAny(
                text,
                "daily life",
                "genre scene",
                "domestic life"
        )) {
            return ArtworkGenre.DAILY_LIFE;
        }
        if (containsAny(
                text,
                "mythology",
                "mythological",
                "classical myth"
        )) {
            return ArtworkGenre.MYTHOLOGY;
        }
        if (containsAny(
                text,
                "religious",
                "biblical",
                "life of christ",
                "madonna",
                "crucifixion",
                "altarpiece",
                "saint",
                "buddha",
                "buddhist",
                "hindu"
        )) {
            return ArtworkGenre.RELIGIOUS;
        }

        return ArtworkGenre.OTHER;
    }

    public boolean isSketch(String... descriptions) {
        return containsAny(
                normalize(Arrays.asList(descriptions)),
                "sketch"
        );
    }

    private String normalize(Collection<String> descriptions) {
        StringBuilder normalized = new StringBuilder();

        for (String description : descriptions) {
            if (description != null && !description.isBlank()) {
                normalized.append(' ')
                        .append(description.trim()
                                .toLowerCase(Locale.ROOT));
            }
        }

        return normalized.toString();
    }

    private boolean containsAny(
            String text,
            String... terms
    ) {
        return Arrays.stream(terms).anyMatch(text::contains);
    }
}
