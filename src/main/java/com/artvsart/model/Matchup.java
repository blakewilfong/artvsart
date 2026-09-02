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

    @ManyToOne
    @JoinColumn(name = "daily_game_id")
    private DailyGame dailyGame;

    @Column(name = "round_number")
    private Integer roundNumber;

    @ManyToOne(optional = false)
    @JoinColumn(name = "artwork_one_id", nullable = false)
    private Artwork artworkOne;

    @ManyToOne(optional = false)
    @JoinColumn(name = "artwork_two_id", nullable = false)
    private Artwork artworkTwo;

    protected Matchup() {
    }

    private Matchup(
            DailyGame dailyGame,
            Integer roundNumber,
            Artwork artworkOne,
            Artwork artworkTwo
    ) {
        validateArtworks(artworkOne, artworkTwo);

        this.dailyGame = dailyGame;
        this.roundNumber = roundNumber;
        this.artworkOne = artworkOne;
        this.artworkTwo = artworkTwo;
    }

    public static Matchup forDailyGame(
            DailyGame dailyGame,
            int roundNumber,
            Artwork artworkOne,
            Artwork artworkTwo
    ) {
        if (dailyGame == null) {
            throw new IllegalArgumentException(
                    "A daily game is required"
            );
        }

        if (roundNumber < 1
                || roundNumber > dailyGame.getTotalRounds()) {
            throw new IllegalArgumentException(
                    "Round number must be within the daily game"
            );
        }

        return new Matchup(
                dailyGame,
                roundNumber,
                artworkOne,
                artworkTwo
        );
    }

    public static Matchup forFreePlay(
            Artwork artworkOne,
            Artwork artworkTwo
    ) {
        return new Matchup(
                null,
                null,
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
    }

    public Long getId() {
        return id;
    }

    public DailyGame getDailyGame() {
        return dailyGame;
    }

    public int getRoundNumber() {
        if (!isDailyGameMatchup()) {
            throw new IllegalStateException(
                    "Free-play matchups do not have round numbers"
            );
        }

        return roundNumber;
    }

    public Artwork getArtworkOne() {
        return artworkOne;
    }

    public Artwork getArtworkTwo() {
        return artworkTwo;
    }

    public boolean isDailyGameMatchup() {
        return dailyGame != null && roundNumber != null;
    }
}