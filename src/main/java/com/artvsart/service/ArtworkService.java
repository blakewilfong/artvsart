package com.artvsart.service;

import com.artvsart.model.Artwork;
import com.artvsart.repository.ArtworkRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ArtworkService {

    private final ArtworkRepository artworkRepository;

    public ArtworkService(ArtworkRepository artworkRepository) {
        this.artworkRepository = artworkRepository;
    }

    public List<Artwork> getAllArtworks() {
        return artworkRepository.findAll();
    }
}