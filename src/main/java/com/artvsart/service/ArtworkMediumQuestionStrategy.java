package com.artvsart.service;

import com.artvsart.model.Artwork;
import com.artvsart.model.ArtworkMediumCategory;
import com.artvsart.model.QuestionType;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Component
public class ArtworkMediumQuestionStrategy
        implements ArtworkQuestionStrategy {

    @Override
    public QuestionType getQuestionType() {
        return QuestionType.ARTWORK_MEDIUM;
    }

    @Override
    public boolean isEligiblePair(
            Artwork artworkOne,
            Artwork artworkTwo,
            int roundNumber
    ) {
        return selectCategory(artworkOne, artworkTwo) != null;
    }

    @Override
    public Artwork getCorrectArtwork(
            Artwork artworkOne,
            Artwork artworkTwo
    ) {
        ArtworkMediumCategory category = requiredCategory(
                artworkOne,
                artworkTwo
        );

        return category.matches(artworkOne)
                ? artworkOne
                : artworkTwo;
    }

    @Override
    public String getQuestionParameter(
            Artwork artworkOne,
            Artwork artworkTwo,
            int roundNumber
    ) {
        return requiredCategory(artworkOne, artworkTwo).name();
    }

    private ArtworkMediumCategory requiredCategory(
            Artwork artworkOne,
            Artwork artworkTwo
    ) {
        ArtworkMediumCategory category = selectCategory(
                artworkOne,
                artworkTwo
        );

        if (category == null) {
            throw new IllegalArgumentException(
                    "The artworks require one exclusive medium category"
            );
        }

        return category;
    }

    private ArtworkMediumCategory selectCategory(
            Artwork artworkOne,
            Artwork artworkTwo
    ) {
        List<ArtworkMediumCategory> categories = Arrays.stream(
                        ArtworkMediumCategory.values()
                )
                .filter(category -> category.matches(artworkOne)
                        != category.matches(artworkTwo))
                .sorted(Comparator.comparing(
                        ArtworkMediumCategory::getDisplayName
                ))
                .toList();

        return categories.isEmpty() ? null : categories.getFirst();
    }
}
