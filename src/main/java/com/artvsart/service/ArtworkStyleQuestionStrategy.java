package com.artvsart.service;

import com.artvsart.model.Artwork;
import com.artvsart.model.ArtworkStyle;
import com.artvsart.model.QuestionType;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.TreeMap;

@Component
public class ArtworkStyleQuestionStrategy
        implements ArtworkQuestionStrategy {

    @Override
    public QuestionType getQuestionType() {
        return QuestionType.ARTWORK_STYLE;
    }

    @Override
    public boolean isEligiblePair(
            Artwork artworkOne,
            Artwork artworkTwo,
            int roundNumber
    ) {
        return selectStyle(artworkOne, artworkTwo) != null;
    }

    @Override
    public Artwork getCorrectArtwork(
            Artwork artworkOne,
            Artwork artworkTwo
    ) {
        String style = requiredStyle(artworkOne, artworkTwo);
        String normalized = ArtworkStyle.normalize(style);

        return artworkOne.findStyleDisplayLabel(normalized).isPresent()
                ? artworkOne
                : artworkTwo;
    }

    @Override
    public String getQuestionParameter(
            Artwork artworkOne,
            Artwork artworkTwo,
            int roundNumber
    ) {
        return requiredStyle(artworkOne, artworkTwo);
    }

    private String requiredStyle(
            Artwork artworkOne,
            Artwork artworkTwo
    ) {
        String style = selectStyle(artworkOne, artworkTwo);

        if (style == null) {
            throw new IllegalArgumentException(
                    "The artworks require one exclusive style"
            );
        }

        return style;
    }

    private String selectStyle(
            Artwork artworkOne,
            Artwork artworkTwo
    ) {
        Map<String, String> first = styles(artworkOne);
        Map<String, String> second = styles(artworkTwo);
        Map<String, String> exclusive = new TreeMap<>();

        first.forEach((key, label) -> {
            if (!second.containsKey(key)) {
                exclusive.put(key, label);
            }
        });

        second.forEach((key, label) -> {
            if (!first.containsKey(key)) {
                exclusive.put(key, label);
            }
        });

        return exclusive.isEmpty()
                ? null
                : exclusive.get(exclusive.keySet().iterator().next());
    }

    private Map<String, String> styles(Artwork artwork) {
        Map<String, String> styles = new TreeMap<>();

        artwork.getStyles().forEach(style -> styles.putIfAbsent(
                style.getNormalizedLabel(),
                style.getDisplayLabel()
        ));

        return styles;
    }
}
