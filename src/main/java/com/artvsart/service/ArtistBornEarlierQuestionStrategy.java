package com.artvsart.service;

import com.artvsart.model.Artwork;
import com.artvsart.model.GameMode;
import com.artvsart.model.GameRun;
import com.artvsart.model.QuestionType;
import org.springframework.stereotype.Component;

@Component
public class ArtistBornEarlierQuestionStrategy
        implements ArtworkQuestionStrategy {

    private final ArtistBornEarlierQuestionService questionService;
    private final StreakDifficultyPolicy streakDifficultyPolicy;

    public ArtistBornEarlierQuestionStrategy(
            ArtistBornEarlierQuestionService questionService,
            StreakDifficultyPolicy streakDifficultyPolicy
    ) {
        this.questionService = questionService;
        this.streakDifficultyPolicy = streakDifficultyPolicy;
    }

    @Override
    public boolean isEligiblePair(
            Artwork artworkOne,
            Artwork artworkTwo,
            GameRun run
    ) {
        if (run.getGameMode() != GameMode.STREAK) {
            return isEligiblePair(
                    artworkOne,
                    artworkTwo,
                    run.getRoundNumber()
            );
        }

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

        return streakDifficultyPolicy
                .isArtistBirthYearDifferenceEligible(
                        difference,
                        run.getRoundNumber()
                );
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
