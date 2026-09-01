package com.artvsart.service;

import com.artvsart.dto.ArtworkStats;
import com.artvsart.model.Artwork;
import com.artvsart.repository.VoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ArtworkStatisticsService {

    private final VoteRepository voteRepository;

    public ArtworkStatisticsService(
            VoteRepository voteRepository
    ) {
        this.voteRepository = voteRepository;
    }

    @Transactional(readOnly = true)
    public ArtworkStats getStats(Artwork artwork) {
        long selections = voteRepository
                .countBySelectedArtworkId(artwork.getId());

        long presentations = voteRepository
                .countPresentationsByArtworkId(artwork.getId());

        return new ArtworkStats(
                selections,
                presentations
        );
    }
}