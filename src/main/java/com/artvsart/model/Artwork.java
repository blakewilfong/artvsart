package com.artvsart.model;

import jakarta.persistence.Column;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Entity
@Table(
        name = "artworks",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_artwork_source_id",
                        columnNames = {
                                "source",
                                "source_artwork_id"
                        }
                )
        }
)
public class Artwork {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String source;

    @Column(
            name = "source_artwork_id",
            nullable = false,
            length = 100
    )
    private String sourceArtworkId;

    @Column(nullable = false, length = 2048)
    private String title;

    @Column(length = 512)
    private String artistName;
    private String department;

    @Column(length = 256)
    private String dateDisplay;

    @Column(nullable = false, length = 1000)
    private String imageUrl;

    @Column(
            name = "original_image_url",
            length = 1000
    )
    private String originalImageUrl;

    @Column(name = "source_url", length = 1000)
    private String sourceUrl;

    @Column(length = 50)
    private String license;

    private String artistNationality;

    private Integer artistBeginYear;

    private Integer artistEndYear;

    @Column(name = "artist_popularity_rank")
    private Integer artistPopularityRank;

    private Integer objectBeginYear;

    private Integer objectEndYear;

    private String culture;

    private String country;

    @Column(length = 2048)
    private String medium;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private ArtworkGenre genre = ArtworkGenre.OTHER;

    @OneToMany(
            mappedBy = "artwork",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER
    )
    private List<ArtworkStyle> styles = new ArrayList<>();

    protected Artwork() {
    }

    public Artwork(
            String source,
            String sourceArtworkId,
            String title,
            String artistName,
            String dateDisplay,
            String imageUrl
    ) {
        this(
                source,
                sourceArtworkId,
                title,
                artistName,
                dateDisplay,
                imageUrl,
                null,
                null
        );
    }

    public Artwork(
            String source,
            String sourceArtworkId,
            String title,
            String artistName,
            String dateDisplay,
            String imageUrl,
            String sourceUrl,
            String license
    ) {
        this.source = source;
        this.sourceArtworkId = sourceArtworkId;
        this.title = title;
        this.artistName = artistName;
        this.dateDisplay = dateDisplay;
        this.imageUrl = imageUrl;
        this.sourceUrl = sourceUrl;
        this.license = license;
    }

    public void updateMetadata(ArtworkMetadata metadata) {
        if (metadata == null) {
            throw new IllegalArgumentException(
                    "Artwork metadata is required"
            );
        }

        this.originalImageUrl =
                metadata.originalImageUrl();

        this.department =
                metadata.department();

        this.artistNationality =
                metadata.artistNationality();

        this.artistBeginYear =
                metadata.artistBeginYear();

        this.artistEndYear =
                metadata.artistEndYear();

        this.objectBeginYear =
                metadata.objectBeginYear();

        this.objectEndYear =
                metadata.objectEndYear();

        this.culture = metadata.culture();
        this.country = metadata.country();
        this.medium = metadata.medium();
    }

    public Long getId() {
        return id;
    }

    public String getSource() {
        return source;
    }

    public String getSourceArtworkId() {
        return sourceArtworkId;
    }

    public String getTitle() {
        return title;
    }

    public String getArtistName() {
        return artistName;
    }

    public String getDateDisplay() {
        return dateDisplay;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getDepartment() {
        return department;
    }

    public String getOriginalImageUrl() {
        return originalImageUrl;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void updateSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getSourceCredit() {
        if (source == null) {
            return null;
        }

        return switch (source.toLowerCase(Locale.ROOT)) {
            case "met" ->
                    "Source: The Metropolitan Museum of Art";
            case "nga" ->
                    "Courtesy National Gallery of Art, Washington";
            case "cma" ->
                    "Source: The Cleveland Museum of Art";
            case "smithsonian" -> department == null
                    || department.isBlank()
                    ? "Source: Smithsonian Institution"
                    : "Source: " + department;
            default -> "Source: " + source;
        };
    }

    public String getLicense() {
        return license;
    }

    public String getArtistNationality() {
        return artistNationality;
    }

    public Integer getArtistBeginYear() {
        return artistBeginYear;
    }

    public Integer getArtistEndYear() {
        return artistEndYear;
    }

    public Integer getArtistPopularityRank() {
        return artistPopularityRank;
    }

    public void rankArtistPopularity(int rank) {
        if (rank < 1 || rank > 100) {
            throw new IllegalArgumentException(
                    "Artist popularity rank must be between 1 and 100"
            );
        }

        this.artistPopularityRank = rank;
    }

    public void clearArtistPopularityRank() {
        this.artistPopularityRank = null;
    }

    public Integer getObjectBeginYear() {
        return objectBeginYear;
    }

    public Integer getObjectEndYear() {
        return objectEndYear;
    }

    public Optional<Integer> findSingleCreationYear() {
        if (objectBeginYear == null
                || objectEndYear == null
                || !objectBeginYear.equals(objectEndYear)
                || objectBeginYear == 0
                || dateDisplay == null
                || dateDisplay.isBlank()
                || dateDisplay.toLowerCase(Locale.ROOT).contains("century")
                || dateDisplay.toLowerCase(Locale.ROOT).contains("centuries")
                || dateDisplay.equalsIgnoreCase("unknown")
                || dateDisplay.equalsIgnoreCase("date unknown")) {
            return Optional.empty();
        }

        return Optional.of(objectBeginYear);
    }

    public String getCulture() {
        return culture;
    }

    public String getCountry() {
        return country;
    }

    public String getMedium() {
        return medium;
    }

    public ArtworkGenre getGenre() {
        return genre == null ? ArtworkGenre.OTHER : genre;
    }

    public void classifyGenre(ArtworkGenre genre) {
        if (genre == null) {
            throw new IllegalArgumentException(
                    "An artwork genre is required"
            );
        }

        this.genre = genre;
    }

    public void replaceStyles(
            Collection<StyleDefinition> definitions
    ) {
        if (definitions == null) {
            throw new IllegalArgumentException(
                    "Artwork styles are required"
            );
        }

        styles.clear();
        addStyles(definitions);
    }

    public void replaceStylesFromSource(
            String source,
            Collection<StyleDefinition> definitions
    ) {
        if (source == null || source.isBlank()) {
            throw new IllegalArgumentException(
                    "An artwork style source is required"
            );
        }

        if (definitions == null) {
            throw new IllegalArgumentException(
                    "Artwork styles are required"
            );
        }

        String normalizedSource = normalizeStyleSource(source);
        Map<StyleIdentity, StyleDefinition> desiredStyles =
                new LinkedHashMap<>();

        for (StyleDefinition definition : definitions) {
            if (definition == null
                    || !normalizedSource.equals(
                    normalizeStyleSource(definition.source())
            )) {
                throw new IllegalArgumentException(
                        "Artwork style definitions must match their source"
                );
            }

            desiredStyles.putIfAbsent(
                    StyleIdentity.from(definition),
                    definition
            );
        }

        styles.removeIf(style -> normalizedSource.equals(
                normalizeStyleSource(style.getSource())
        ) && !desiredStyles.containsKey(
                StyleIdentity.from(style)
        ));

        Map<StyleIdentity, ArtworkStyle> existingStyles =
                new LinkedHashMap<>();

        styles.stream()
                .filter(style -> normalizedSource.equals(
                        normalizeStyleSource(style.getSource())
                ))
                .forEach(style -> existingStyles.put(
                        StyleIdentity.from(style),
                        style
                ));

        desiredStyles.forEach((identity, definition) -> {
            ArtworkStyle existingStyle = existingStyles.get(identity);

            if (existingStyle == null) {
                styles.add(new ArtworkStyle(
                        this,
                        definition.type(),
                        definition.label(),
                        definition.source()
                ));
                return;
            }

            existingStyle.updateDisplayLabel(definition.label());
        });
    }

    private void addStyles(
            Collection<StyleDefinition> definitions
    ) {

        definitions.stream()
                .distinct()
                .map(definition -> new ArtworkStyle(
                        this,
                        definition.type(),
                        definition.label(),
                        definition.source()
                ))
                .forEach(styles::add);
    }

    public List<ArtworkStyle> getStyles() {
        return styles.stream()
                .sorted(Comparator.comparing(
                        ArtworkStyle::getDisplayLabel,
                        String.CASE_INSENSITIVE_ORDER
                ))
                .toList();
    }

    public Optional<String> findStyleDisplayLabel(
            String normalizedLabel
    ) {
        return styles.stream()
                .filter(style -> style.getNormalizedLabel()
                        .equals(normalizedLabel))
                .map(ArtworkStyle::getDisplayLabel)
                .findFirst();
    }

    public record StyleDefinition(
            ArtworkStyleType type,
            String label,
            String source
    ) {
    }

    private static String normalizeStyleSource(String source) {
        return source == null
                ? ""
                : source.trim().toLowerCase(Locale.ROOT);
    }

    private record StyleIdentity(
            ArtworkStyleType type,
            String normalizedLabel,
            String source
    ) {
        private static StyleIdentity from(
                StyleDefinition definition
        ) {
            return new StyleIdentity(
                    definition.type(),
                    ArtworkStyle.normalize(definition.label()),
                    normalizeStyleSource(definition.source())
            );
        }

        private static StyleIdentity from(ArtworkStyle style) {
            return new StyleIdentity(
                    style.getType(),
                    style.getNormalizedLabel(),
                    normalizeStyleSource(style.getSource())
            );
        }
    }
}
