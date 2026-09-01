package com.artvsart.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "matchups",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_matchup_game_round",
                        columnNames = {
                                "daily_game_id",
                                "round_number"
                        }
                )
        }
)
public class Matchup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "daily_game_id", nullable = false)
    private DailyGame dailyGame;

    @Column(name = "round_number", nullable = false)
    private int roundNumber;

    @ManyToOne(optional = false)
    @JoinColumn(name = "artwork_one_id", nullable = false)
    private Artwork artworkOne;

    @ManyToOne(optional = false)
    @JoinColumn(name = "artwork_two_id", nullable = false)
    private Artwork artworkTwo;

    protected Matchup() {
    }

    public Matchup(
            DailyGame dailyGame,
            int roundNumber,
            Artwork artworkOne,
            Artwork artworkTwo
    ) {
        if (roundNumber < 1
                || roundNumber > dailyGame.getTotalRounds()) {
            throw new IllegalArgumentException(
                    "Round number must be within the daily game"
            );
        }

        if (artworkOne == artworkTwo) {
            throw new IllegalArgumentException(
                    "A matchup requires two different artworks"
            );
        }

        this.dailyGame = dailyGame;
        this.roundNumber = roundNumber;
        this.artworkOne = artworkOne;
        this.artworkTwo = artworkTwo;
    }

    public Long getId() {
        return id;
    }

    public DailyGame getDailyGame() {
        return dailyGame;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public Artwork getArtworkOne() {
        return artworkOne;
    }

    public Artwork getArtworkTwo() {
        return artworkTwo;
    }
}