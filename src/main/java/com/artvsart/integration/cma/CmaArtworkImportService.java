package com.artvsart.integration.cma;

import com.artvsart.model.Artwork;
import com.artvsart.model.ArtworkMetadata;
import com.artvsart.repository.ArtworkRepository;
import com.artvsart.service.ArtworkGenreClassifier;
import com.artvsart.service.BalancedPoolSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class CmaArtworkImportService {

    private static final Logger LOGGER = LoggerFactory.getLogger(
            CmaArtworkImportService.class
    );
    private static final String SOURCE = "cma";
    private static final String LICENSE = "CC0";
    private static final int PAGE_SIZE = 1000;

    private final CmaArtworkClient client;
    private final ArtworkRepository repository;
    private final CmaArtworkEligibilityPolicy eligibilityPolicy;
    private final ArtworkGenreClassifier genreClassifier;
    private final BalancedPoolSelector balancedPoolSelector;

    public CmaArtworkImportService(
            CmaArtworkClient client,
            ArtworkRepository repository,
            CmaArtworkEligibilityPolicy eligibilityPolicy,
            ArtworkGenreClassifier genreClassifier,
            BalancedPoolSelector balancedPoolSelector
    ) {
        this.client = client;
        this.repository = repository;
        this.eligibilityPolicy = eligibilityPolicy;
        this.genreClassifier = genreClassifier;
        this.balancedPoolSelector = balancedPoolSelector;
    }

    public int importModernPaintingPool(
            int targetSize,
            int createdAfterYear,
            int maximumWorksPerArtist
    ) {
        if (targetSize <= 0) {
            throw new IllegalArgumentException(
                    "Target size must be positive"
            );
        }

        if (maximumWorksPerArtist <= 0) {
            throw new IllegalArgumentException(
                    "Maximum works per artist must be positive"
            );
        }

        List<Artwork> existingArtworks =
                repository.findAllBySourceOrderByIdAsc(SOURCE);

        if (existingArtworks.size() >= targetSize) {
            LOGGER.info(
                    "CMA artwork pool already contains {} artworks",
                    existingArtworks.size()
            );
            return 0;
        }

        Set<String> existingIds = new HashSet<>();
        Map<String, Integer> worksByArtist = new HashMap<>();

        for (Artwork artwork : existingArtworks) {
            existingIds.add(artwork.getSourceArtworkId());
            worksByArtist.merge(
                    normalizeArtist(artwork.getArtistName()),
                    1,
                    Integer::sum
            );
        }

        List<CmaArtworkResponse> candidates = loadCandidates(
                createdAfterYear
        );

        candidates.sort(
                Comparator.comparing(
                                CmaArtworkResponse::creationDateEarliest,
                                Comparator.reverseOrder()
                        )
                        .thenComparing(CmaArtworkResponse::id)
        );

        int needed = targetSize - existingArtworks.size();
        List<Artwork> eligible = new ArrayList<>();

        for (CmaArtworkResponse candidate : candidates) {
            String sourceArtworkId = candidate.id().toString();

            if (existingIds.contains(sourceArtworkId)
                    || !eligibilityPolicy.isEligible(
                    candidate,
                    createdAfterYear
            )) {
                continue;
            }

            CmaArtworkResponse.Creator artist = candidate
                    .primaryArtist()
                    .orElseThrow();
            String artistKey = normalizeArtist(artist.displayName());
            int artistCount = worksByArtist.getOrDefault(artistKey, 0);

            if (artistCount >= maximumWorksPerArtist) {
                continue;
            }

            eligible.add(toArtwork(candidate, artist));
            existingIds.add(sourceArtworkId);
            worksByArtist.put(artistKey, artistCount + 1);
        }

        List<Artwork> selected = balancedPoolSelector.select(
                eligible,
                needed,
                Artwork::getGenre
        );

        repository.saveAll(selected);

        LOGGER.info(
                "CMA import completed with {} new post-{} paintings",
                selected.size(),
                createdAfterYear
        );

        if (selected.size() < needed) {
            LOGGER.warn(
                    "CMA import found only {} of {} requested eligible artworks",
                    selected.size(),
                    needed
            );
        }

        return selected.size();
    }

    private List<CmaArtworkResponse> loadCandidates(
            int createdAfterYear
    ) {
        List<CmaArtworkResponse> candidates = new ArrayList<>();
        int skip = 0;
        int total;

        do {
            CmaArtworkSearchResponse response =
                    client.searchOpenAccessPaintings(
                            createdAfterYear,
                            skip,
                            PAGE_SIZE
                    );
            List<CmaArtworkResponse> page = response.artworks();

            if (page.isEmpty()) {
                break;
            }

            candidates.addAll(page);
            skip += page.size();
            total = response.total();
        } while (skip < total);

        return candidates;
    }

    private Artwork toArtwork(
            CmaArtworkResponse response,
            CmaArtworkResponse.Creator artist
    ) {
        Artwork artwork = new Artwork(
                SOURCE,
                response.id().toString(),
                response.title().trim(),
                artist.displayName(),
                response.creationDate().trim(),
                response.webImageUrl(),
                response.url(),
                LICENSE
        );

        artwork.updateMetadata(new ArtworkMetadata(
                response.printImageUrl(),
                optionalText(response.department()),
                optionalText(artist.nationality()),
                parseYear(artist.birthYear()),
                parseYear(artist.deathYear()),
                normalizeYear(response.creationDateEarliest()),
                normalizeYear(response.creationDateLatest()),
                optionalText(response.cultureDisplay()),
                null,
                optionalText(response.technique())
        ));
        artwork.classifyGenre(genreClassifier.classify(
                response.title(),
                response.description(),
                response.technique(),
                response.department(),
                response.cultureDisplay()
        ));

        return artwork;
    }

    private String normalizeArtist(String artistName) {
        if (artistName == null) {
            return "";
        }

        return artistName.trim().toLowerCase(Locale.ROOT);
    }

    private String optionalText(String value) {
        return value == null || value.isBlank()
                ? null
                : value.trim();
    }

    private Integer parseYear(String value) {
        String normalized = optionalText(value);

        if (normalized == null) {
            return null;
        }

        try {
            return normalizeYear(Integer.parseInt(normalized));
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private Integer normalizeYear(Integer year) {
        return year == null || year == 0 ? null : year;
    }
}
