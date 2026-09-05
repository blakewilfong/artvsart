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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(
        name = "artwork_questions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_question_run_round",
                        columnNames = {
                                "game_run_id",
                                "round_number"
                        }
                )
        }
)
public class ArtworkQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GameMode gameMode;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 40)
    private QuestionType questionType;

    @Column(name = "question_parameter", length = 256)
    private String questionParameter;

    @ManyToOne
    @JoinColumn(name = "game_run_id")
    private GameRun gameRun;

    @Column(name = "round_number")
    private Integer roundNumber;

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
        this(
                gameMode,
                questionType,
                null,
                null,
                artworkOne,
                artworkTwo,
                correctArtwork,
                null
        );
    }

    private ArtworkQuestion(
            GameMode gameMode,
            QuestionType questionType,
            GameRun gameRun,
            Integer roundNumber,
            Artwork artworkOne,
            Artwork artworkTwo,
            Artwork correctArtwork,
            String questionParameter
    ) {
        validate(
                gameMode,
                questionType,
                gameRun,
                roundNumber,
                artworkOne,
                artworkTwo,
                correctArtwork,
                questionParameter
        );

        this.gameMode = gameMode;
        this.questionType = questionType;
        this.gameRun = gameRun;
        this.roundNumber = roundNumber;
        this.artworkOne = artworkOne;
        this.artworkTwo = artworkTwo;
        this.correctArtwork = correctArtwork;
        this.questionParameter = questionParameter;
    }

    public static ArtworkQuestion forRun(
            GameRun gameRun,
            int roundNumber,
            QuestionType questionType,
            Artwork artworkOne,
            Artwork artworkTwo,
            Artwork correctArtwork,
            String questionParameter
    ) {
        if (gameRun == null) {
            throw new IllegalArgumentException(
                    "A game run is required"
            );
        }

        return new ArtworkQuestion(
                gameRun.getGameMode(),
                questionType,
                gameRun,
                roundNumber,
                artworkOne,
                artworkTwo,
                correctArtwork,
                questionParameter
        );
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

    public boolean belongsToRun() {
        return gameRun != null && roundNumber != null;
    }

    private void validate(
            GameMode gameMode,
            QuestionType questionType,
            GameRun gameRun,
            Integer roundNumber,
            Artwork artworkOne,
            Artwork artworkTwo,
            Artwork correctArtwork,
            String questionParameter
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

        if (questionType.requiresParameter()
                && (questionParameter == null
                || questionParameter.isBlank())) {
            throw new IllegalArgumentException(
                    "This question type requires a parameter"
            );
        }

        if ((gameRun == null) != (roundNumber == null)) {
            throw new IllegalArgumentException(
                    "A run and round number must be provided together"
            );
        }

        if (gameRun != null) {
            if (gameRun.getGameMode() != gameMode) {
                throw new IllegalArgumentException(
                        "Question mode must match its game run"
                );
            }

            if (roundNumber < 1) {
                throw new IllegalArgumentException(
                        "Round number must be positive"
                );
            }
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

    public String getQuestionParameter() {
        return questionParameter;
    }

    public String getPrompt() {
        return questionType.getPrompt(questionParameter);
    }

    public String getCorrectAnswerLabel() {
        return questionType.getCorrectAnswerLabel();
    }

    public String getIncorrectAnswerLabel() {
        return questionType.getIncorrectAnswerLabel();
    }

    public String getValueLabel() {
        return questionType.getValueLabel();
    }

    public String displayValue(Artwork artwork) {
        return questionType.displayValue(
                artwork,
                questionParameter
        );
    }

    public String getAnswerCaption(Artwork artwork) {
        return questionType.getAnswerCaption(
                artwork,
                questionParameter
        );
    }

    public String getAnswerContext() {
        return questionType.getAnswerContext(
                questionParameter
        );
    }

    public HistoricalEvent getHistoricalEvent() {
        return switch (questionType) {
            case BEFORE_HISTORICAL_EVENT, ARTIST_ALIVE_DURING_EVENT ->
                    HistoricalEvent.valueOf(questionParameter);
            default -> null;
        };
    }

    public GameRun getGameRun() {
        return gameRun;
    }

    public int getRoundNumber() {
        if (!belongsToRun()) {
            throw new IllegalStateException(
                    "Question does not belong to a game run"
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

    public Artwork getCorrectArtwork() {
        return correctArtwork;
    }
}
