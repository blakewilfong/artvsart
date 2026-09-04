package com.artvsart.service;

import com.artvsart.model.QuestionType;
import org.springframework.stereotype.Component;

@Component
public class StreakDifficultyPolicy {

    private static final double STARTING_DIFFICULTY = 0.03;
    private static final double DIFFICULTY_INCREASE_PER_ROUND = 0.03;
    private static final double PAIR_DIFFICULTY_TOLERANCE = 0.14;

    private static final long EASY_ARTWORK_YEAR_DIFFERENCE = 400;
    private static final long HARD_ARTWORK_YEAR_DIFFERENCE = 25;
    private static final long EASY_ARTIST_BIRTH_YEAR_DIFFERENCE = 100;
    private static final long HARD_ARTIST_BIRTH_YEAR_DIFFERENCE = 5;
    private static final long EASY_ARTIST_AGE_DIFFERENCE = 40;
    private static final long HARD_ARTIST_AGE_DIFFERENCE = 5;

    public int getQuestionTypeWeight(
            QuestionType questionType,
            int roundNumber
    ) {
        if (questionType == null) {
            throw new IllegalArgumentException(
                    "A question type is required"
            );
        }

        double exponent = switch (questionType) {
            case OLDER_ARTWORK -> 0.0;
            case ARTIST_BORN_EARLIER -> 0.25;
            case ARTWORK_MEDIUM -> 0.5;
            case ARTIST_NATIONALITY -> 0.65;
            case ARTIST_YOUNGER_AT_CREATION -> 0.8;
            case BEFORE_HISTORICAL_EVENT -> 1.0;
            case ARTWORK_STYLE -> 1.2;
            default -> 1.0;
        };

        return Math.max(
                1,
                (int) Math.round(
                        100 * Math.pow(
                                getDifficultyForRound(roundNumber),
                                exponent
                        )
                )
        );
    }

    public boolean isArtworkYearDifferenceEligible(
            long difference,
            int roundNumber
    ) {
        return isDifferenceEligible(
                difference,
                roundNumber,
                EASY_ARTWORK_YEAR_DIFFERENCE,
                HARD_ARTWORK_YEAR_DIFFERENCE
        );
    }

    public boolean isArtistBirthYearDifferenceEligible(
            long difference,
            int roundNumber
    ) {
        return isDifferenceEligible(
                difference,
                roundNumber,
                EASY_ARTIST_BIRTH_YEAR_DIFFERENCE,
                HARD_ARTIST_BIRTH_YEAR_DIFFERENCE
        );
    }

    public boolean isArtistAgeDifferenceEligible(
            long difference,
            int roundNumber
    ) {
        return isDifferenceEligible(
                difference,
                roundNumber,
                EASY_ARTIST_AGE_DIFFERENCE,
                HARD_ARTIST_AGE_DIFFERENCE
        );
    }

    public double getDifficultyForRound(int roundNumber) {
        if (roundNumber < 1) {
            throw new IllegalArgumentException(
                    "Round number must be positive"
            );
        }

        return Math.min(
                1.0,
                STARTING_DIFFICULTY
                        + (roundNumber - 1)
                        * DIFFICULTY_INCREASE_PER_ROUND
        );
    }

    private boolean isDifferenceEligible(
            long difference,
            int roundNumber,
            long easyDifference,
            long hardDifference
    ) {
        if (difference < hardDifference) {
            return false;
        }

        double pairDifficulty = Math.clamp(
                (double) (easyDifference - difference)
                        / (easyDifference - hardDifference),
                0.0,
                1.0
        );

        return Math.abs(
                pairDifficulty
                        - getDifficultyForRound(roundNumber)
        ) <= PAIR_DIFFICULTY_TOLERANCE;
    }
}
