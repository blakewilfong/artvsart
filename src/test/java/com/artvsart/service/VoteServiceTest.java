package com.artvsart.service;

import com.artvsart.dto.ArtworkStats;
import com.artvsart.model.Artwork;
import com.artvsart.model.Matchup;
import com.artvsart.model.PredictionOutcome;
import com.artvsart.model.Vote;
import com.artvsart.repository.VoteRepository;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VoteServiceTest {

    private static final Long MATCHUP_ID = 10L;
    private static final Long ARTWORK_ONE_ID = 1L;
    private static final Long ARTWORK_TWO_ID = 2L;
    private static final String VOTER_ID = "voter-1";

    @Mock
    private VoteRepository voteRepository;

    @Mock
    private ArtworkStatisticsService statisticsService;

    private VoteService voteService;
    private Artwork artworkOne;
    private Artwork artworkTwo;
    private Matchup matchup;

    @BeforeEach
    void setUp() {
        voteService = new VoteService(
                voteRepository,
                statisticsService
        );

        artworkOne = mock(Artwork.class);
        artworkTwo = mock(Artwork.class);
        matchup = mock(Matchup.class);

        when(artworkOne.getId()).thenReturn(ARTWORK_ONE_ID);

        when(matchup.getId()).thenReturn(MATCHUP_ID);
        when(matchup.getArtworkOne()).thenReturn(artworkOne);
        when(matchup.getArtworkTwo()).thenReturn(artworkTwo);

        when(voteRepository.findByMatchupIdAndVoterId(
                MATCHUP_ID,
                VOTER_ID
        )).thenReturn(Optional.empty());

        when(voteRepository.save(any(Vote.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void storesCorrectWhenSelectedArtworkFinishesWithHigherRate() {
        when(statisticsService.getStats(artworkOne))
                .thenReturn(new ArtworkStats(6, 10));

        when(statisticsService.getStats(artworkTwo))
                .thenReturn(new ArtworkStats(4, 10));

        Vote vote = voteService.castVote(
                matchup,
                ARTWORK_ONE_ID,
                VOTER_ID
        );

        assertSame(artworkOne, vote.getSelectedArtwork());
        assertEquals(
                PredictionOutcome.CORRECT,
                vote.getOutcome()
        );
    }

    @Test
    void storesIncorrectWhenSelectedArtworkFinishesWithLowerRate() {
        when(artworkTwo.getId()).thenReturn(ARTWORK_TWO_ID);

        when(statisticsService.getStats(artworkTwo))
                .thenReturn(new ArtworkStats(3, 10));

        when(statisticsService.getStats(artworkOne))
                .thenReturn(new ArtworkStats(6, 10));

        Vote vote = voteService.castVote(
                matchup,
                ARTWORK_TWO_ID,
                VOTER_ID
        );

        assertSame(artworkTwo, vote.getSelectedArtwork());
        assertEquals(
                PredictionOutcome.INCORRECT,
                vote.getOutcome()
        );
    }

    @Test
    void storesCorrectWhenBothArtworksFinishWithSameRate() {
        when(statisticsService.getStats(artworkOne))
                .thenReturn(new ArtworkStats(4, 10));

        when(statisticsService.getStats(artworkTwo))
                .thenReturn(new ArtworkStats(5, 10));

        Vote vote = voteService.castVote(
                matchup,
                ARTWORK_ONE_ID,
                VOTER_ID
        );

        assertSame(artworkOne, vote.getSelectedArtwork());
        assertEquals(
                PredictionOutcome.CORRECT,
                vote.getOutcome()
        );
    }
}