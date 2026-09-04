package com.artvsart.service;

import com.artvsart.model.ArtworkAnswer;
import com.artvsart.model.ArtworkQuestion;
import com.artvsart.model.GameRun;
import com.artvsart.repository.GameRunRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArtworkQuestionRerollServiceTest {

    private static final String VOTER_ID = "voter-1";

    @Mock
    private ArtworkAnswerService answerService;

    @Mock
    private ArtworkQuestionFactory questionFactory;

    @Mock
    private GameRunRepository gameRunRepository;

    @Test
    void spendsTokenAndReplacesCurrentUnansweredQuestion() {
        ArtworkQuestion question = mock(ArtworkQuestion.class);
        ArtworkQuestion replacement = mock(ArtworkQuestion.class);
        GameRun run = mock(GameRun.class);
        ArtworkQuestionRerollService service = service();

        when(question.getId()).thenReturn(10L);
        when(question.getGameRun()).thenReturn(run);
        when(question.getRoundNumber()).thenReturn(4);
        when(run.isActive()).thenReturn(true);
        when(run.getRoundNumber()).thenReturn(4);
        when(answerService.findAnswer(10L, VOTER_ID))
                .thenReturn(Optional.empty());
        when(questionFactory.reroll(question))
                .thenReturn(replacement);

        ArtworkQuestion result = service.reroll(
                question,
                VOTER_ID
        );

        assertSame(replacement, result);
        verify(run).spendReroll();
        verify(gameRunRepository).save(run);
    }

    @Test
    void rejectsAnsweredQuestionWithoutSpendingToken() {
        ArtworkQuestion question = mock(ArtworkQuestion.class);
        ArtworkQuestionRerollService service = service();

        when(question.getId()).thenReturn(10L);
        when(answerService.findAnswer(10L, VOTER_ID))
                .thenReturn(Optional.of(
                        mock(ArtworkAnswer.class)
                ));

        assertThrows(
                IllegalStateException.class,
                () -> service.reroll(question, VOTER_ID)
        );

        verify(questionFactory, never()).reroll(question);
        verifyNoInteractions(gameRunRepository);
    }

    private ArtworkQuestionRerollService service() {
        return new ArtworkQuestionRerollService(
                answerService,
                questionFactory,
                gameRunRepository
        );
    }
}
