package com.artvsart.service;

import com.artvsart.model.Artwork;
import com.artvsart.model.ArtworkQuestion;
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

    private final ArtworkService artworkService;
    private final ArtworkQuestionRepository questionRepository;
    private final List<ArtworkQuestionStrategy> strategies;

    public ArtworkQuestionFactory(
            ArtworkService artworkService,
            ArtworkQuestionRepository questionRepository,
            List<ArtworkQuestionStrategy> strategies
    ) {
        this.artworkService = artworkService;
        this.questionRepository = questionRepository;
        this.strategies = List.copyOf(strategies);
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
                artworkService.getPlayableArtworks();

        List<ArtworkQuestionStrategy> shuffledStrategies =
                new ArrayList<>(strategies);

        Collections.shuffle(
                shuffledStrategies,
                ThreadLocalRandom.current()
        );

        for (ArtworkQuestionStrategy strategy
                : shuffledStrategies) {

            List<ArtworkPair> eligiblePairs =
                    createEligiblePairs(
                            artworks,
                            strategy,
                            run.getRoundNumber()
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
            int roundNumber
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
                        roundNumber
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

    private record ArtworkPair(
            Artwork artworkOne,
            Artwork artworkTwo
    ) {
    }
}
