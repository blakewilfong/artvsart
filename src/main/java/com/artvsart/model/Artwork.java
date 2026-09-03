package com.artvsart.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

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

    @Column(nullable = false)
    private String title;

    private String artistName;
    private String department;
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

    private Integer objectBeginYear;

    private Integer objectEndYear;

    private String culture;

    private String country;

    @Column(length = 1000)
    private String medium;

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

    public Integer getObjectBeginYear() {
        return objectBeginYear;
    }

    public Integer getObjectEndYear() {
        return objectEndYear;
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
}