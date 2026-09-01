package com.artvsart.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "daily_games")
public class DailyGame {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private LocalDate gameDate;

    @Column(nullable = false)
    private int totalRounds;

    protected DailyGame() {
    }

    public DailyGame(LocalDate gameDate, int totalRounds) {
        if (totalRounds < 1) {
            throw new IllegalArgumentException(
                    "A daily game must contain at least one round"
            );
        }

        this.gameDate = gameDate;
        this.totalRounds = totalRounds;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getGameDate() {
        return gameDate;
    }

    public int getTotalRounds() {
        return totalRounds;
    }
}