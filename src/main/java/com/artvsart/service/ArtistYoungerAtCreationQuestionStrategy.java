package com.artvsart.service;

import com.artvsart.model.Artwork;
import com.artvsart.model.QuestionType;
import org.springframework.stereotype.Component;

@Component
public class ArtistYoungerAtCreationQuestionStrategy
        implements ArtworkQuestionStrategy {

    private final ArtistYoungerAtCreationQuestionService
            questionService;

    public ArtistYoungerAtCreationQuestionStrategy(
            ArtistYoungerAtCreationQuestionService questionService
    ) {
        this.questionService = questionService;
    }

    @Override
    public QuestionType getQuestionType() {
        return QuestionType.ARTIST_YOUNGER_AT_CREATION;
    }

    @Override
    public boolean isEligiblePair(
            Artwork artworkOne,
            Artwork artworkTwo,
            int roundNumber
    ) {
        if (!questionService.isEligiblePair(
                artworkOne,
                artworkTwo
        )) {
            return false;
        }

        int firstAge =
                questionService.getArtistAgeAtCreation(
                        artworkOne
                );

        int secondAge =
                questionService.getArtistAgeAtCreation(
                        artworkTwo
                );

        long difference = Math.abs(
                (long) firstAge - secondAge
        );

        if (roundNumber <= 5) {
            return difference >= 30;
        }

        if (roundNumber <= 10) {
            return difference >= 20
                    && difference <= 29;
        }

        if (roundNumber <= 15) {
            return difference >= 10
                    && difference <= 19;
        }

        return difference
                >= ArtistYoungerAtCreationQuestionService
                .MINIMUM_AGE_DIFFERENCE
                && difference <= 9;
    }

    @Override
    public Artwork getCorrectArtwork(
            Artwork artworkOne,
            Artwork artworkTwo
    ) {
        return questionService.getCorrectArtwork(
                artworkOne,
                artworkTwo
        );
    }
}