package com.artvsart.service;

import com.artvsart.model.Artwork;
import com.artvsart.model.Matchup;
import com.artvsart.repository.MatchupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class CrowdGameService {

    private final ArtworkService artworkService;
    private final MatchupRepository matchupRepository;

    public CrowdGameService(
            ArtworkService artworkService,
            MatchupRepository matchupRepository
    ) {
        this.artworkService = artworkService;
        this.matchupRepository = matchupRepository;
    }

    @Transactional
    public Matchup createMatchup() {
        List<Artwork> artworks =
                artworkService.getPlayableArtworks();

        if (artworks.size() < 2) {
            throw new IllegalStateException(
                    "At least two playable artworks are required"
            );
        }

        int firstIndex = ThreadLocalRandom
                .current()
                .nextInt(artworks.size());

        int secondIndex = ThreadLocalRandom
                .current()
                .nextInt(artworks.size() - 1);

        if (secondIndex >= firstIndex) {
            secondIndex++;
        }

        Matchup matchup = Matchup.forCrowd(
                artworks.get(firstIndex),
                artworks.get(secondIndex)
        );

        return matchupRepository.save(matchup);
    }

    @Transactional(readOnly = true)
    public Matchup getMatchup(Long matchupId) {
        return matchupRepository
                .findById(matchupId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Matchup does not exist"
                ));
    }
}