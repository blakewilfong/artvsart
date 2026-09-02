package com.artvsart.integration.met;

import com.artvsart.model.Artwork;
import com.artvsart.repository.ArtworkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class MetArtworkImportService {

    private static final String SOURCE = "met";
    private static final String LICENSE = "CC0";

    private final MetArtworkClient metArtworkClient;
    private final ArtworkRepository artworkRepository;

    public MetArtworkImportService(
            MetArtworkClient metArtworkClient,
            ArtworkRepository artworkRepository
    ) {
        this.metArtworkClient = metArtworkClient;
        this.artworkRepository = artworkRepository;
    }

    @Transactional
    public Optional<Artwork> importArtwork(long objectId) {
        MetArtworkResponse response =
                metArtworkClient.fetchArtwork(objectId);

        if (!response.isUsable()) {
            return Optional.empty();
        }

        String sourceArtworkId =
                Long.toString(response.objectId());

        Artwork artwork = artworkRepository
                .findBySourceAndSourceArtworkId(
                        SOURCE,
                        sourceArtworkId
                )
                .orElseGet(() -> artworkRepository.save(
                        new Artwork(
                                SOURCE,
                                sourceArtworkId,
                                response.title(),
                                response.artistDisplayName(),
                                response.objectDate(),
                                response.primaryImageSmall(),
                                response.objectUrl(),
                                LICENSE
                        )
                ));

        return Optional.of(artwork);
    }
}