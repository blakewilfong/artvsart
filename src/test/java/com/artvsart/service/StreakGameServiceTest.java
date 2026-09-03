package com.artvsart.service;

import com.artvsart.model.ArtworkAnswer;
import com.artvsart.model.ArtworkQuestion;
import com.artvsart.model.GameMode;
import com.artvsart.model.GameRun;
import com.artvsart.repository.ArtworkQuestionRepository;
import com.artvsart.repository.GameRunRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StreakGameServiceTest {

    private static final String VOTER_ID = "voter-1";

    @Mock
    private ArtworkQuestionRepository questionRepository;

    @Mock
    private GameRunRepository gameRunRepository;

    @Mock
    private ArtworkAnswerService answerService;

    @Mock
    private ArtworkQuestionFactory questionFactory;

    private StreakGameService service;

    @BeforeEach
    void setUp() {
        service = new StreakGameService(
                questionRepository,
                gameRunRepository,
                answerService,
                questionFactory
        );
    }

    @Test
    void startsNewRunAndCreatesQuestion() {
        GameRun run =
                GameRun.startStreak(VOTER_ID);

        ArtworkQuestion question =
                mock(ArtworkQuestion.class);

        when(gameRunRepository
                .findFirstByVoterIdAndGameModeAndActiveTrueOrderByStartedAtDesc(
                        VOTER_ID,
                        GameMode.STREAK
                ))
                .thenReturn(Optional.empty());

        when(gameRunRepository.save(
                any(GameRun.class)
        )).thenReturn(run);

        when(questionFactory.getOrCreateForRun(run))
                .thenReturn(question);

        ArtworkQuestion result =
                service.startOrResume(VOTER_ID);

        assertSame(question, result);
    }

    @Test
    void resumesExistingActiveRun() {
        GameRun run =
                GameRun.startStreak(VOTER_ID);

        ArtworkQuestion question =
                mock(ArtworkQuestion.class);

        when(gameRunRepository
                .findFirstByVoterIdAndGameModeAndActiveTrueOrderByStartedAtDesc(
                        VOTER_ID,
                        GameMode.STREAK
                ))
                .thenReturn(Optional.of(run));

        when(questionFactory.getOrCreateForRun(run))
                .thenReturn(question);

        ArtworkQuestion result =
                service.startOrResume(VOTER_ID);

        assertSame(question, result);
    }

    @Test
    void correctAnswerAdvancesStreakRun() {
        ArtworkQuestion question =
                mock(ArtworkQuestion.class);

        ArtworkAnswer answer =
                mock(ArtworkAnswer.class);

        GameRun run =
                mock(GameRun.class);

        when(questionRepository.findById(10L))
                .thenReturn(Optional.of(question));

        when(question.getGameMode())
                .thenReturn(GameMode.STREAK);

        when(question.belongsToRun())
                .thenReturn(true);

        when(question.getGameRun())
                .thenReturn(run);

        when(question.getRoundNumber())
                .thenReturn(1);

        when(run.getVoterId())
                .thenReturn(VOTER_ID);

        when(run.isActive())
                .thenReturn(true);

        when(run.getRoundNumber())
                .thenReturn(1);

        when(answerService.findAnswer(
                10L,
                VOTER_ID
        )).thenReturn(Optional.empty());

        when(answerService.answerQuestion(
                question,
                1L,
                VOTER_ID
        )).thenReturn(answer);

        when(answer.isCorrect())
                .thenReturn(true);

        ArtworkAnswer result =
                service.answerQuestion(
                        10L,
                        1L,
                        VOTER_ID
                );

        assertSame(answer, result);

        verify(run).recordStreakAnswer(true);
        verify(gameRunRepository).save(run);
    }

    @Test
    void existingAnswerDoesNotAdvanceRunAgain() {
        ArtworkQuestion question =
                mock(ArtworkQuestion.class);

        ArtworkAnswer existingAnswer =
                mock(ArtworkAnswer.class);

        GameRun run =
                mock(GameRun.class);

        when(questionRepository.findById(10L))
                .thenReturn(Optional.of(question));

        when(question.getGameMode())
                .thenReturn(GameMode.STREAK);

        when(question.belongsToRun())
                .thenReturn(true);

        when(question.getGameRun())
                .thenReturn(run);

        when(run.getVoterId())
                .thenReturn(VOTER_ID);

        when(answerService.findAnswer(
                10L,
                VOTER_ID
        )).thenReturn(Optional.of(existingAnswer));

        ArtworkAnswer result =
                service.answerQuestion(
                        10L,
                        1L,
                        VOTER_ID
                );

        assertSame(existingAnswer, result);
    }
}