package com.artvsart.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.Locale;

@Entity
@Table(
        name = "artwork_styles",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_artwork_style_source",
                columnNames = {
                        "artwork_id",
                        "style_type",
                        "normalized_label",
                        "source"
                }
        )
)
public class ArtworkStyle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "artwork_id", nullable = false)
    private Artwork artwork;

    @Enumerated(EnumType.STRING)
    @Column(name = "style_type", nullable = false, length = 20)
    private ArtworkStyleType type;

    @Column(name = "normalized_label", nullable = false, length = 128)
    private String normalizedLabel;

    @Column(name = "display_label", nullable = false, length = 256)
    private String displayLabel;

    @Column(nullable = false, length = 50)
    private String source;

    protected ArtworkStyle() {
    }

    ArtworkStyle(
            Artwork artwork,
            ArtworkStyleType type,
            String label,
            String source
    ) {
        if (artwork == null) {
            throw new IllegalArgumentException("An artwork is required");
        }

        if (type == null) {
            throw new IllegalArgumentException("A style type is required");
        }

        if (label == null || label.isBlank()) {
            throw new IllegalArgumentException("A style label is required");
        }

        if (source == null || source.isBlank()) {
            throw new IllegalArgumentException("A style source is required");
        }

        this.artwork = artwork;
        this.type = type;
        this.displayLabel = label.trim();
        this.normalizedLabel = normalize(label);
        this.source = source.trim().toLowerCase(Locale.ROOT);
    }

    public static String normalize(String label) {
        if (label == null) {
            return null;
        }

        return label.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", " ")
                .trim();
    }

    public ArtworkStyleType getType() {
        return type;
    }

    public String getNormalizedLabel() {
        return normalizedLabel;
    }

    public String getDisplayLabel() {
        return displayLabel;
    }

    public String getSource() {
        return source;
    }
}
