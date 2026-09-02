package com.artvsart.service;

import com.artvsart.model.Artwork;
import com.artvsart.repository.ArtworkRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ArtworkService {

    private final ArtworkRepository artworkRepository;
    private final String playableArtworkSource;

    public ArtworkService(
            ArtworkRepository artworkRepository,
            @Value("${artvsart.game.artwork-source}")
            String playableArtworkSource
    ) {
        this.artworkRepository = artworkRepository;
        this.playableArtworkSource = playableArtworkSource;
    }

    public List<Artwork> getAllArtworks() {
        return artworkRepository.findAll();
    }

    public List<Artwork> getPlayableArtworks() {
        return artworkRepository
                .findAllBySourceOrderByIdAsc(
                        playableArtworkSource
                );
    }
}