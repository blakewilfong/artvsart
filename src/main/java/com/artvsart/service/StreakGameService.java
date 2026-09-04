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
    private final GameRunLifecycleService runLifecycleService;
    private final ArtworkAnswerService answerService;
    private final ArtworkQuestionFactory questionFactory;
    private final LeaderboardService leaderboardService;
    private final StreakLeaderboardScoreProvider scoreProvider;
    private final ArtworkQuestionRerollService rerollService;

    public StreakGameService(
            ArtworkQuestionRepository questionRepository,
            GameRunRepository gameRunRepository,
            GameRunLifecycleService runLifecycleService,
            ArtworkAnswerService answerService,
            ArtworkQuestionFactory questionFactory,
            LeaderboardService leaderboardService,
            StreakLeaderboardScoreProvider scoreProvider,
            ArtworkQuestionRerollService rerollService
    ) {
        this.questionRepository = questionRepository;
        this.gameRunRepository = gameRunRepository;
        this.runLifecycleService = runLifecycleService;
        this.answerService = answerService;
        this.questionFactory = questionFactory;
        this.leaderboardService = leaderboardService;
        this.scoreProvider = scoreProvider;
        this.rerollService = rerollService;
    }

    @Transactional
    public ArtworkQuestion startNew(String voterId) {
        validateVoterId(voterId);

        GameRun run = runLifecycleService.startNew(
                voterId,
                GameMode.STREAK
        );

        return questionFactory.getOrCreateForRun(run);
    }

    @Transactional
    public ArtworkQuestion continueRun(String voterId) {
        validateVoterId(voterId);

        GameRun run = runLifecycleService.resumeOrStart(
                voterId,
                GameMode.STREAK
        );

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

        if (!answer.isCorrect()) {
            leaderboardService.recordCompletedRun(
                    run,
                    scoreProvider.getScore(run)
            );
        }

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
        return leaderboardService.getAllTimeHighScore(
                GameMode.STREAK
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
    public int getDailyHighScore() {
        return leaderboardService.getDailyHighScore(
                GameMode.STREAK
        );
    }

    @Transactional(readOnly = true)
    public LeaderboardView getLeaderboard(
            Long runId,
            String voterId
    ) {
        validateVoterId(voterId);
        return leaderboardService.getView(
                GameMode.STREAK,
                voterId,
                runId
        );
    }

    @Transactional
    public void nameScore(
            Long runId,
            String voterId,
            String displayName
    ) {
        validateVoterId(voterId);
        leaderboardService.nameScore(
                GameMode.STREAK,
                runId,
                voterId,
                displayName
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
