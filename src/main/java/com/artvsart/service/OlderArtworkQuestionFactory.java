package com.artvsart.service;

import com.artvsart.model.Artwork;
import com.artvsart.model.ArtworkQuestion;
import com.artvsart.model.GameRun;
import com.artvsart.model.QuestionType;
import com.artvsart.repository.ArtworkQuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class OlderArtworkQuestionFactory {

    private final ArtworkService artworkService;
    private final OlderArtworkQuestionService olderArtworkService;
    private final ArtworkQuestionRepository questionRepository;

    public OlderArtworkQuestionFactory(
            ArtworkService artworkService,
            OlderArtworkQuestionService olderArtworkService,
            ArtworkQuestionRepository questionRepository
    ) {
        this.artworkService = artworkService;
        this.olderArtworkService = olderArtworkService;
        this.questionRepository = questionRepository;
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

        DifficultyBand difficulty =
                difficultyForRound(
                        run.getRoundNumber()
                );

        List<ArtworkPair> eligiblePairs =
                createEligiblePairs(
                        artworkService.getPlayableArtworks(),
                        difficulty
                );

        if (eligiblePairs.isEmpty()) {
            throw new IllegalStateException(
                    "No artwork pairs are available for the current difficulty"
            );
        }

        ArtworkPair selectedPair = eligiblePairs.get(
                ThreadLocalRandom.current().nextInt(
                        eligiblePairs.size()
                )
        );

        Artwork correctArtwork =
                olderArtworkService.getCorrectArtwork(
                        selectedPair.artworkOne(),
                        selectedPair.artworkTwo()
                );

        ArtworkQuestion question =
                ArtworkQuestion.forRun(
                        run,
                        run.getRoundNumber(),
                        QuestionType.OLDER_ARTWORK,
                        selectedPair.artworkOne(),
                        selectedPair.artworkTwo(),
                        correctArtwork
                );

        return questionRepository.save(question);
    }

    private List<ArtworkPair> createEligiblePairs(
            List<Artwork> artworks,
            DifficultyBand difficulty
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

                if (!olderArtworkService.isEligiblePair(
                        artworkOne,
                        artworkTwo
                )) {
                    continue;
                }

                long difference = Math.abs(
                        (long) artworkOne.getObjectBeginYear()
                                - artworkTwo.getObjectBeginYear()
                );

                if (difficulty.includes(difference)) {
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

    private DifficultyBand difficultyForRound(
            int roundNumber
    ) {
        if (roundNumber <= 5) {
            return new DifficultyBand(
                    250,
                    Long.MAX_VALUE
            );
        }

        if (roundNumber <= 10) {
            return new DifficultyBand(
                    150,
                    249
            );
        }

        if (roundNumber <= 15) {
            return new DifficultyBand(
                    75,
                    149
            );
        }

        return new DifficultyBand(
                OlderArtworkQuestionService
                        .MINIMUM_YEAR_DIFFERENCE,
                74
        );
    }

    private record ArtworkPair(
            Artwork artworkOne,
            Artwork artworkTwo
    ) {
    }

    private record DifficultyBand(
            long minimumDifference,
            long maximumDifference
    ) {

        private boolean includes(long difference) {
            return difference >= minimumDifference
                    && difference <= maximumDifference;
        }
    }
}