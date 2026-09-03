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

import java.time.Instant;

@Entity
@Table(
        name = "artwork_answers",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_answer_question_voter",
                        columnNames = {
                                "question_id",
                                "voter_id"
                        }
                )
        }
)
public class ArtworkAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private ArtworkQuestion question;

    @ManyToOne(optional = false)
    @JoinColumn(name = "selected_artwork_id", nullable = false)
    private Artwork selectedArtwork;

    @Column(name = "voter_id", nullable = false, length = 36)
    private String voterId;

    @Column(nullable = false)
    private boolean correct;

    private Integer wagerAmount;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected ArtworkAnswer() {
    }

    public ArtworkAnswer(
            ArtworkQuestion question,
            Artwork selectedArtwork,
            String voterId
    ) {
        this(
                question,
                selectedArtwork,
                voterId,
                null
        );
    }

    private ArtworkAnswer(
            ArtworkQuestion question,
            Artwork selectedArtwork,
            String voterId,
            Integer wagerAmount
    ) {
        validate(
                question,
                selectedArtwork,
                voterId,
                wagerAmount
        );

        this.question = question;
        this.selectedArtwork = selectedArtwork;
        this.voterId = voterId;
        this.correct = question.isCorrect(
                selectedArtwork.getId()
        );
        this.wagerAmount = wagerAmount;
        this.createdAt = Instant.now();
    }

    public static ArtworkAnswer forWager(
            ArtworkQuestion question,
            Artwork selectedArtwork,
            String voterId,
            int wagerAmount
    ) {
        return new ArtworkAnswer(
                question,
                selectedArtwork,
                voterId,
                wagerAmount
        );
    }

    private void validate(
            ArtworkQuestion question,
            Artwork selectedArtwork,
            String voterId,
            Integer wagerAmount
    ) {
        if (question == null) {
            throw new IllegalArgumentException(
                    "A question is required"
            );
        }

        if (selectedArtwork == null
                || !question.containsArtwork(
                selectedArtwork.getId()
        )) {
            throw new IllegalArgumentException(
                    "Selected artwork is not part of this question"
            );
        }

        if (voterId == null || voterId.isBlank()) {
            throw new IllegalArgumentException(
                    "A voter ID is required"
            );
        }

        if (question.getGameMode() == GameMode.WAGER) {
            if (wagerAmount == null || wagerAmount < 1) {
                throw new IllegalArgumentException(
                        "A positive wager is required"
                );
            }
        } else if (wagerAmount != null) {
            throw new IllegalArgumentException(
                    "Only Wager Mode answers may contain a wager"
            );
        }
    }

    public Long getId() {
        return id;
    }

    public ArtworkQuestion getQuestion() {
        return question;
    }

    public Artwork getSelectedArtwork() {
        return selectedArtwork;
    }

    public String getVoterId() {
        return voterId;
    }

    public boolean isCorrect() {
        return correct;
    }

    public Integer getWagerAmount() {
        return wagerAmount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}