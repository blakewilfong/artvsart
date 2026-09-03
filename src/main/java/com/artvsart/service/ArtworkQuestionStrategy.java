package com.artvsart.service;

import com.artvsart.model.Artwork;
import com.artvsart.model.QuestionType;

public interface ArtworkQuestionStrategy {

    QuestionType getQuestionType();

    boolean isEligiblePair(
            Artwork artworkOne,
            Artwork artworkTwo,
            int roundNumber
    );

    Artwork getCorrectArtwork(
            Artwork artworkOne,
            Artwork artworkTwo
    );

    default String getQuestionParameter(
            Artwork artworkOne,
            Artwork artworkTwo,
            int roundNumber
    ) {
        return null;
    }
}
