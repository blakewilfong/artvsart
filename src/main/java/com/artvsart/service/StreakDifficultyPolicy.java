package com.artvsart.service;

import com.artvsart.model.ArtworkQuestion;
import com.artvsart.model.QuestionType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

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

    public List<ArtworkQuestionStrategy> orderStrategies(
            List<ArtworkQuestionStrategy> strategies,
            List<ArtworkQuestion> previousQuestions,
            int roundNumber
    ) {
        if (strategies == null || previousQuestions == null) {
            throw new IllegalArgumentException(
                    "Strategies and previous questions are required"
            );
        }

        Map<QuestionType, Integer> usageCounts =
                new EnumMap<>(QuestionType.class);

        previousQuestions.forEach(question -> usageCounts.merge(
                question.getQuestionType(),
                1,
                Integer::sum
        ));

        QuestionType previousType = previousQuestions.isEmpty()
                ? null
                : previousQuestions.getLast().getQuestionType();

        Map<Integer, List<ArtworkQuestionStrategy>> usageGroups =
                new TreeMap<>();

        strategies.stream()
                .filter(strategy -> strategy.getQuestionType()
                        != previousType)
                .forEach(strategy -> usageGroups.computeIfAbsent(
                                usageCounts.getOrDefault(
                                        strategy.getQuestionType(),
                                        0
                                ),
                                ignored -> new ArrayList<>()
                        )
                        .add(strategy));

        List<ArtworkQuestionStrategy> ordered =
                new ArrayList<>();

        usageGroups.values().forEach(group -> ordered.addAll(
                weightedOrder(group, roundNumber)
        ));

        return List.copyOf(ordered);
    }

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
            case ARTIST_LAST_NAME -> 0.1;
            case ARTWORK_CENTURY -> 0.2;
            case ARTIST_BORN_EARLIER -> 0.25;
            case ARTWORK_TITLE_WORD -> 0.35;
            case ARTWORK_MEDIUM -> 0.5;
            case ARTWORK_CULTURE -> 0.6;
            case ARTIST_NATIONALITY -> 0.65;
            case ARTIST_YOUNGER_AT_CREATION -> 0.8;
            case ARTIST_ALIVE_DURING_EVENT -> 0.9;
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

    private List<ArtworkQuestionStrategy> weightedOrder(
            List<ArtworkQuestionStrategy> strategies,
            int roundNumber
    ) {
        List<ArtworkQuestionStrategy> remaining =
                new ArrayList<>(strategies);

        List<ArtworkQuestionStrategy> ordered =
                new ArrayList<>();

        while (!remaining.isEmpty()) {
            int totalWeight = remaining.stream()
                    .mapToInt(strategy -> getQuestionTypeWeight(
                            strategy.getQuestionType(),
                            roundNumber
                    ))
                    .sum();

            int ticket = ThreadLocalRandom.current()
                    .nextInt(totalWeight);

            for (int index = 0;
                 index < remaining.size();
                 index++) {
                ArtworkQuestionStrategy strategy =
                        remaining.get(index);

                ticket -= getQuestionTypeWeight(
                        strategy.getQuestionType(),
                        roundNumber
                );

                if (ticket < 0) {
                    ordered.add(strategy);
                    remaining.remove(index);
                    break;
                }
            }
        }

        return ordered;
    }
}
