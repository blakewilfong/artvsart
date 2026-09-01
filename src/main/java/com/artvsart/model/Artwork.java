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
                        columnNames = {"source", "source_artwork_id"}
                )
        }
)
public class Artwork {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String source;

    @Column(name = "source_artwork_id", nullable = false, length = 100)
    private String sourceArtworkId;

    @Column(nullable = false)
    private String title;

    private String artistName;

    private String dateDisplay;

    @Column(nullable = false, length = 1000)
    private String imageUrl;

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
        this.source = source;
        this.sourceArtworkId = sourceArtworkId;
        this.title = title;
        this.artistName = artistName;
        this.dateDisplay = dateDisplay;
        this.imageUrl = imageUrl;
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
}