package com.artvsart.service;

import com.artvsart.dto.ArtworkStats;
import com.artvsart.model.Artwork;
import com.artvsart.model.Matchup;
import com.artvsart.model.PredictionOutcome;
import com.artvsart.model.Vote;
import com.artvsart.repository.VoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class VoteService {

    private final MatchupService matchupService;
    private final VoteRepository voteRepository;
    private final ArtworkStatisticsService statisticsService;

    public VoteService(
            MatchupService matchupService,
            VoteRepository voteRepository,
            ArtworkStatisticsService statisticsService
    ) {
        this.matchupService = matchupService;
        this.voteRepository = voteRepository;
        this.statisticsService = statisticsService;
    }

    @Transactional
    public Vote castVote(
            Long matchupId,
            Long selectedArtworkId,
            String voterId
    ) {
        if (voterId == null || voterId.isBlank()) {
            throw new IllegalArgumentException(
                    "A voter ID is required"
            );
        }

        Matchup matchup = matchupService
                .getTodaysMatchupById(matchupId);

        Artwork selectedArtwork = resolveSelectedArtwork(
                matchup,
                selectedArtworkId
        );

        Optional<Vote> existingVote = voteRepository
                .findByMatchupIdAndVoterId(
                        matchupId,
                        voterId
                );

        if (existingVote.isPresent()) {
            return existingVote.get();
        }

        Artwork nonSelectedArtwork = resolveNonSelectedArtwork(
                matchup,
                selectedArtwork
        );

        ArtworkStats selectedStats = statisticsService
                .getStats(selectedArtwork)
                .afterSelection();

        ArtworkStats nonSelectedStats = statisticsService
                .getStats(nonSelectedArtwork)
                .afterNonSelection();

        PredictionOutcome outcome = determineOutcome(
                selectedStats,
                nonSelectedStats
        );

        Vote vote = new Vote(
                matchup,
                selectedArtwork,
                voterId,
                outcome
        );

        return voteRepository.save(vote);
    }

    @Transactional(readOnly = true)
    public Optional<Vote> findVote(
            Long matchupId,
            String voterId
    ) {
        if (voterId == null || voterId.isBlank()) {
            return Optional.empty();
        }

        return voteRepository.findByMatchupIdAndVoterId(
                matchupId,
                voterId
        );
    }

    private PredictionOutcome determineOutcome(
            ArtworkStats selectedStats,
            ArtworkStats nonSelectedStats
    ) {
        int comparison = Double.compare(
                selectedStats.selectionPercentage(),
                nonSelectedStats.selectionPercentage()
        );

        if (comparison >= 0) {
            return PredictionOutcome.CORRECT;
        }

        return PredictionOutcome.INCORRECT;
    }

    private Artwork resolveSelectedArtwork(
            Matchup matchup,
            Long selectedArtworkId
    ) {
        if (matchup.getArtworkOne().getId()
                .equals(selectedArtworkId)) {
            return matchup.getArtworkOne();
        }

        if (matchup.getArtworkTwo().getId()
                .equals(selectedArtworkId)) {
            return matchup.getArtworkTwo();
        }

        throw new IllegalArgumentException(
                "Selected artwork is not part of this matchup"
        );
    }

    private Artwork resolveNonSelectedArtwork(
            Matchup matchup,
            Artwork selectedArtwork
    ) {
        if (matchup.getArtworkOne().getId()
                .equals(selectedArtwork.getId())) {
            return matchup.getArtworkTwo();
        }

        return matchup.getArtworkOne();
    }
}