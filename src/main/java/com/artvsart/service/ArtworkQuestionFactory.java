package com.artvsart.service;

import com.artvsart.model.Artwork;
import com.artvsart.model.ArtworkQuestion;
import com.artvsart.model.GameMode;
import com.artvsart.model.GameRun;
import com.artvsart.repository.ArtworkQuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ArtworkQuestionFactory {

    private static final int MAXIMUM_QUESTION_CANDIDATES = 240;

    private final ArtworkService artworkService;
    private final ArtworkQuestionRepository questionRepository;
    private final List<ArtworkQuestionStrategy> strategies;
    private final StreakDifficultyPolicy streakDifficultyPolicy;

    public ArtworkQuestionFactory(
            ArtworkService artworkService,
            ArtworkQuestionRepository questionRepository,
            List<ArtworkQuestionStrategy> strategies,
            StreakDifficultyPolicy streakDifficultyPolicy
    ) {
        this.artworkService = artworkService;
        this.questionRepository = questionRepository;
        this.strategies = List.copyOf(strategies);
        this.streakDifficultyPolicy = streakDifficultyPolicy;
    }

    @Transactional
    public ArtworkQuestion getOrCreateForRun(
            GameRun run
    ) {
        if (run == null || run.getId() == null) {
            throw new IllegalArgumentException(
                    "A saved game run is required"
            );
        }

        if (!run.isActive()) {
            throw new IllegalStateException(
                    "Cannot create a question for a completed run"
            );
        }

        Optional<ArtworkQuestion> existingQuestion =
                questionRepository
                        .findByGameRunIdAndRoundNumber(
                                run.getId(),
                                run.getRoundNumber()
                        );

        if (existingQuestion.isPresent()) {
            return existingQuestion.get();
        }

        if (strategies.isEmpty()) {
            throw new IllegalStateException(
                    "No artwork question strategies are available"
            );
        }

        List<Artwork> artworks =
                artworkService.getBalancedQuestionCandidates(
                        MAXIMUM_QUESTION_CANDIDATES
                );

        List<ArtworkQuestionStrategy> shuffledStrategies =
                new ArrayList<>(strategies);

        if (run.getGameMode() == GameMode.STREAK) {
            applyStreakDifficultyWeights(
                    shuffledStrategies,
                    run.getRoundNumber()
            );
        } else {
            Collections.shuffle(
                    shuffledStrategies,
                    ThreadLocalRandom.current()
            );
        }

        for (ArtworkQuestionStrategy strategy
                : shuffledStrategies) {

            List<ArtworkPair> eligiblePairs =
                    createEligiblePairs(
                            artworks,
                            strategy,
                            run
                    );

            if (eligiblePairs.isEmpty()) {
                continue;
            }

            ArtworkPair selectedPair = eligiblePairs.get(
                    ThreadLocalRandom.current().nextInt(
                            eligiblePairs.size()
                    )
            );

            Artwork correctArtwork =
                    strategy.getCorrectArtwork(
                            selectedPair.artworkOne(),
                            selectedPair.artworkTwo()
                    );

            String questionParameter =
                    strategy.getQuestionParameter(
                            selectedPair.artworkOne(),
                            selectedPair.artworkTwo(),
                            run.getRoundNumber()
                    );

            ArtworkQuestion question =
                    ArtworkQuestion.forRun(
                            run,
                            run.getRoundNumber(),
                            strategy.getQuestionType(),
                            selectedPair.artworkOne(),
                            selectedPair.artworkTwo(),
                            correctArtwork,
                            questionParameter
                    );

            return questionRepository.save(question);
        }

        throw new IllegalStateException(
                "No artwork pairs are available for the current difficulty"
        );
    }

    private List<ArtworkPair> createEligiblePairs(
            List<Artwork> artworks,
            ArtworkQuestionStrategy strategy,
            GameRun run
    ) {
        List<ArtworkPair> eligiblePairs =
                new ArrayList<>();

        for (int firstIndex = 0;
             firstIndex < artworks.size();
             firstIndex++) {

            for (int secondIndex = firstIndex + 1;
                 secondIndex < artworks.size();
                 secondIndex++) {

                Artwork artworkOne =
                        artworks.get(firstIndex);

                Artwork artworkTwo =
                        artworks.get(secondIndex);

                if (strategy.isEligiblePair(
                        artworkOne,
                        artworkTwo,
                        run
                )) {
                    eligiblePairs.add(
                            new ArtworkPair(
                                    artworkOne,
                                    artworkTwo
                            )
                    );
                }
            }
        }

        return eligiblePairs;
    }

    private void applyStreakDifficultyWeights(
            List<ArtworkQuestionStrategy> strategies,
            int roundNumber
    ) {
        List<ArtworkQuestionStrategy> remaining =
                new ArrayList<>(strategies);

        strategies.clear();

        while (!remaining.isEmpty()) {
            int totalWeight = remaining.stream()
                    .mapToInt(strategy -> streakDifficultyPolicy
                            .getQuestionTypeWeight(
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

                ticket -= streakDifficultyPolicy
                        .getQuestionTypeWeight(
                                strategy.getQuestionType(),
                                roundNumber
                        );

                if (ticket < 0) {
                    strategies.add(strategy);
                    remaining.remove(index);
                    break;
                }
            }
        }
    }

    private record ArtworkPair(
            Artwork artworkOne,
            Artwork artworkTwo
    ) {
    }
}
