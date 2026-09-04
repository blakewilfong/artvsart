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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
                        List.of(),
                        new StreakDifficultyPolicy()
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
                run
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
                        List.of(strategy),
                        new StreakDifficultyPolicy()
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
    void usesArtistPopularityForCategoricalStreakQuestions() {
        GameRun run = run(10L, 1, GameMode.STREAK);
        Artwork popularOne = artwork(1L);
        Artwork popularTwo = artwork(2L);
        Artwork obscure = mock(Artwork.class);
        ArtworkQuestionStrategy strategy =
                mock(ArtworkQuestionStrategy.class);

        when(popularOne.getArtistPopularityRank()).thenReturn(5);
        when(popularTwo.getArtistPopularityRank()).thenReturn(10);
        when(obscure.getArtistPopularityRank()).thenReturn(90);
        when(questionRepository
                .findByGameRunIdAndRoundNumber(10L, 1))
                .thenReturn(Optional.empty());
        when(questionRepository
                .findAllByGameRunIdOrderByRoundNumberAsc(10L))
                .thenReturn(List.of());
        when(artworkService.getBalancedQuestionCandidates(240))
                .thenReturn(List.of(
                        popularOne,
                        popularTwo,
                        obscure
                ));
        when(strategy.getQuestionType())
                .thenReturn(QuestionType.ARTWORK_MEDIUM);
        when(strategy.usesArtistPopularityDifficulty())
                .thenReturn(true);
        when(strategy.isEligiblePair(popularOne, popularTwo, run))
                .thenReturn(true);
        when(strategy.isEligiblePair(popularOne, obscure, run))
                .thenReturn(true);
        when(strategy.isEligiblePair(popularTwo, obscure, run))
                .thenReturn(true);
        when(strategy.getCorrectArtwork(popularOne, popularTwo))
                .thenReturn(popularOne);
        when(strategy.getQuestionParameter(
                popularOne,
                popularTwo,
                1
        )).thenReturn("OIL");
        when(questionRepository.save(any(ArtworkQuestion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ArtworkQuestionFactory factory =
                new ArtworkQuestionFactory(
                        artworkService,
                        questionRepository,
                        List.of(strategy),
                        new StreakDifficultyPolicy()
                );

        ArtworkQuestion question = factory.getOrCreateForRun(run);

        assertSame(popularOne, question.getArtworkOne());
        assertSame(popularTwo, question.getArtworkTwo());
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
                        run
                )
        ).thenReturn(false);

        when(availableStrategy.isEligiblePair(
                artworkOne,
                artworkTwo,
                run
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
                        ),
                        new StreakDifficultyPolicy()
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

    @Test
    void rerollUsesADifferentQuestionTypeForTheSameRound() {
        GameRun run = run(10L, 4, GameMode.WAGER);
        Artwork artworkOne = artwork(1L);
        Artwork artworkTwo = artwork(2L);
        ArtworkQuestion currentQuestion =
                mock(ArtworkQuestion.class);
        ArtworkQuestionStrategy currentStrategy =
                mock(ArtworkQuestionStrategy.class);
        ArtworkQuestionStrategy replacementStrategy =
                mock(ArtworkQuestionStrategy.class);

        when(currentQuestion.belongsToRun()).thenReturn(true);
        when(currentQuestion.getGameRun()).thenReturn(run);
        when(currentQuestion.getRoundNumber()).thenReturn(4);
        when(currentQuestion.getQuestionType())
                .thenReturn(QuestionType.OLDER_ARTWORK);
        when(currentStrategy.getQuestionType())
                .thenReturn(QuestionType.OLDER_ARTWORK);
        when(replacementStrategy.getQuestionType())
                .thenReturn(QuestionType.ARTWORK_MEDIUM);
        when(artworkService.getBalancedQuestionCandidates(240))
                .thenReturn(List.of(artworkOne, artworkTwo));
        when(replacementStrategy.isEligiblePair(
                artworkOne,
                artworkTwo,
                run
        )).thenReturn(true);
        when(replacementStrategy.getCorrectArtwork(
                artworkOne,
                artworkTwo
        )).thenReturn(artworkOne);
        when(replacementStrategy.getQuestionParameter(
                artworkOne,
                artworkTwo,
                4
        )).thenReturn("Oil");
        when(questionRepository.save(any(ArtworkQuestion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ArtworkQuestionFactory factory =
                new ArtworkQuestionFactory(
                        artworkService,
                        questionRepository,
                        List.of(
                                currentStrategy,
                                replacementStrategy
                        ),
                        new StreakDifficultyPolicy()
                );

        ArtworkQuestion replacement = factory.reroll(
                currentQuestion
        );

        assertSame(
                QuestionType.ARTWORK_MEDIUM,
                replacement.getQuestionType()
        );
        assertEquals(4, replacement.getRoundNumber());
        verify(questionRepository).delete(currentQuestion);
        verify(questionRepository).flush();
        verify(currentStrategy, never()).isEligiblePair(
                artworkOne,
                artworkTwo,
                run
        );
    }

    private GameRun run(
            Long id,
            int roundNumber,
            GameMode gameMode
    ) {
        GameRun run = mock(GameRun.class);

        lenient().when(run.getId()).thenReturn(id);
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
