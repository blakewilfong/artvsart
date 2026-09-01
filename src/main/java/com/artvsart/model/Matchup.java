package com.artvsart.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "matchups")
public class Matchup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private LocalDate matchupDate;

    @ManyToOne(optional = false)
    @JoinColumn(name = "artwork_one_id", nullable = false)
    private Artwork artworkOne;

    @ManyToOne(optional = false)
    @JoinColumn(name = "artwork_two_id", nullable = false)
    private Artwork artworkTwo;

    protected Matchup() {
    }

    public Matchup(
            LocalDate matchupDate,
            Artwork artworkOne,
            Artwork artworkTwo
    ) {
        if (artworkOne == artworkTwo) {
            throw new IllegalArgumentException(
                    "A matchup requires two different artworks"
            );
        }

        this.matchupDate = matchupDate;
        this.artworkOne = artworkOne;
        this.artworkTwo = artworkTwo;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getMatchupDate() {
        return matchupDate;
    }

    public Artwork getArtworkOne() {
        return artworkOne;
    }

    public Artwork getArtworkTwo() {
        return artworkTwo;
    }
}