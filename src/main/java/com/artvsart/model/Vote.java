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

import java.time.Instant;

@Entity
@Table(
        name = "votes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_vote_matchup_voter",
                        columnNames = {"matchup_id", "voter_id"}
                )
        }
)
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "matchup_id", nullable = false)
    private Matchup matchup;

    @ManyToOne(optional = false)
    @JoinColumn(name = "selected_artwork_id", nullable = false)
    private Artwork selectedArtwork;

    @Column(name = "voter_id", nullable = false, length = 36)
    private String voterId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PredictionOutcome outcome;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Vote() {
    }

    public Vote(
            Matchup matchup,
            Artwork selectedArtwork,
            String voterId,
            PredictionOutcome outcome
    ) {
        this.matchup = matchup;
        this.selectedArtwork = selectedArtwork;
        this.voterId = voterId;
        this.outcome = outcome;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Matchup getMatchup() {
        return matchup;
    }

    public Artwork getSelectedArtwork() {
        return selectedArtwork;
    }

    public String getVoterId() {
        return voterId;
    }

    public PredictionOutcome getOutcome() {
        return outcome;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}