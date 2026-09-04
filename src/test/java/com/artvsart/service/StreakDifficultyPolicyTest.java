package com.artvsart.service;

import com.artvsart.model.QuestionType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StreakDifficultyPolicyTest {

    private final StreakDifficultyPolicy policy =
            new StreakDifficultyPolicy();

    @Test
    void increasesDifficultyAfterEveryRound() {
        assertEquals(
                0.03,
                policy.getDifficultyForRound(1),
                0.0001
        );
        assertEquals(
                0.06,
                policy.getDifficultyForRound(2),
                0.0001
        );
        assertEquals(
                0.09,
                policy.getDifficultyForRound(3),
                0.0001
        );
    }

    @Test
    void capsDifficultyAtOne() {
        assertEquals(
                1.0,
                policy.getDifficultyForRound(100),
                0.0001
        );
    }

    @Test
    void startsWithAHeavyBiasTowardSimpleQuestions() {
        int olderArtworkWeight = policy.getQuestionTypeWeight(
                QuestionType.OLDER_ARTWORK,
                1
        );
        int historicalEventWeight = policy.getQuestionTypeWeight(
                QuestionType.BEFORE_HISTORICAL_EVENT,
                1
        );
        int styleWeight = policy.getQuestionTypeWeight(
                QuestionType.ARTWORK_STYLE,
                1
        );

        assertEquals(100, olderArtworkWeight);
        assertEquals(3, historicalEventWeight);
        assertEquals(1, styleWeight);
    }

    @Test
    void broadensTheQuestionMixEachRound() {
        int firstRoundWeight = policy.getQuestionTypeWeight(
                QuestionType.ARTWORK_STYLE,
                1
        );
        int secondRoundWeight = policy.getQuestionTypeWeight(
                QuestionType.ARTWORK_STYLE,
                2
        );

        assertTrue(secondRoundWeight > firstRoundWeight);
        assertEquals(
                100,
                policy.getQuestionTypeWeight(
                        QuestionType.ARTWORK_STYLE,
                        100
                )
        );
    }

    @Test
    void shiftsEligibleArtworkDateGapsEveryRound() {
        assertFalse(
                policy.isArtworkYearDifferenceEligible(
                        331,
                        1
                )
        );
        assertTrue(
                policy.isArtworkYearDifferenceEligible(
                        331,
                        2
                )
        );
        assertTrue(
                policy.isArtworkYearDifferenceEligible(
                        350,
                        1
                )
        );
        assertFalse(
                policy.isArtworkYearDifferenceEligible(
                        350,
                        10
                )
        );
    }
}
