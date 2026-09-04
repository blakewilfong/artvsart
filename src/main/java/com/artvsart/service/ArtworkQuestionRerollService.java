package com.artvsart.service;

import com.artvsart.model.ArtworkQuestion;
import com.artvsart.model.GameRun;
import com.artvsart.repository.GameRunRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ArtworkQuestionRerollService {

    private final ArtworkAnswerService answerService;
    private final ArtworkQuestionFactory questionFactory;
    private final GameRunRepository gameRunRepository;

    public ArtworkQuestionRerollService(
            ArtworkAnswerService answerService,
            ArtworkQuestionFactory questionFactory,
            GameRunRepository gameRunRepository
    ) {
        this.answerService = answerService;
        this.questionFactory = questionFactory;
        this.gameRunRepository = gameRunRepository;
    }

    @Transactional
    public ArtworkQuestion reroll(
            ArtworkQuestion question,
            String voterId
    ) {
        if (answerService.findAnswer(
                question.getId(),
                voterId
        ).isPresent()) {
            throw new IllegalStateException(
                    "An answered question cannot be rerolled"
            );
        }

        GameRun run = question.getGameRun();

        if (!run.isActive()
                || question.getRoundNumber()
                != run.getRoundNumber()) {
            throw new IllegalStateException(
                    "Only the current question can be rerolled"
            );
        }

        run.spendReroll();
        gameRunRepository.save(run);

        return questionFactory.reroll(question);
    }
}
