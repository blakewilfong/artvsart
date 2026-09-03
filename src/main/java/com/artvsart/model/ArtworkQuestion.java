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

@Entity
@Table(name = "artwork_questions")
public class ArtworkQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GameMode gameMode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private QuestionType questionType;

    @ManyToOne(optional = false)
    @JoinColumn(name = "artwork_one_id", nullable = false)
    private Artwork artworkOne;

    @ManyToOne(optional = false)
    @JoinColumn(name = "artwork_two_id", nullable = false)
    private Artwork artworkTwo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "correct_artwork_id", nullable = false)
    private Artwork correctArtwork;

    protected ArtworkQuestion() {
    }

    public ArtworkQuestion(
            GameMode gameMode,
            QuestionType questionType,
            Artwork artworkOne,
            Artwork artworkTwo,
            Artwork correctArtwork
    ) {
        validate(
                gameMode,
                questionType,
                artworkOne,
                artworkTwo,
                correctArtwork
        );

        this.gameMode = gameMode;
        this.questionType = questionType;
        this.artworkOne = artworkOne;
        this.artworkTwo = artworkTwo;
        this.correctArtwork = correctArtwork;
    }

    public boolean isCorrect(Long selectedArtworkId) {
        if (selectedArtworkId == null) {
            return false;
        }

        return selectedArtworkId.equals(
                correctArtwork.getId()
        );
    }

    public boolean containsArtwork(Long artworkId) {
        if (artworkId == null) {
            return false;
        }

        return artworkId.equals(artworkOne.getId())
                || artworkId.equals(artworkTwo.getId());
    }

    private void validate(
            GameMode gameMode,
            QuestionType questionType,
            Artwork artworkOne,
            Artwork artworkTwo,
            Artwork correctArtwork
    ) {
        if (gameMode == null) {
            throw new IllegalArgumentException(
                    "A game mode is required"
            );
        }

        if (gameMode == GameMode.CROWD) {
            throw new IllegalArgumentException(
                    "Crowd Mode does not use factual questions"
            );
        }

        if (questionType == null) {
            throw new IllegalArgumentException(
                    "A question type is required"
            );
        }

        if (artworkOne == null
                || artworkTwo == null
                || correctArtwork == null) {
            throw new IllegalArgumentException(
                    "A question requires two artworks and an answer"
            );
        }

        if (sameArtwork(artworkOne, artworkTwo)) {
            throw new IllegalArgumentException(
                    "A question requires two different artworks"
            );
        }

        if (!sameArtwork(correctArtwork, artworkOne)
                && !sameArtwork(correctArtwork, artworkTwo)) {
            throw new IllegalArgumentException(
                    "The correct artwork must belong to the question"
            );
        }
    }

    private boolean sameArtwork(
            Artwork first,
            Artwork second
    ) {
        if (first == second) {
            return true;
        }

        return first.getId() != null
                && first.getId().equals(second.getId());
    }

    public Long getId() {
        return id;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public QuestionType getQuestionType() {
        return questionType;
    }

    public Artwork getArtworkOne() {
        return artworkOne;
    }

    public Artwork getArtworkTwo() {
        return artworkTwo;
    }

    public Artwork getCorrectArtwork() {
        return correctArtwork;
    }
}