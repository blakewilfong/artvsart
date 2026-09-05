package com.artvsart.integration.met;

import com.artvsart.model.Artwork;
import com.artvsart.model.ArtworkMetadata;
import com.artvsart.repository.ArtworkRepository;
import com.artvsart.service.ArtworkGenreClassifier;
import com.artvsart.service.BalancedPoolSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
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
            MAX_CANDIDATES_PER_DEPARTMENT = 1500;

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
                            15,
                            "The Robert Lehman Collection"
                    ),
                    new MetDepartment(
                            11,
                            "European Paintings"
                    )
            );

    private final MetArtworkClient metArtworkClient;
    private final ArtworkRepository artworkRepository;
    private final MetArtworkEligibilityPolicy
            eligibilityPolicy;
    private final ArtworkGenreClassifier genreClassifier;
    private final BalancedPoolSelector balancedPoolSelector;
    private final long requestDelayMilliseconds;

    public MetArtworkImportService(
            MetArtworkClient metArtworkClient,
            ArtworkRepository artworkRepository,
            MetArtworkEligibilityPolicy eligibilityPolicy,
            ArtworkGenreClassifier genreClassifier,
            BalancedPoolSelector balancedPoolSelector,
            @Value("${artvsart.import.met.request-delay-milliseconds:1000}")
            long requestDelayMilliseconds
    ) {
        if (requestDelayMilliseconds < 0) {
            throw new IllegalArgumentException(
                    "Met request delay cannot be negative"
            );
        }

        this.metArtworkClient = metArtworkClient;
        this.artworkRepository = artworkRepository;
        this.eligibilityPolicy = eligibilityPolicy;
        this.genreClassifier = genreClassifier;
        this.balancedPoolSelector = balancedPoolSelector;
        this.requestDelayMilliseconds = requestDelayMilliseconds;
    }

    public int importPaintingPool(int targetSize) {
        if (targetSize < 0) {
            throw new IllegalArgumentException(
                    "Target size cannot be negative (zero imports all)"
            );
        }

        long existingArtworkCount =
                artworkRepository.countBySource(SOURCE);

        boolean importAll = targetSize == 0;

        if (!importAll && existingArtworkCount >= targetSize) {
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
        List<MetDepartment> departments = importAll
                ? List.of(new MetDepartment(0, "All painting departments"))
                : DEPARTMENTS;

        departmentLoop:
        for (int departmentIndex = 0;
             departmentIndex < departments.size();
             departmentIndex++) {

            MetDepartment department =
                    departments.get(departmentIndex);

            int departmentTarget = baseQuota;

            if (departmentIndex
                    < departmentsWithExtraArtwork) {
                departmentTarget++;
            }

            LOGGER.info(
                    "Loading painting object IDs for {}",
                    department.name()
            );

            MetSearchResponse departmentObjects;

            pauseBeforeRequest();

            try {
                departmentObjects = importAll
                        ? metArtworkClient.searchPaintings()
                        : metArtworkClient
                                .searchDepartmentPaintings(
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
                            new LinkedHashSet<>(departmentObjects.objectIds())
                    );

            long existingDepartmentCount =
                    candidateIds.stream()
                            .filter(Objects::nonNull)
                            .map(String::valueOf)
                            .filter(
                                    importedObjectIds::contains
                            )
                            .count();

            int neededCount = importAll ? Integer.MAX_VALUE : Math.max(
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
                    importAll ? "all eligible" : neededCount,
                    department.name()
            );

            Collections.shuffle(
                    candidateIds,
                    new Random(department.id())
            );

            int checkedCount = 0;
            boolean stopImport = false;
            List<Artwork> eligibleCandidates = new ArrayList<>();
            int candidateTarget = importAll ? Integer.MAX_VALUE : (int) Math.min(
                    MAX_CANDIDATES_PER_DEPARTMENT,
                    (long) neededCount * 2
            );

            for (Long objectId : candidateIds) {
                if (eligibleCandidates.size() >= candidateTarget) {
                    break;
                }

                if (!importAll && checkedCount
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
                    Optional<Artwork> candidate =
                            loadArtwork(objectId);

                    if (candidate.isPresent()) {
                        eligibleCandidates.add(candidate.get());
                    }

                    if (importAll && eligibleCandidates.size() >= 50) {
                        artworkRepository.saveAll(eligibleCandidates);
                        eligibleCandidates.stream()
                                .map(Artwork::getSourceArtworkId)
                                .forEach(importedObjectIds::add);
                        importedCount += eligibleCandidates.size();
                        LOGGER.info("Met full import saved {} new artworks", importedCount);
                        eligibleCandidates = new ArrayList<>();
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
                        stopImport = true;
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

            List<Artwork> selected = balancedPoolSelector.select(
                    eligibleCandidates,
                    neededCount,
                    Artwork::getGenre
            );
            artworkRepository.saveAll(selected);
            selected.stream()
                    .map(Artwork::getSourceArtworkId)
                    .forEach(importedObjectIds::add);

            int departmentImportedCount = selected.size();
            importedCount += departmentImportedCount;

            LOGGER.info(
                    "Imported {} of {} requested artworks from {} after checking {} candidates",
                    departmentImportedCount,
                    importAll ? "all eligible" : neededCount,
                    department.name(),
                    checkedCount
            );

            if (stopImport) {
                break departmentLoop;
            }
        }

        long finalArtworkCount =
                artworkRepository.countBySource(SOURCE);

        if (!importAll && finalArtworkCount < targetSize) {
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
        return loadArtwork(objectId).map(artworkRepository::save);
    }

    private Optional<Artwork> loadArtwork(long objectId) {
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
        List<String> genreDescriptions = new ArrayList<>();
        Collections.addAll(
                genreDescriptions,
                optionalText(response.title()),
                optionalText(response.classification()),
                optionalText(response.objectName()),
                optionalText(response.medium()),
                optionalText(response.department())
        );

        if (response.tags() != null) {
            response.tags().stream()
                    .filter(Objects::nonNull)
                    .map(MetArtworkResponse.Tag::term)
                    .filter(Objects::nonNull)
                    .forEach(genreDescriptions::add);
        }

        artwork.classifyGenre(
                genreClassifier.classify(genreDescriptions)
        );

        return Optional.of(artwork);
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
        if (requestDelayMilliseconds == 0) {
            return;
        }

        try {
            Thread.sleep(
                    requestDelayMilliseconds
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
