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

        if (containsAny(
                text,
                "still life",
                "nature morte",
                "flowers",
                "flower piece",
                "fruit",
                "bouquet",
                "vase of",
                "bowl of"
        )) {
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
                "topographical",
                "coast",
                "harbor",
                "harbour",
                "waterfall",
                "mountain",
                "valley",
                "river view",
                "river scene",
                "lake view",
                "by the lake",
                "by the sea",
                "road to the sea",
                "beach",
                "dunes",
                "forest",
                "woodland",
                "country house",
                "winter landscape",
                "moonlight"
        )) {
            return ArtworkGenre.LANDSCAPE;
        }
        if (containsAny(
                text,
                "non-representational",
                "nonrepresentational",
                "abstract",
                "abstraction",
                "color field",
                "colour field",
                "geometric composition",
                "constructivist",
                "suprematist"
        )) {
            return ArtworkGenre.ABSTRACT;
        }
        if (containsAny(
                text,
                "daily life",
                "genre scene",
                "domestic life",
                "street scene",
                "market scene",
                "cafe scene",
                "café scene",
                "tavern scene",
                "workers",
                "washerwomen",
                "at play",
                "circus",
                "household",
                "school interior",
                "people dancing"
        )) {
            return ArtworkGenre.DAILY_LIFE;
        }
        if (containsAny(
                text,
                "mythology",
                "mythological",
                "classical myth",
                "ariadne",
                "aphrodite",
                "artemis",
                "athena",
                "bacchus",
                "dionysus",
                "hercules",
                "heracles",
                "odysseus",
                "achilles",
                "centaur",
                "nymph"
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
                "hindu",
                "christ",
                "jesus",
                "virgin mary",
                "holy family",
                "nativity",
                "annunciation",
                "resurrection",
                "good samaritan",
                "parable",
                "apostle",
                "bodhisattva",
                "guanyin",
                "ganesha",
                "sutra",
                "mandala",
                "arhat",
                "luohan"
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
