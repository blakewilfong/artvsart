package com.artvsart.service;

import com.artvsart.model.Artwork;
import com.artvsart.model.ArtworkAnswer;
import com.artvsart.model.ArtworkQuestion;
import com.artvsart.model.GameMode;
import com.artvsart.model.GameRun;
import com.artvsart.model.QuestionType;
import com.artvsart.repository.ArtworkQuestionRepository;
import com.artvsart.repository.GameRunRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StreakGameServiceTest {

    private static final String VOTER_ID = "voter-1";

    @Mock
    private ArtworkService artworkService;

    @Mock
    private OlderArtworkQuestionService olderArtworkService;

    @Mock
    private ArtworkQuestionRepository questionRepository;

    @Mock
    private GameRunRepository gameRunRepository;

    @Mock
    private ArtworkAnswerService answerService;

    private StreakGameService service;

    @BeforeEach
    void setUp() {
        service = new StreakGameService(
                artworkService,
                olderArtworkService,
                questionRepository,
                gameRunRepository,
                answerService
        );
    }

    @Test
    void startsRunWithEasyOlderArtworkQuestion() {
        Artwork older = artwork(
                1L,
                1500
        );

        Artwork newer = artwork(
                2L,
                1800
        );

        when(gameRunRepository
                .findFirstByVoterIdAndGameModeAndActiveTrueOrderByStartedAtDesc(
                        VOTER_ID,
                        GameMode.STREAK
                ))
                .thenReturn(Optional.empty());

        when(gameRunRepository.save(
                any(GameRun.class)
        )).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        when(questionRepository
                .findByGameRunIdAndRoundNumber(
                        null,
                        1
                ))
                .thenReturn(Optional.empty());

        when(artworkService.getPlayableArtworks())
                .thenReturn(List.of(older, newer));

        when(olderArtworkService.isEligiblePair(
                older,
                newer
        )).thenReturn(true);

        when(olderArtworkService.getCorrectArtwork(
                older,
                newer
        )).thenReturn(older);

        when(questionRepository.save(
                any(ArtworkQuestion.class)
        )).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        ArtworkQuestion question =
                service.startOrResume(VOTER_ID);

        assertEquals(
                GameMode.STREAK,
                question.getGameMode()
        );

        assertEquals(
                QuestionType.OLDER_ARTWORK,
                question.getQuestionType()
        );

        assertSame(
                older,
                question.getCorrectArtwork()
        );

        assertTrue(question.belongsToRun());

        assertEquals(
                1,
                question.getRoundNumber()
        );

        assertEquals(
                0,
                question.getGameRun().getCorrectAnswers()
        );
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
    void returnsExistingAnswerWithoutAdvancingRunAgain() {
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

    private Artwork artwork(
            Long id,
            Integer year
    ) {
        Artwork artwork = mock(Artwork.class);

        when(artwork.getId()).thenReturn(id);
        when(artwork.getObjectBeginYear())
                .thenReturn(year);

        return artwork;
    }
}