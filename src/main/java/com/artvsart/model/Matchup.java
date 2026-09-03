package com.artvsart.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "matchups")
public class Matchup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "artwork_one_id", nullable = false)
    private Artwork artworkOne;

    @ManyToOne(optional = false)
    @JoinColumn(name = "artwork_two_id", nullable = false)
    private Artwork artworkTwo;

    protected Matchup() {
    }

    private Matchup(
            Artwork artworkOne,
            Artwork artworkTwo
    ) {
        validateArtworks(
                artworkOne,
                artworkTwo
        );

        this.artworkOne = artworkOne;
        this.artworkTwo = artworkTwo;
    }

    public static Matchup forCrowd(
            Artwork artworkOne,
            Artwork artworkTwo
    ) {
        return new Matchup(
                artworkOne,
                artworkTwo
        );
    }

    private static void validateArtworks(
            Artwork artworkOne,
            Artwork artworkTwo
    ) {
        if (artworkOne == null || artworkTwo == null) {
            throw new IllegalArgumentException(
                    "A matchup requires two artworks"
            );
        }

        if (artworkOne == artworkTwo) {
            throw new IllegalArgumentException(
                    "A matchup requires two different artworks"
            );
        }

        if (artworkOne.getId() != null
                && artworkOne.getId().equals(
                artworkTwo.getId()
        )) {
            throw new IllegalArgumentException(
                    "A matchup requires two different artworks"
            );
        }
    }

    public Long getId() {
        return id;
    }

    public Artwork getArtworkOne() {
        return artworkOne;
    }

    public Artwork getArtworkTwo() {
        return artworkTwo;
    }
}