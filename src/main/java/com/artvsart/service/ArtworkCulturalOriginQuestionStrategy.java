package com.artvsart.service;

import com.artvsart.model.Artwork;
import com.artvsart.model.ArtworkCulturalOrigin;
import com.artvsart.model.QuestionType;

import java.util.Arrays;

/**
 * Retained only so previously stored culture questions remain compatible.
 * This strategy is deliberately not registered for question generation.
 */
public class ArtworkCulturalOriginQuestionStrategy
        implements ArtworkQuestionStrategy {

    @Override
    public QuestionType getQuestionType() {
        return QuestionType.ARTWORK_CULTURE;
    }

    @Override
    public boolean isEligiblePair(
            Artwork artworkOne,
            Artwork artworkTwo,
            int roundNumber
    ) {
        return selectCulturalOrigin(
                artworkOne,
                artworkTwo
        ) != null;
    }

    @Override
    public Artwork getCorrectArtwork(
            Artwork artworkOne,
            Artwork artworkTwo
    ) {
        ArtworkCulturalOrigin origin = requiredCulturalOrigin(
                artworkOne,
                artworkTwo
        );

        return origin.matches(artworkOne)
                ? artworkOne
                : artworkTwo;
    }

    @Override
    public boolean usesArtistPopularityDifficulty() {
        return true;
    }

    @Override
    public String getQuestionParameter(
            Artwork artworkOne,
            Artwork artworkTwo,
            int roundNumber
    ) {
        return requiredCulturalOrigin(
                artworkOne,
                artworkTwo
        ).toParameter();
    }

    private ArtworkCulturalOrigin requiredCulturalOrigin(
            Artwork artworkOne,
            Artwork artworkTwo
    ) {
        ArtworkCulturalOrigin origin = selectCulturalOrigin(
                artworkOne,
                artworkTwo
        );

        if (origin == null) {
            throw new IllegalArgumentException(
                    "Two different known cultural origins are required"
            );
        }

        return origin;
    }

    private ArtworkCulturalOrigin selectCulturalOrigin(
            Artwork artworkOne,
            Artwork artworkTwo
    ) {
        return Arrays.stream(ArtworkCulturalOrigin.Kind.values())
                .map(kind -> selectForKind(
                        artworkOne,
                        artworkTwo,
                        kind
                ))
                .filter(origin -> origin != null)
                .findFirst()
                .orElse(null);
    }

    private ArtworkCulturalOrigin selectForKind(
            Artwork artworkOne,
            Artwork artworkTwo,
            ArtworkCulturalOrigin.Kind kind
    ) {
        ArtworkCulturalOrigin first = ArtworkCulturalOrigin
                .find(artworkOne, kind)
                .orElse(null);
        ArtworkCulturalOrigin second = ArtworkCulturalOrigin
                .find(artworkTwo, kind)
                .orElse(null);

        if (first == null || second == null
                || first.displayName().equalsIgnoreCase(
                second.displayName()
        )) {
            return null;
        }

        return first.displayName().compareToIgnoreCase(
                second.displayName()
        ) <= 0
                ? first
                : second;
    }
}
