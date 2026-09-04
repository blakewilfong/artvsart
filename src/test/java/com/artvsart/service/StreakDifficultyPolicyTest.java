package com.artvsart.service;

import com.artvsart.model.Artwork;
import com.artvsart.model.ArtworkQuestion;
import com.artvsart.model.QuestionType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        int centuryWeight = policy.getQuestionTypeWeight(
                QuestionType.ARTWORK_CENTURY,
                1
        );
        int styleWeight = policy.getQuestionTypeWeight(
                QuestionType.ARTWORK_STYLE,
                1
        );

        assertEquals(100, olderArtworkWeight);
        assertTrue(centuryWeight > historicalEventWeight);
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

    @Test
    void movesFromPopularArtistsToObscureArtists() {
        Artwork veryPopular = artworkWithPopularityRank(5);
        Artwork popular = artworkWithPopularityRank(10);
        Artwork obscure = artworkWithPopularityRank(80);

        assertTrue(policy.isArtistPopularityEligible(
                veryPopular,
                popular,
                1
        ));
        assertFalse(policy.isArtistPopularityEligible(
                veryPopular,
                obscure,
                1
        ));
        assertTrue(policy.isArtistPopularityEligible(
                veryPopular,
                obscure,
                27
        ));
        assertFalse(policy.isArtistPopularityEligible(
                veryPopular,
                popular,
                27
        ));
    }

    @Test
    void excludesThePreviousQuestionType() {
        ArtworkQuestionStrategy older = strategy(
                QuestionType.OLDER_ARTWORK
        );
        ArtworkQuestionStrategy medium = strategy(
                QuestionType.ARTWORK_MEDIUM
        );

        List<ArtworkQuestionStrategy> ordered =
                policy.orderStrategies(
                        List.of(older, medium),
                        List.of(question(
                                QuestionType.OLDER_ARTWORK
                        )),
                        2
                );

        assertEquals(1, ordered.size());
        assertSame(medium, ordered.getFirst());
    }

    @Test
    void putsLeastUsedQuestionTypesFirst() {
        ArtworkQuestionStrategy older = strategy(
                QuestionType.OLDER_ARTWORK
        );
        ArtworkQuestionStrategy nationality = strategy(
                QuestionType.ARTIST_NATIONALITY
        );

        List<ArtworkQuestionStrategy> ordered =
                policy.orderStrategies(
                        List.of(older, nationality),
                        List.of(
                                question(QuestionType.OLDER_ARTWORK),
                                question(QuestionType.OLDER_ARTWORK),
                                question(QuestionType.ARTWORK_STYLE)
                        ),
                        4
                );

        assertSame(nationality, ordered.getFirst());
        assertSame(older, ordered.getLast());
    }

    private ArtworkQuestionStrategy strategy(
            QuestionType questionType
    ) {
        ArtworkQuestionStrategy strategy =
                mock(ArtworkQuestionStrategy.class);

        when(strategy.getQuestionType()).thenReturn(questionType);

        return strategy;
    }

    private Artwork artworkWithPopularityRank(int rank) {
        Artwork artwork = mock(Artwork.class);
        when(artwork.getArtistPopularityRank()).thenReturn(rank);
        return artwork;
    }

    private ArtworkQuestion question(QuestionType questionType) {
        ArtworkQuestion question = mock(ArtworkQuestion.class);

        when(question.getQuestionType()).thenReturn(questionType);

        return question;
    }
}
