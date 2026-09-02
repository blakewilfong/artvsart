package com.artvsart.integration.met;

import com.artvsart.model.Artwork;
import com.artvsart.repository.ArtworkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

@Service
public class MetArtworkImportService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    MetArtworkImportService.class
            );

    private static final String SOURCE = "met";
    private static final String LICENSE = "CC0";

    private static final int MAX_CANDIDATES_TO_CHECK =
            1000;

    private final MetArtworkClient metArtworkClient;
    private final ArtworkRepository artworkRepository;

    public MetArtworkImportService(
            MetArtworkClient metArtworkClient,
            ArtworkRepository artworkRepository
    ) {
        this.metArtworkClient = metArtworkClient;
        this.artworkRepository = artworkRepository;
    }

    public int importPaintingPool(int targetSize) {
        if (targetSize < 1) {
            throw new IllegalArgumentException(
                    "Target size must be at least one"
            );
        }

        long existingCount =
                artworkRepository.countBySource(SOURCE);

        if (existingCount >= targetSize) {
            return 0;
        }

        MetSearchResponse searchResponse =
                metArtworkClient.searchPaintings();

        int importedCount = 0;
        int checkedCount = 0;

        for (Long objectId : searchResponse.objectIds()) {
            if (existingCount + importedCount >= targetSize
                    || checkedCount
                    >= MAX_CANDIDATES_TO_CHECK) {
                break;
            }

            if (objectId == null) {
                continue;
            }

            checkedCount++;

            String sourceArtworkId =
                    Long.toString(objectId);

            if (artworkRepository
                    .findBySourceAndSourceArtworkId(
                            SOURCE,
                            sourceArtworkId
                    )
                    .isPresent()) {
                continue;
            }

            try {
                if (importArtwork(objectId).isPresent()) {
                    importedCount++;
                }
            } catch (RestClientException exception) {
                LOGGER.warn(
                        "Could not import Met object {}: {}",
                        objectId,
                        exception.getMessage()
                );
            }
        }

        long finalCount = existingCount + importedCount;

        if (finalCount < targetSize) {
            LOGGER.warn(
                    "Met import stopped with {} of {} artworks",
                    finalCount,
                    targetSize
            );
        }

        return importedCount;
    }

    @Transactional
    public Optional<Artwork> importArtwork(long objectId) {
        String sourceArtworkId =
                Long.toString(objectId);

        Optional<Artwork> existingArtwork =
                artworkRepository
                        .findBySourceAndSourceArtworkId(
                                SOURCE,
                                sourceArtworkId
                        );

        if (existingArtwork.isPresent()) {
            return existingArtwork;
        }

        MetArtworkResponse response =
                metArtworkClient.fetchArtwork(objectId);

        if (!response.isUsable()) {
            return Optional.empty();
        }

        Artwork artwork = artworkRepository.save(
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
        );

        return Optional.of(artwork);
    }
}