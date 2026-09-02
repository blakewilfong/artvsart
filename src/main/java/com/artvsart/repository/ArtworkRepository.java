package com.artvsart.repository;

import com.artvsart.model.Artwork;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ArtworkRepository
        extends JpaRepository<Artwork, Long> {

    Optional<Artwork> findBySourceAndSourceArtworkId(
            String source,
            String sourceArtworkId
    );

    long countBySource(String source);
}