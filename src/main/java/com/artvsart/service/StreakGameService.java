package com.artvsart.service;

import com.artvsart.model.Artwork;
import com.artvsart.model.ArtworkAnswer;
import com.artvsart.model.ArtworkQuestion;
import com.artvsart.model.GameMode;
import com.artvsart.model.GameRun;
import com.artvsart.model.QuestionType;
import com.artvsart.repository.ArtworkQuestionRepository;
import com.artvsart.repository.GameRunRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class StreakGameService {

    private final ArtworkService artworkService;
    private final OlderArtworkQuestionService olderArtworkService;
    private final ArtworkQuestionRepository questionRepository;
    private final GameRunRepository gameRunRepository;
    private final ArtworkAnswerService answerService;

    public StreakGameService(
            ArtworkService artworkService,
            OlderArtworkQuestionService olderArtworkService,
            ArtworkQuestionRepository questionRepository,
            GameRunRepository gameRunRepository,
            ArtworkAnswerService answerService
    ) {
        this.artworkService = artworkService;
        this.olderArtworkService = olderArtworkService;
        this.questionRepository = questionRepository;
        this.gameRunRepository = gameRunRepository;
        this.answerService = answerService;
    }

    @Transactional
    public ArtworkQuestion startOrResume(String voterId) {
        validateVoterId(voterId);

        GameRun run = gameRunRepository
                .findFirstByVoterIdAndGameModeAndActiveTrueOrderByStartedAtDesc(
                        voterId,
                        GameMode.STREAK
                )
                .orElseGet(() -> gameRunRepository.save(
                        GameRun.startStreak(voterId)
                ));

        return getOrCreateCurrentQuestion(run);
    }

    @Transactional(readOnly = true)
    public ArtworkQuestion getQuestion(
            Long questionId,
            String voterId
    ) {
        validateVoterId(voterId);

        ArtworkQuestion question = questionRepository
                .findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Question does not exist"
                ));

        if (question.getGameMode() != GameMode.STREAK
                || !question.belongsToRun()) {
            throw new IllegalArgumentException(
                    "Question is not a Streak Mode question"
            );
        }

        if (!question.getGameRun()
                .getVoterId()
                .equals(voterId)) {
            throw new IllegalArgumentException(
                    "Question does not belong to this player"
            );
        }

        return question;
    }

    @Transactional
    public ArtworkAnswer answerQuestion(
            Long questionId,
            Long selectedArtworkId,
            String voterId
    ) {
        ArtworkQuestion question =
                getQuestion(questionId, voterId);

        Optional<ArtworkAnswer> existingAnswer =
                answerService.findAnswer(
                        questionId,
                        voterId
                );

        if (existingAnswer.isPresent()) {
            return existingAnswer.get();
        }

        GameRun run = question.getGameRun();

        if (!run.isActive()) {
            throw new IllegalStateException(
                    "Streak run is already complete"
            );
        }

        if (question.getRoundNumber()
                != run.getRoundNumber()) {
            throw new IllegalStateException(
                    "Question is not the current round"
            );
        }

        ArtworkAnswer answer =
                answerService.answerQuestion(
                        question,
                        selectedArtworkId,
                        voterId
                );

        run.recordStreakAnswer(
                answer.isCorrect()
        );

        gameRunRepository.save(run);

        return answer;
    }

    @Transactional(readOnly = true)
    public Optional<ArtworkAnswer> findAnswer(
            Long questionId,
            String voterId
    ) {
        return answerService.findAnswer(
                questionId,
                voterId
        );
    }

    @Transactional(readOnly = true)
    public int getGlobalHighScore() {
        return gameRunRepository.findHighScoreByGameMode(
                GameMode.STREAK
        );
    }

    private ArtworkQuestion getOrCreateCurrentQuestion(
            GameRun run
    ) {
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
                difficultyFor(run.getCorrectAnswers());

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

                long difference = yearDifference(
                        artworkOne,
                        artworkTwo
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

    private long yearDifference(
            Artwork artworkOne,
            Artwork artworkTwo
    ) {
        return Math.abs(
                (long) artworkOne.getObjectBeginYear()
                        - artworkTwo.getObjectBeginYear()
        );
    }

    private DifficultyBand difficultyFor(
            int correctAnswers
    ) {
        if (correctAnswers < 5) {
            return new DifficultyBand(
                    250,
                    Long.MAX_VALUE
            );
        }

        if (correctAnswers < 10) {
            return new DifficultyBand(
                    150,
                    249
            );
        }

        if (correctAnswers < 15) {
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

    private void validateVoterId(String voterId) {
        if (voterId == null || voterId.isBlank()) {
            throw new IllegalArgumentException(
                    "A voter ID is required"
            );
        }
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