package com.artvsart.integration.met;

import com.artvsart.model.Artwork;
import com.artvsart.model.ArtworkMetadata;
import com.artvsart.repository.ArtworkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MetArtworkImportService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    MetArtworkImportService.class
            );

    private static final String SOURCE = "met";
    private static final String LICENSE = "CC0";

    private static final int
            MAX_CANDIDATES_PER_DEPARTMENT = 1000;

    private static final long
            REQUEST_DELAY_MILLISECONDS = 1000;

    private static final List<MetDepartment> DEPARTMENTS =
            List.of(
                    new MetDepartment(
                            1,
                            "The American Wing"
                    ),
                    new MetDepartment(
                            6,
                            "Asian Art"
                    ),
                    new MetDepartment(
                            9,
                            "Drawings and Prints"
                    ),
                    new MetDepartment(
                            11,
                            "European Paintings"
                    ),
                    new MetDepartment(
                            14,
                            "Islamic Art"
                    ),
                    new MetDepartment(
                            15,
                            "The Robert Lehman Collection"
                    ),
                    new MetDepartment(
                            21,
                            "Modern and Contemporary Art"
                    )
            );

    private final MetArtworkClient metArtworkClient;
    private final ArtworkRepository artworkRepository;
    private final MetArtworkEligibilityPolicy
            eligibilityPolicy;

    public MetArtworkImportService(
            MetArtworkClient metArtworkClient,
            ArtworkRepository artworkRepository,
            MetArtworkEligibilityPolicy eligibilityPolicy
    ) {
        this.metArtworkClient = metArtworkClient;
        this.artworkRepository = artworkRepository;
        this.eligibilityPolicy = eligibilityPolicy;
    }

    public int importPaintingPool(int targetSize) {
        if (targetSize <= 0) {
            throw new IllegalArgumentException(
                    "Target size must be positive"
            );
        }

        long existingArtworkCount =
                artworkRepository.countBySource(SOURCE);

        if (existingArtworkCount >= targetSize) {
            LOGGER.info(
                    "Met artwork pool already contains {} artworks",
                    existingArtworkCount
            );

            return 0;
        }

        Set<String> importedObjectIds =
                artworkRepository
                        .findAllBySourceOrderByIdAsc(SOURCE)
                        .stream()
                        .map(Artwork::getSourceArtworkId)
                        .collect(Collectors.toSet());

        int baseQuota =
                targetSize / DEPARTMENTS.size();

        int departmentsWithExtraArtwork =
                targetSize % DEPARTMENTS.size();

        int importedCount = 0;

        departmentLoop:
        for (int departmentIndex = 0;
             departmentIndex < DEPARTMENTS.size();
             departmentIndex++) {

            MetDepartment department =
                    DEPARTMENTS.get(departmentIndex);

            int departmentTarget = baseQuota;

            if (departmentIndex
                    < departmentsWithExtraArtwork) {
                departmentTarget++;
            }

            LOGGER.info(
                    "Loading object IDs for {}",
                    department.name()
            );

            MetSearchResponse departmentObjects;

            pauseBeforeRequest();

            try {
                departmentObjects =
                        metArtworkClient
                                .listDepartmentObjects(
                                        department.id()
                                );
            } catch (
                    RestClientResponseException exception
            ) {
                int statusCode =
                        exception.getStatusCode().value();

                LOGGER.warn(
                        "Could not load Met department {}: HTTP {}",
                        department.name(),
                        statusCode
                );

                if (statusCode == 403
                        || statusCode == 429) {
                    break;
                }

                continue;
            } catch (RestClientException exception) {
                LOGGER.warn(
                        "Could not load Met department {}: {}",
                        department.name(),
                        exception.getMessage()
                );

                continue;
            }

            List<Long> candidateIds =
                    new ArrayList<>(
                            departmentObjects.objectIds()
                    );

            long existingDepartmentCount =
                    candidateIds.stream()
                            .filter(Objects::nonNull)
                            .map(String::valueOf)
                            .filter(
                                    importedObjectIds::contains
                            )
                            .count();

            int neededCount = Math.max(
                    0,
                    departmentTarget
                            - Math.toIntExact(
                            existingDepartmentCount
                    )
            );

            if (neededCount == 0) {
                LOGGER.info(
                        "{} already meets its quota of {} artworks",
                        department.name(),
                        departmentTarget
                );

                continue;
            }

            LOGGER.info(
                    "Importing {} flat artworks from {}",
                    neededCount,
                    department.name()
            );

            Collections.shuffle(
                    candidateIds,
                    new Random(department.id())
            );

            int checkedCount = 0;
            int departmentImportedCount = 0;

            for (Long objectId : candidateIds) {
                if (departmentImportedCount
                        >= neededCount) {
                    break;
                }

                if (checkedCount
                        >= MAX_CANDIDATES_PER_DEPARTMENT) {
                    break;
                }

                if (objectId == null) {
                    continue;
                }

                String sourceArtworkId =
                        Long.toString(objectId);

                if (importedObjectIds.contains(
                        sourceArtworkId
                )) {
                    continue;
                }

                checkedCount++;
                pauseBeforeRequest();

                try {
                    Optional<Artwork> importedArtwork =
                            importArtwork(objectId);

                    if (importedArtwork.isPresent()) {
                        importedObjectIds.add(
                                sourceArtworkId
                        );

                        importedCount++;
                        departmentImportedCount++;
                    }
                } catch (
                        RestClientResponseException exception
                ) {
                    int statusCode =
                            exception
                                    .getStatusCode()
                                    .value();

                    if (statusCode == 403
                            || statusCode == 429) {
                        LOGGER.warn(
                                "Met returned HTTP {}. Stopping import.",
                                statusCode
                        );

                        break departmentLoop;
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

            LOGGER.info(
                    "Imported {} of {} requested artworks from {} after checking {} candidates",
                    departmentImportedCount,
                    neededCount,
                    department.name(),
                    checkedCount
            );
        }

        long finalArtworkCount =
                artworkRepository.countBySource(SOURCE);

        if (finalArtworkCount < targetSize) {
            LOGGER.warn(
                    "Met import stopped with {} of {} artworks",
                    finalArtworkCount,
                    targetSize
            );
        }

        return importedCount;
    }

    public int refreshImportedMetadata() {
        List<Artwork> importedArtworks =
                artworkRepository
                        .findAllBySourceOrderByIdAsc(SOURCE);

        int refreshedCount = 0;

        for (Artwork artwork : importedArtworks) {
            long objectId;

            try {
                objectId = Long.parseLong(
                        artwork.getSourceArtworkId()
                );
            } catch (NumberFormatException exception) {
                LOGGER.warn(
                        "Invalid Met object ID: {}",
                        artwork.getSourceArtworkId()
                );

                continue;
            }

            pauseBeforeRequest();

            try {
                Optional<Artwork> refreshedArtwork =
                        importArtwork(objectId);

                if (refreshedArtwork.isPresent()) {
                    refreshedCount++;
                }
            } catch (
                    RestClientResponseException exception
            ) {
                int statusCode =
                        exception.getStatusCode().value();

                if (statusCode == 403
                        || statusCode == 429) {
                    LOGGER.warn(
                            "Met returned HTTP {}. Stopping metadata refresh.",
                            statusCode
                    );

                    break;
                }

                LOGGER.warn(
                        "Could not refresh Met object {}: HTTP {}",
                        objectId,
                        statusCode
                );
            } catch (RestClientException exception) {
                LOGGER.warn(
                        "Could not refresh Met object {}: {}",
                        objectId,
                        exception.getMessage()
                );
            }
        }

        return refreshedCount;
    }

    @Transactional
    public Optional<Artwork> importArtwork(long objectId) {
        MetArtworkResponse response =
                metArtworkClient.fetchArtwork(objectId);

        if (!eligibilityPolicy.isEligible(response)) {
            return Optional.empty();
        }

        String sourceArtworkId =
                Long.toString(response.objectId());

        Artwork artwork = artworkRepository
                .findBySourceAndSourceArtworkId(
                        SOURCE,
                        sourceArtworkId
                )
                .orElseGet(() -> new Artwork(
                        SOURCE,
                        sourceArtworkId,
                        response.title(),
                        response.artistDisplayName(),
                        response.objectDate(),
                        response.primaryImageSmall(),
                        response.objectUrl(),
                        LICENSE
                ));

        artwork.updateMetadata(
                createMetadata(response)
        );

        return Optional.of(
                artworkRepository.save(artwork)
        );
    }

    private ArtworkMetadata createMetadata(
            MetArtworkResponse response
    ) {
        return new ArtworkMetadata(
                optionalText(response.primaryImage()),
                optionalText(response.department()),
                optionalText(response.artistNationality()),
                parseYear(response.artistBeginDate()),
                parseYear(response.artistEndDate()),
                normalizeYear(response.objectBeginDate()),
                normalizeYear(response.objectEndDate()),
                optionalText(response.culture()),
                optionalText(response.country()),
                optionalText(response.medium())
        );
    }

    private String optionalText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private Integer parseYear(String value) {
        String normalizedValue =
                optionalText(value);

        if (normalizedValue == null) {
            return null;
        }

        try {
            return normalizeYear(
                    Integer.parseInt(normalizedValue)
            );
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private Integer normalizeYear(Integer year) {
        if (year == null || year == 0) {
            return null;
        }

        return year;
    }

    private void pauseBeforeRequest() {
        try {
            Thread.sleep(
                    REQUEST_DELAY_MILLISECONDS
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "Met artwork import was interrupted",
                    exception
            );
        }
    }

    private record MetDepartment(
            int id,
            String name
    ) {
    }
}