package com.artvsart.service;

import com.artvsart.model.Artwork;
import com.artvsart.model.ArtworkGenre;
import com.artvsart.repository.ArtworkRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ArtworkService {

    private final ArtworkRepository artworkRepository;
    private final List<String> playableArtworkSources;
    private final BalancedPoolSelector balancedPoolSelector;

    public ArtworkService(
            ArtworkRepository artworkRepository,
            @Value("${artvsart.game.artwork-sources:${artvsart.game.artwork-source:met}}")
            String playableArtworkSources,
            BalancedPoolSelector balancedPoolSelector
    ) {
        this.artworkRepository = artworkRepository;
        this.balancedPoolSelector = balancedPoolSelector;
        this.playableArtworkSources = Arrays.stream(
                        playableArtworkSources.split(",")
                )
                .map(String::trim)
                .filter(source -> !source.isBlank())
                .distinct()
                .toList();

        if (this.playableArtworkSources.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one playable artwork source is required"
            );
        }
    }

    public List<Artwork> getAllArtworks() {
        return artworkRepository.findAll();
    }

    public List<Artwork> getPlayableArtworks() {
        return artworkRepository
                .findAllBySourceInOrderByIdAsc(
                        playableArtworkSources
                );
    }

    public List<Artwork> getBalancedQuestionCandidates(
            int limit
    ) {
        if (limit < 2) {
            throw new IllegalArgumentException(
                    "At least two question candidates are required"
            );
        }

        List<Artwork> candidates = new ArrayList<>(
                getPlayableArtworks()
        );
        Collections.shuffle(
                candidates,
                ThreadLocalRandom.current()
        );

        return balancedPoolSelector.select(
                candidates,
                limit,
                artwork -> new CollectionBucket(
                        artwork.getSource().toLowerCase(Locale.ROOT),
                        artwork.getGenre()
                )
        );
    }

    private record CollectionBucket(
            String source,
            ArtworkGenre genre
    ) {
    }
}
