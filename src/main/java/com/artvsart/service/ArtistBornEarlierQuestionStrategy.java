package com.artvsart.service;

import com.artvsart.model.Artwork;
import com.artvsart.model.QuestionType;
import org.springframework.stereotype.Component;

@Component
public class ArtistBornEarlierQuestionStrategy
        implements ArtworkQuestionStrategy {

    private final ArtistBornEarlierQuestionService questionService;

    public ArtistBornEarlierQuestionStrategy(
            ArtistBornEarlierQuestionService questionService
    ) {
        this.questionService = questionService;
    }

    @Override
    public QuestionType getQuestionType() {
        return QuestionType.ARTIST_BORN_EARLIER;
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

        long difference = Math.abs(
                (long) artworkOne.getArtistBeginYear()
                        - artworkTwo.getArtistBeginYear()
        );

        if (roundNumber <= 5) {
            return difference >= 75;
        }

        if (roundNumber <= 10) {
            return difference >= 40
                    && difference <= 74;
        }

        if (roundNumber <= 15) {
            return difference >= 15
                    && difference <= 39;
        }

        return difference
                >= ArtistBornEarlierQuestionService
                .MINIMUM_BIRTH_YEAR_DIFFERENCE
                && difference <= 14;
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