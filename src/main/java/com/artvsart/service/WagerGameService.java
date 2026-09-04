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
public class WagerGameService {

    private final ArtworkQuestionRepository questionRepository;
    private final GameRunRepository gameRunRepository;
    private final ArtworkAnswerService answerService;
    private final ArtworkQuestionFactory questionFactory;
    private final ArtworkQuestionRerollService rerollService;

    public WagerGameService(
            ArtworkQuestionRepository questionRepository,
            GameRunRepository gameRunRepository,
            ArtworkAnswerService answerService,
            ArtworkQuestionFactory questionFactory,
            ArtworkQuestionRerollService rerollService
    ) {
        this.questionRepository = questionRepository;
        this.gameRunRepository = gameRunRepository;
        this.answerService = answerService;
        this.questionFactory = questionFactory;
        this.rerollService = rerollService;
    }

    @Transactional
    public ArtworkQuestion startOrResume(String voterId) {
        validateVoterId(voterId);

        GameRun run = gameRunRepository
                .findFirstByVoterIdAndGameModeAndActiveTrueOrderByStartedAtDesc(
                        voterId,
                        GameMode.WAGER
                )
                .orElseGet(() -> gameRunRepository.save(
                        GameRun.startWager(voterId)
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

        if (question.getGameMode() != GameMode.WAGER
                || !question.belongsToRun()) {
            throw new IllegalArgumentException(
                    "Question is not a Wager Mode question"
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
            int wagerAmount,
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
                    "Wager run is already complete"
            );
        }

        if (question.getRoundNumber()
                != run.getRoundNumber()) {
            throw new IllegalStateException(
                    "Question is not the current round"
            );
        }

        ArtworkAnswer answer =
                answerService.answerWagerQuestion(
                        question,
                        selectedArtworkId,
                        voterId,
                        wagerAmount
                );

        run.recordWagerAnswer(
                answer.isCorrect(),
                answer.getWagerAmount()
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

    @Transactional
    public ArtworkQuestion rerollQuestion(
            Long questionId,
            String voterId
    ) {
        return rerollService.reroll(
                getQuestion(questionId, voterId),
                voterId
        );
    }

    @Transactional(readOnly = true)
    public int getGlobalHighScore() {
        return gameRunRepository
                .findHighestPointBalanceByGameMode(
                        GameMode.WAGER
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
