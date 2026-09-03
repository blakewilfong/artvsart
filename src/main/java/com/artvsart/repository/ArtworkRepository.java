package com.artvsart.repository;

import com.artvsart.model.Artwork;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ArtworkRepository
        extends JpaRepository<Artwork, Long> {

    Optional<Artwork> findBySourceAndSourceArtworkId(
            String source,
            String sourceArtworkId
    );

    List<Artwork> findAllBySourceOrderByIdAsc(
            String source
    );

    List<Artwork> findAllBySourceInOrderByIdAsc(
            Collection<String> sources
    );

    long countBySource(String source);
}
