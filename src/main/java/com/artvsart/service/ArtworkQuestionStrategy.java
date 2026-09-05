package com.artvsart.service;

import com.artvsart.model.Artwork;
import com.artvsart.model.GameRun;
import com.artvsart.model.QuestionType;

public interface ArtworkQuestionStrategy {

    QuestionType getQuestionType();

    boolean isEligiblePair(
            Artwork artworkOne,
            Artwork artworkTwo,
            int roundNumber
    );

    default boolean isEligiblePair(
            Artwork artworkOne,
            Artwork artworkTwo,
            GameRun run
    ) {
        if (run == null) {
            throw new IllegalArgumentException(
                    "A game run is required"
            );
        }

        return isEligiblePair(
                artworkOne,
                artworkTwo,
                run.getRoundNumber()
        );
    }

    Artwork getCorrectArtwork(
            Artwork artworkOne,
            Artwork artworkTwo
    );

    default boolean usesArtistPopularityDifficulty() {
        return false;
    }

    default Artwork getCorrectArtwork(Artwork artworkOne, Artwork artworkTwo,
                                      int roundNumber) {
        return getCorrectArtwork(artworkOne, artworkTwo);
    }

    default String getQuestionParameter(
            Artwork artworkOne,
            Artwork artworkTwo,
            int roundNumber
    ) {
        return null;
    }
}
