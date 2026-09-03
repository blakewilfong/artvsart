package com.artvsart.service;

import com.artvsart.model.ArtworkAnswer;
import com.artvsart.model.ArtworkQuestion;
import com.artvsart.model.GameMode;
import com.artvsart.model.GameRun;
import com.artvsart.repository.ArtworkQuestionRepository;
import com.artvsart.repository.GameRunRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class StreakGameService {

    private final ArtworkQuestionRepository questionRepository;
    private final GameRunRepository gameRunRepository;
    private final ArtworkAnswerService answerService;
    private final ArtworkQuestionFactory questionFactory;

    public StreakGameService(
            ArtworkQuestionRepository questionRepository,
            GameRunRepository gameRunRepository,
            ArtworkAnswerService answerService,
            ArtworkQuestionFactory questionFactory
    ) {
        this.questionRepository = questionRepository;
        this.gameRunRepository = gameRunRepository;
        this.answerService = answerService;
        this.questionFactory = questionFactory;
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

        return questionFactory.getOrCreateForRun(run);
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

    private void validateVoterId(String voterId) {
        if (voterId == null || voterId.isBlank()) {
            throw new IllegalArgumentException(
                    "A voter ID is required"
            );
        }
    }
}