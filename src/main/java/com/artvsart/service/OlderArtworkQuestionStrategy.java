package com.artvsart.service;

import com.artvsart.model.Artwork;
import com.artvsart.model.QuestionType;
import org.springframework.stereotype.Component;

@Component
public class OlderArtworkQuestionStrategy
        implements ArtworkQuestionStrategy {

    private final OlderArtworkQuestionService questionService;

    public OlderArtworkQuestionStrategy(
            OlderArtworkQuestionService questionService
    ) {
        this.questionService = questionService;
    }

    @Override
    public QuestionType getQuestionType() {
        return QuestionType.OLDER_ARTWORK;
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
                (long) artworkOne.getObjectBeginYear()
                        - artworkTwo.getObjectBeginYear()
        );

        if (roundNumber <= 5) {
            return difference >= 250;
        }

        if (roundNumber <= 10) {
            return difference >= 150
                    && difference <= 249;
        }

        if (roundNumber <= 15) {
            return difference >= 75
                    && difference <= 149;
        }

        return difference
                >= OlderArtworkQuestionService
                .MINIMUM_YEAR_DIFFERENCE
                && difference <= 74;
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