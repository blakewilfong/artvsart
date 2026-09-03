package com.artvsart.integration.nga;

import com.artvsart.model.Artwork;
import com.artvsart.model.ArtworkStyle;
import com.artvsart.model.ArtworkStyleType;
import com.artvsart.repository.ArtworkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class NgaArtworkStyleImportService {

    private static final Logger LOGGER = LoggerFactory.getLogger(
            NgaArtworkStyleImportService.class
    );
    private static final String SOURCE = "nga";

    private final NgaOpenDataClient client;
    private final ArtworkRepository repository;
    private final String termsUrl;

    public NgaArtworkStyleImportService(
            NgaOpenDataClient client,
            ArtworkRepository repository,
            @Value("${artvsart.import.nga.terms-url}")
            String termsUrl
    ) {
        this.client = client;
        this.repository = repository;
        this.termsUrl = termsUrl;
    }

    @Transactional
    public Coverage importStyles() {
        List<Artwork> artworks =
                repository.findAllBySourceOrderByIdAsc(SOURCE);

        Map<String, Artwork> artworksBySourceId = artworks.stream()
                .collect(Collectors.toMap(
                        Artwork::getSourceArtworkId,
                        artwork -> artwork
                ));

        Map<String, List<Artwork.StyleDefinition>> definitions =
                client.read(
                        termsUrl,
                        reader -> readDefinitions(
                                reader,
                                artworksBySourceId.keySet()
                        )
                );

        int coveredArtworkCount = 0;
        Set<String> distinctLabels = definitions.values().stream()
                .flatMap(List::stream)
                .map(Artwork.StyleDefinition::label)
                .map(ArtworkStyle::normalize)
                .collect(Collectors.toSet());

        for (Artwork artwork : artworks) {
            List<Artwork.StyleDefinition> artworkDefinitions =
                    definitions.getOrDefault(
                            artwork.getSourceArtworkId(),
                            List.of()
                    );

            artwork.replaceStylesFromSource(
                    SOURCE,
                    artworkDefinitions
            );

            if (!artworkDefinitions.isEmpty()) {
                coveredArtworkCount++;
            }
        }

        repository.saveAll(artworks);

        Coverage coverage = new Coverage(
                artworks.size(),
                coveredArtworkCount,
                distinctLabels.size()
        );

        LOGGER.info(
                "NGA style coverage: {} of {} imported artworks, {} distinct style/school labels",
                coverage.coveredArtworkCount(),
                coverage.totalArtworkCount(),
                coverage.distinctLabelCount()
        );

        return coverage;
    }

    private Map<String, List<Artwork.StyleDefinition>> readDefinitions(
            Reader reader,
            Set<String> importedObjectIds
    ) throws IOException {
        NgaCsvReader csv = new NgaCsvReader(reader);
        Map<String, Map<String, Artwork.StyleDefinition>> byObject =
                new HashMap<>();
        NgaCsvReader.Row row;

        while ((row = csv.next()) != null) {
            String objectId = clean(row.get("objectID"));
            ArtworkStyleType type = styleType(row.get("termType"));

            if (objectId == null
                    || type == null
                    || !importedObjectIds.contains(objectId)) {
                continue;
            }

            String label = clean(row.get("visualBrowserStyle"));

            if (label == null) {
                label = clean(row.get("term"));
            }

            if (label == null) {
                continue;
            }

            Artwork.StyleDefinition definition =
                    new Artwork.StyleDefinition(type, label, SOURCE);

            String key = type + ":" + ArtworkStyle.normalize(label);

            byObject.computeIfAbsent(
                            objectId,
                            ignored -> new LinkedHashMap<>()
                    )
                    .putIfAbsent(key, definition);
        }

        Map<String, List<Artwork.StyleDefinition>> result =
                new HashMap<>();

        byObject.forEach((objectId, values) -> result.put(
                objectId,
                new ArrayList<>(values.values())
        ));

        return result;
    }

    private ArtworkStyleType styleType(String value) {
        String type = clean(value);

        if (type == null) {
            return null;
        }

        return switch (type.toLowerCase(Locale.ROOT)) {
            case "style" -> ArtworkStyleType.STYLE;
            case "school" -> ArtworkStyleType.SCHOOL;
            default -> null;
        };
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }

        String cleaned = value
                .replace("\uFEFF", "")
                .replaceAll("<[^>]+>", "")
                .trim();

        return cleaned.isBlank() ? null : cleaned;
    }

    public record Coverage(
            int totalArtworkCount,
            int coveredArtworkCount,
            int distinctLabelCount
    ) {
        public double percentage() {
            if (totalArtworkCount == 0) {
                return 0;
            }

            return coveredArtworkCount * 100.0 / totalArtworkCount;
        }
    }
}
