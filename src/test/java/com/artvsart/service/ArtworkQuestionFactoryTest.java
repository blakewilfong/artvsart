package com.artvsart.service;

import com.artvsart.model.Artwork;
import com.artvsart.model.ArtworkQuestion;
import com.artvsart.model.GameMode;
import com.artvsart.model.GameRun;
import com.artvsart.model.QuestionType;
import com.artvsart.repository.ArtworkQuestionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArtworkQuestionFactoryTest {

    @Mock
    private ArtworkService artworkService;

    @Mock
    private ArtworkQuestionRepository questionRepository;

    @Test
    void returnsExistingQuestionForCurrentRound() {
        GameRun run = run(
                10L,
                3,
                GameMode.STREAK
        );

        ArtworkQuestion existingQuestion =
                mock(ArtworkQuestion.class);

        when(questionRepository
                .findByGameRunIdAndRoundNumber(
                        10L,
                        3
                ))
                .thenReturn(
                        Optional.of(existingQuestion)
                );

        ArtworkQuestionFactory factory =
                new ArtworkQuestionFactory(
                        artworkService,
                        questionRepository,
                        List.of()
                );

        ArtworkQuestion result =
                factory.getOrCreateForRun(run);

        assertSame(existingQuestion, result);
        verifyNoInteractions(artworkService);
    }

    @Test
    void createsQuestionUsingEligibleStrategy() {
        GameRun run = run(
                10L,
                1,
                GameMode.STREAK
        );

        Artwork artworkOne =
                artwork(1L);

        Artwork artworkTwo =
                artwork(2L);

        ArtworkQuestionStrategy strategy =
                mock(ArtworkQuestionStrategy.class);

        when(questionRepository
                .findByGameRunIdAndRoundNumber(
                        10L,
                        1
                ))
                .thenReturn(Optional.empty());

        when(artworkService.getBalancedQuestionCandidates(240))
                .thenReturn(
                        List.of(
                                artworkOne,
                                artworkTwo
                        )
                );

        when(strategy.isEligiblePair(
                artworkOne,
                artworkTwo,
                1
        )).thenReturn(true);

        when(strategy.getCorrectArtwork(
                artworkOne,
                artworkTwo
        )).thenReturn(artworkOne);

        when(strategy.getQuestionType())
                .thenReturn(
                        QuestionType.ARTIST_NATIONALITY
                );

        when(strategy.getQuestionParameter(
                artworkOne,
                artworkTwo,
                1
        )).thenReturn("French");

        when(questionRepository.save(
                any(ArtworkQuestion.class)
        )).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        ArtworkQuestionFactory factory =
                new ArtworkQuestionFactory(
                        artworkService,
                        questionRepository,
                        List.of(strategy)
                );

        ArtworkQuestion question =
                factory.getOrCreateForRun(run);

        assertSame(
                QuestionType.ARTIST_NATIONALITY,
                question.getQuestionType()
        );

        assertEquals("French", question.getQuestionParameter());

        assertSame(
                artworkOne,
                question.getCorrectArtwork()
        );

        assertSame(
                run,
                question.getGameRun()
        );
    }

    @Test
    void usesAvailableStrategyWhenAnotherHasNoPairs() {
        GameRun run = run(
                10L,
                1,
                GameMode.WAGER
        );

        Artwork artworkOne =
                artwork(1L);

        Artwork artworkTwo =
                artwork(2L);

        ArtworkQuestionStrategy unavailableStrategy =
                mock(ArtworkQuestionStrategy.class);

        ArtworkQuestionStrategy availableStrategy =
                mock(ArtworkQuestionStrategy.class);

        when(questionRepository
                .findByGameRunIdAndRoundNumber(
                        10L,
                        1
                ))
                .thenReturn(Optional.empty());

        when(artworkService.getBalancedQuestionCandidates(240))
                .thenReturn(
                        List.of(
                                artworkOne,
                                artworkTwo
                        )
                );

        lenient().when(
                unavailableStrategy.isEligiblePair(
                        artworkOne,
                        artworkTwo,
                        1
                )
        ).thenReturn(false);

        when(availableStrategy.isEligiblePair(
                artworkOne,
                artworkTwo,
                1
        )).thenReturn(true);

        when(availableStrategy.getCorrectArtwork(
                artworkOne,
                artworkTwo
        )).thenReturn(artworkTwo);

        when(availableStrategy.getQuestionType())
                .thenReturn(
                        QuestionType.ARTIST_BORN_EARLIER
                );

        when(questionRepository.save(
                any(ArtworkQuestion.class)
        )).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        ArtworkQuestionFactory factory =
                new ArtworkQuestionFactory(
                        artworkService,
                        questionRepository,
                        List.of(
                                unavailableStrategy,
                                availableStrategy
                        )
                );

        ArtworkQuestion question =
                factory.getOrCreateForRun(run);

        assertSame(
                QuestionType.ARTIST_BORN_EARLIER,
                question.getQuestionType()
        );

        assertSame(
                artworkTwo,
                question.getCorrectArtwork()
        );
    }

    private GameRun run(
            Long id,
            int roundNumber,
            GameMode gameMode
    ) {
        GameRun run = mock(GameRun.class);

        when(run.getId()).thenReturn(id);
        when(run.isActive()).thenReturn(true);
        when(run.getRoundNumber())
                .thenReturn(roundNumber);
        lenient().when(run.getGameMode())
                .thenReturn(gameMode);

        return run;
    }

    private Artwork artwork(Long id) {
        Artwork artwork = mock(Artwork.class);

        when(artwork.getId()).thenReturn(id);

        return artwork;
    }
}
