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

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected ArtworkAnswer() {
    }

    public ArtworkAnswer(
            ArtworkQuestion question,
            Artwork selectedArtwork,
            String voterId
    ) {
        validate(question, selectedArtwork, voterId);

        this.question = question;
        this.selectedArtwork = selectedArtwork;
        this.voterId = voterId;
        this.correct = question.isCorrect(
                selectedArtwork.getId()
        );
        this.createdAt = Instant.now();
    }

    private void validate(
            ArtworkQuestion question,
            Artwork selectedArtwork,
            String voterId
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

    public Instant getCreatedAt() {
        return createdAt;
    }
}