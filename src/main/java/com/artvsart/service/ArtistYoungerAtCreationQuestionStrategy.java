package com.artvsart.service;

import com.artvsart.model.Artwork;
import com.artvsart.model.GameMode;
import com.artvsart.model.GameRun;
import com.artvsart.model.QuestionType;
import org.springframework.stereotype.Component;

@Component
public class ArtistYoungerAtCreationQuestionStrategy
        implements ArtworkQuestionStrategy {

    static final int FIRST_AVAILABLE_ROUND = 16;

    private final ArtistYoungerAtCreationQuestionService
            questionService;
    private final StreakDifficultyPolicy streakDifficultyPolicy;

    public ArtistYoungerAtCreationQuestionStrategy(
            ArtistYoungerAtCreationQuestionService questionService,
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
        if (run.getRoundNumber() < FIRST_AVAILABLE_ROUND) {
            return false;
        }

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
                (long) questionService.getArtistAgeAtCreation(
                        artworkOne
                ) - questionService.getArtistAgeAtCreation(
                        artworkTwo
                )
        );

        return streakDifficultyPolicy
                .isArtistAgeDifferenceEligible(
                        difference,
                        run.getRoundNumber()
                );
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
        if (roundNumber < FIRST_AVAILABLE_ROUND) {
            return false;
        }

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
