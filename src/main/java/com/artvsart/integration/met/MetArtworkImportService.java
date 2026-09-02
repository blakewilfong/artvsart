package com.artvsart.integration.met;

import com.artvsart.model.Artwork;
import com.artvsart.repository.ArtworkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.Optional;

@Service
public class MetArtworkImportService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(MetArtworkImportService.class);

    private static final String SOURCE = "met";
    private static final String LICENSE = "CC0";

    private static final int MAX_CANDIDATES_TO_CHECK = 1000;
    private static final long REQUEST_DELAY_MILLISECONDS = 1000;

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
        long existingArtworkCount =
                artworkRepository.countBySource(SOURCE);

        if (existingArtworkCount >= targetSize) {
            LOGGER.info(
                    "Met artwork pool already contains {} artworks",
                    existingArtworkCount
            );

            return 0;
        }

        MetSearchResponse searchResponse =
                metArtworkClient.searchPaintings();

        int importedCount = 0;
        int checkedCount = 0;

        for (Long objectId : searchResponse.objectIds()) {
            if (existingArtworkCount + importedCount >= targetSize) {
                break;
            }

            if (checkedCount >= MAX_CANDIDATES_TO_CHECK) {
                break;
            }

            String sourceArtworkId = Long.toString(objectId);

            boolean alreadyImported = artworkRepository
                    .findBySourceAndSourceArtworkId(
                            SOURCE,
                            sourceArtworkId
                    )
                    .isPresent();

            if (alreadyImported) {
                continue;
            }

            checkedCount++;

            pauseBeforeRequest();

            try {
                Optional<Artwork> importedArtwork =
                        importArtwork(objectId);

                if (importedArtwork.isPresent()) {
                    importedCount++;
                }
            } catch (RestClientResponseException exception) {
                int statusCode =
                        exception.getStatusCode().value();

                if (statusCode == 403 || statusCode == 429) {
                    LOGGER.warn(
                            "Met returned HTTP {}. Stopping import to avoid further requests.",
                            statusCode
                    );

                    break;
                }

                LOGGER.warn(
                        "Could not import Met object {}: HTTP {}",
                        objectId,
                        statusCode
                );
            } catch (RestClientException exception) {
                LOGGER.warn(
                        "Could not import Met object {}: {}",
                        objectId,
                        exception.getMessage()
                );
            }
        }

        long finalArtworkCount =
                existingArtworkCount + importedCount;

        if (finalArtworkCount < targetSize) {
            LOGGER.warn(
                    "Met import stopped with {} of {} artworks",
                    finalArtworkCount,
                    targetSize
            );
        }

        return importedCount;
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

    private void pauseBeforeRequest() {
        try {
            Thread.sleep(REQUEST_DELAY_MILLISECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "Met artwork import was interrupted",
                    exception
            );
        }
    }
}