package com.artvsart.service;

import com.artvsart.model.Artwork;
import com.artvsart.model.Matchup;
import com.artvsart.repository.MatchupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class FreePlayService {

    private final ArtworkService artworkService;
    private final MatchupRepository matchupRepository;

    public FreePlayService(
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

        Matchup matchup = Matchup.forFreePlay(
                artworks.get(firstIndex),
                artworks.get(secondIndex)
        );

        return matchupRepository.save(matchup);
    }

    @Transactional(readOnly = true)
    public Matchup getMatchup(Long matchupId) {
        Matchup matchup = matchupRepository
                .findById(matchupId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Matchup does not exist"
                ));

        if (matchup.isDailyGameMatchup()) {
            throw new IllegalArgumentException(
                    "Matchup is not a free-play matchup"
            );
        }

        return matchup;
    }
}