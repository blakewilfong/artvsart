package com.artvsart.integration.nga;

import com.artvsart.model.Artwork;
import com.artvsart.model.ArtworkMetadata;
import com.artvsart.repository.ArtworkRepository;
import com.artvsart.service.ArtworkGenreClassifier;
import com.artvsart.service.BalancedPoolSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class NgaArtworkImportService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    NgaArtworkImportService.class
            );

    private static final String SOURCE = "nga";
    private static final String LICENSE = "CC0";
    private static final int DISPLAY_IMAGE_SIZE = 1600;
    private static final Pattern HTML_TAG =
            Pattern.compile("<[^>]+>");

    private final NgaOpenDataClient client;
    private final ArtworkRepository repository;
    private final NgaArtworkEligibilityPolicy eligibilityPolicy;
    private final ArtworkGenreClassifier genreClassifier;
    private final BalancedPoolSelector balancedPoolSelector;
    private final String objectsUrl;
    private final String constituentsUrl;
    private final String linksUrl;
    private final String imagesUrl;
    private final String termsUrl;

    public NgaArtworkImportService(
            NgaOpenDataClient client,
            ArtworkRepository repository,
            NgaArtworkEligibilityPolicy eligibilityPolicy,
            ArtworkGenreClassifier genreClassifier,
            BalancedPoolSelector balancedPoolSelector,
            @Value("${artvsart.import.nga.objects-url}")
            String objectsUrl,
            @Value("${artvsart.import.nga.constituents-url}")
            String constituentsUrl,
            @Value("${artvsart.import.nga.object-constituents-url}")
            String linksUrl,
            @Value("${artvsart.import.nga.images-url}")
            String imagesUrl,
            @Value("${artvsart.import.nga.terms-url}")
            String termsUrl
    ) {
        this.client = client;
        this.repository = repository;
        this.eligibilityPolicy = eligibilityPolicy;
        this.genreClassifier = genreClassifier;
        this.balancedPoolSelector = balancedPoolSelector;
        this.objectsUrl = objectsUrl;
        this.constituentsUrl = constituentsUrl;
        this.linksUrl = linksUrl;
        this.imagesUrl = imagesUrl;
        this.termsUrl = termsUrl;
    }

    public int importPaintingPool(int targetSize) {
        if (targetSize <= 0) {
            throw new IllegalArgumentException(
                    "Target size must be positive"
            );
        }

        List<Artwork> existingArtworks =
                repository.findAllBySourceOrderByIdAsc(SOURCE);

        if (existingArtworks.size() >= targetSize) {
            LOGGER.info(
                    "NGA artwork pool already contains {} artworks",
                    existingArtworks.size()
            );

            return 0;
        }

        Set<String> existingIds = new HashSet<>();

        for (Artwork artwork : existingArtworks) {
            existingIds.add(artwork.getSourceArtworkId());
        }

        LOGGER.info("Loading NGA constituent metadata");
        Map<String, Artist> people = client.read(
                constituentsUrl,
                this::readPeople
        );

        LOGGER.info("Joining NGA artworks to primary artists");
        Map<String, Artist> artistsByObject = client.read(
                linksUrl,
                reader -> readArtistsByObject(reader, people)
        );

        LOGGER.info("Loading NGA open-access primary images");
        Map<String, Image> imagesByObject = client.read(
                imagesUrl,
                this::readImagesByObject
        );

        LOGGER.info("Loading NGA genre metadata");
        Map<String, List<String>> genresByObject = client.read(
                termsUrl,
                this::readGenresByObject
        );

        int remaining = targetSize - existingArtworks.size();

        LOGGER.info(
                "Selecting up to {} NGA paintings and collages",
                remaining
        );

        List<Artwork> selectedArtworks = client.read(
                objectsUrl,
                reader -> selectArtworks(
                        reader,
                        artistsByObject,
                        imagesByObject,
                        genresByObject,
                        existingIds,
                        remaining
                )
        );

        repository.saveAll(selectedArtworks);

        LOGGER.info(
                "NGA artwork import completed with {} new artworks",
                selectedArtworks.size()
        );

        if (selectedArtworks.size() < remaining) {
            LOGGER.warn(
                    "NGA import found only {} of {} requested eligible artworks",
                    selectedArtworks.size(),
                    remaining
            );
        }

        return selectedArtworks.size();
    }

    private Map<String, Artist> readPeople(
            Reader reader
    ) throws IOException {
        NgaCsvReader csv = new NgaCsvReader(reader);
        Map<String, Artist> people = new HashMap<>();
        NgaCsvReader.Row row;

        while ((row = csv.next()) != null) {
            String id = clean(row.get("constituentID"));
            String name = clean(row.get("forwardDisplayName"));

            if (id == null || name == null) {
                continue;
            }

            people.put(
                    id,
                    new Artist(
                            name,
                            clean(row.get("nationality")),
                            year(row.get("beginYear")),
                            year(row.get("endYear"))
                    )
            );
        }

        return people;
    }

    private Map<String, Artist> readArtistsByObject(
            Reader reader,
            Map<String, Artist> people
    ) throws IOException {
        NgaCsvReader csv = new NgaCsvReader(reader);
        Map<String, ArtistSelection> selections =
                new HashMap<>();
        NgaCsvReader.Row row;

        while ((row = csv.next()) != null) {
            if (!"artist".equalsIgnoreCase(
                    clean(row.get("roleType"))
            )) {
                continue;
            }

            String objectId = clean(row.get("objectID"));
            Artist artist = people.get(
                    clean(row.get("constituentID"))
            );

            if (objectId == null || artist == null) {
                continue;
            }

            int displayOrder = integer(
                    row.get("displayOrder"),
                    Integer.MAX_VALUE
            );

            ArtistSelection current = selections.get(objectId);

            if (current == null
                    || displayOrder < current.displayOrder()) {
                selections.put(
                        objectId,
                        new ArtistSelection(artist, displayOrder)
                );
            }
        }

        Map<String, Artist> artists = new HashMap<>();

        selections.forEach(
                (objectId, selection) -> artists.put(
                        objectId,
                        selection.artist()
                )
        );

        return artists;
    }

    private Map<String, Image> readImagesByObject(
            Reader reader
    ) throws IOException {
        NgaCsvReader csv = new NgaCsvReader(reader);
        Map<String, Image> images = new HashMap<>();
        NgaCsvReader.Row row;

        while ((row = csv.next()) != null) {
            if (!"1".equals(clean(row.get("openaccess")))
                    || !"primary".equalsIgnoreCase(
                    clean(row.get("viewtype"))
            )) {
                continue;
            }

            String objectId = clean(
                    row.get("depictstmsobjectid")
            );

            if (objectId == null || images.containsKey(objectId)) {
                continue;
            }

            String iiifBaseUrl = clean(row.get("iiifURL"));
            String thumbnailUrl = clean(row.get("iiifThumbURL"));
            String displayUrl = iiifImageUrl(
                    iiifBaseUrl,
                    "!" + DISPLAY_IMAGE_SIZE
                            + ","
                            + DISPLAY_IMAGE_SIZE
            );

            if (displayUrl == null) {
                displayUrl = thumbnailUrl;
            }

            if (displayUrl == null) {
                continue;
            }

            String originalUrl = iiifImageUrl(
                    iiifBaseUrl,
                    "full"
            );

            images.put(
                    objectId,
                    new Image(
                            displayUrl,
                            originalUrl == null
                                    ? displayUrl
                                    : originalUrl
                    )
            );
        }

        return images;
    }

    private List<Artwork> selectArtworks(
            Reader reader,
            Map<String, Artist> artistsByObject,
            Map<String, Image> imagesByObject,
            Map<String, List<String>> genresByObject,
            Set<String> existingIds,
            int limit
    ) throws IOException {
        NgaCsvReader csv = new NgaCsvReader(reader);
        List<Artwork> candidates = new ArrayList<>();
        NgaCsvReader.Row row;

        while ((row = csv.next()) != null) {
            String id = clean(row.get("objectID"));

            if (id == null || existingIds.contains(id)) {
                continue;
            }

            Artist artist = artistsByObject.get(id);
            Image image = imagesByObject.get(id);

            if (artist == null || image == null) {
                continue;
            }

            String title = clean(row.get("title"));
            String displayDate = clean(row.get("displayDate"));
            Integer beginYear = year(row.get("beginYear"));
            String medium = clean(row.get("medium"));

            if (!eligibilityPolicy.isEligible(
                    title,
                    displayDate,
                    beginYear,
                    clean(row.get("classification")),
                    clean(row.get("subClassification")),
                    clean(row.get("visualBrowserClassification")),
                    medium,
                    "1".equals(clean(row.get("isVirtual")))
            )) {
                continue;
            }

            Artwork artwork = new Artwork(
                    SOURCE,
                    id,
                    title,
                    artist.name(),
                    displayDate,
                    image.displayUrl(),
                    "https://www.nga.gov/artworks/" + id,
                    LICENSE
            );

            artwork.updateMetadata(
                    new ArtworkMetadata(
                            image.originalUrl(),
                            clean(row.get("departmentAbbr")),
                            artist.nationality(),
                            artist.beginYear(),
                            artist.endYear(),
                            beginYear,
                            year(row.get("endYear")),
                            null,
                            null,
                            medium
                    )
            );

            List<String> genreDescriptions = new ArrayList<>(
                    genresByObject.getOrDefault(id, List.of())
            );
            genreDescriptions.add(title);
            genreDescriptions.add(medium);
            genreDescriptions.add(clean(row.get("classification")));
            genreDescriptions.add(clean(row.get("subClassification")));
            genreDescriptions.add(clean(
                    row.get("visualBrowserClassification")
            ));
            artwork.classifyGenre(
                    genreClassifier.classify(genreDescriptions)
            );

            candidates.add(artwork);
        }

        return balancedPoolSelector.select(
                candidates,
                limit,
                Artwork::getGenre
        );
    }

    private Map<String, List<String>> readGenresByObject(
            Reader reader
    ) throws IOException {
        NgaCsvReader csv = new NgaCsvReader(reader);
        Map<String, List<String>> genres = new HashMap<>();
        NgaCsvReader.Row row;

        while ((row = csv.next()) != null) {
            if (!"theme".equalsIgnoreCase(
                    clean(row.get("termType"))
            )) {
                continue;
            }

            String objectId = clean(row.get("objectID"));
            String term = clean(row.get("visualBrowserTheme"));

            if (term == null) {
                term = clean(row.get("term"));
            }

            if (objectId != null && term != null) {
                genres.computeIfAbsent(
                        objectId,
                        ignored -> new ArrayList<>()
                ).add(term);
            }
        }

        return genres;
    }

    private String iiifImageUrl(
            String baseUrl,
            String size
    ) {
        if (baseUrl == null) {
            return null;
        }

        String normalized = baseUrl.endsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1)
                : baseUrl;

        return normalized
                + "/full/"
                + size
                + "/0/default.jpg";
    }

    private Integer year(String value) {
        int parsed = integer(value, 0);
        return parsed == 0 ? null : parsed;
    }

    private int integer(String value, int defaultValue) {
        String cleaned = clean(value);

        if (cleaned == null) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(cleaned);
        } catch (NumberFormatException exception) {
            return defaultValue;
        }
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }

        String cleaned = HTML_TAG.matcher(value)
                .replaceAll("")
                .replace("\uFEFF", "")
                .trim();

        if (cleaned.isBlank()
                || "NULL".equalsIgnoreCase(cleaned)) {
            return null;
        }

        return cleaned;
    }

    private record Artist(
            String name,
            String nationality,
            Integer beginYear,
            Integer endYear
    ) {
    }

    private record ArtistSelection(
            Artist artist,
            int displayOrder
    ) {
    }

    private record Image(
            String displayUrl,
            String originalUrl
    ) {
    }
}
