package com.artvsart.service;

import com.artvsart.model.Artwork;
import com.artvsart.repository.ArtworkRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class ArtworkService {

    private final ArtworkRepository artworkRepository;
    private final List<String> playableArtworkSources;

    public ArtworkService(
            ArtworkRepository artworkRepository,
            @Value("${artvsart.game.artwork-sources:${artvsart.game.artwork-source:met}}")
            String playableArtworkSources
    ) {
        this.artworkRepository = artworkRepository;
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
}
