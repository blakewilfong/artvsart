package com.artvsart.service;

import com.artvsart.model.Artwork;
import com.artvsart.model.Matchup;
import com.artvsart.model.Vote;
import com.artvsart.repository.VoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VoteService {

    private final MatchupService matchupService;
    private final VoteRepository voteRepository;

    public VoteService(
            MatchupService matchupService,
            VoteRepository voteRepository
    ) {
        this.matchupService = matchupService;
        this.voteRepository = voteRepository;
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

        return voteRepository
                .findByMatchupIdAndVoterId(matchupId, voterId)
                .orElseGet(() -> voteRepository.save(
                        new Vote(matchup, selectedArtwork, voterId)
                ));
    }

    @Transactional(readOnly = true)
    public boolean hasVoted(
            Long matchupId,
            String voterId
    ) {
        if (voterId == null || voterId.isBlank()) {
            return false;
        }

        return voteRepository
                .findByMatchupIdAndVoterId(matchupId, voterId)
                .isPresent();
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
}