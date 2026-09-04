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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WagerGameServiceTest {

    private static final String VOTER_ID = "voter-1";

    @Mock
    private ArtworkQuestionRepository questionRepository;

    @Mock
    private GameRunRepository gameRunRepository;

    @Mock
    private ArtworkAnswerService answerService;

    @Mock
    private ArtworkQuestionFactory questionFactory;

    @Mock
    private ArtworkQuestionRerollService rerollService;

    private WagerGameService service;

    @BeforeEach
    void setUp() {
        service = new WagerGameService(
                questionRepository,
                gameRunRepository,
                answerService,
                questionFactory,
                rerollService
        );
    }

    @Test
    void startsNewWagerRunAndCreatesQuestion() {
        GameRun run =
                GameRun.startWager(VOTER_ID);

        ArtworkQuestion question =
                mock(ArtworkQuestion.class);

        when(gameRunRepository
                .findFirstByVoterIdAndGameModeAndActiveTrueOrderByStartedAtDesc(
                        VOTER_ID,
                        GameMode.WAGER
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
    void resumesExistingActiveWagerRun() {
        GameRun run =
                GameRun.startWager(VOTER_ID);

        ArtworkQuestion question =
                mock(ArtworkQuestion.class);

        when(gameRunRepository
                .findFirstByVoterIdAndGameModeAndActiveTrueOrderByStartedAtDesc(
                        VOTER_ID,
                        GameMode.WAGER
                ))
                .thenReturn(Optional.of(run));

        when(questionFactory.getOrCreateForRun(run))
                .thenReturn(question);

        ArtworkQuestion result =
                service.startOrResume(VOTER_ID);

        assertSame(question, result);
    }

    @Test
    void recordsAnswerAndAppliesWagerToRun() {
        ArtworkQuestion question =
                mock(ArtworkQuestion.class);

        ArtworkAnswer answer =
                mock(ArtworkAnswer.class);

        GameRun run =
                mock(GameRun.class);

        when(questionRepository.findById(10L))
                .thenReturn(Optional.of(question));

        when(question.getGameMode())
                .thenReturn(GameMode.WAGER);

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

        when(answerService.answerWagerQuestion(
                question,
                1L,
                VOTER_ID,
                20
        )).thenReturn(answer);

        when(answer.isCorrect())
                .thenReturn(true);

        when(answer.getWagerAmount())
                .thenReturn(20);

        ArtworkAnswer result =
                service.answerQuestion(
                        10L,
                        1L,
                        20,
                        VOTER_ID
                );

        assertSame(answer, result);

        verify(run).recordWagerAnswer(
                true,
                20
        );

        verify(gameRunRepository).save(run);
    }

    @Test
    void returnsExistingAnswerWithoutApplyingWagerAgain() {
        ArtworkQuestion question =
                mock(ArtworkQuestion.class);

        ArtworkAnswer existingAnswer =
                mock(ArtworkAnswer.class);

        GameRun run =
                mock(GameRun.class);

        when(questionRepository.findById(10L))
                .thenReturn(Optional.of(question));

        when(question.getGameMode())
                .thenReturn(GameMode.WAGER);

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
                        20,
                        VOTER_ID
                );

        assertSame(existingAnswer, result);
    }

    @Test
    void returnsGlobalHighestBankroll() {
        when(gameRunRepository
                .findHighestPointBalanceByGameMode(
                        GameMode.WAGER
                ))
                .thenReturn(425);

        int highScore =
                service.getGlobalHighScore();

        assertEquals(425, highScore);
    }

    @Test
    void rerollsOwnedWagerQuestion() {
        ArtworkQuestion question = mock(ArtworkQuestion.class);
        ArtworkQuestion replacement = mock(ArtworkQuestion.class);
        GameRun run = mock(GameRun.class);

        when(questionRepository.findById(10L))
                .thenReturn(Optional.of(question));
        when(question.getGameMode()).thenReturn(GameMode.WAGER);
        when(question.belongsToRun()).thenReturn(true);
        when(question.getGameRun()).thenReturn(run);
        when(run.getVoterId()).thenReturn(VOTER_ID);
        when(rerollService.reroll(question, VOTER_ID))
                .thenReturn(replacement);

        ArtworkQuestion result = service.rerollQuestion(
                10L,
                VOTER_ID
        );

        assertSame(replacement, result);
    }
}
