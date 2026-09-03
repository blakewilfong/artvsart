package com.artvsart.model;

public enum QuestionType {
    OLDER_ARTWORK,
    ARTIST_BORN_EARLIER,
    ARTIST_LIVED_LONGER,
    ARTIST_YOUNGER_AT_CREATION,
    ARTIST_NATIONALITY,
    ARTWORK_CULTURE,
    ARTWORK_MEDIUM,
    ARTWORK_SUBJECT,
    LARGER_ARTWORK,
    BEFORE_HISTORICAL_EVENT;

    public String getPrompt() {
        return switch (this) {
            case OLDER_ARTWORK ->
                    "Which artwork is older?";

            case ARTIST_BORN_EARLIER ->
                    "Which artist was born earlier?";

            default ->
                    throw new IllegalStateException(
                            "Question type is not implemented: "
                                    + this
                    );
        };
    }

    public String getCorrectAnswerLabel() {
        return switch (this) {
            case OLDER_ARTWORK ->
                    "Older";

            case ARTIST_BORN_EARLIER ->
                    "Born earlier";

            default ->
                    throw new IllegalStateException(
                            "Question type is not implemented: "
                                    + this
                    );
        };
    }

    public String getIncorrectAnswerLabel() {
        return switch (this) {
            case OLDER_ARTWORK ->
                    "Newer";

            case ARTIST_BORN_EARLIER ->
                    "Born later";

            default ->
                    throw new IllegalStateException(
                            "Question type is not implemented: "
                                    + this
                    );
        };
    }

    public String getValueLabel() {
        return switch (this) {
            case OLDER_ARTWORK ->
                    "Artwork date";

            case ARTIST_BORN_EARLIER ->
                    "Artist birth year";

            default ->
                    throw new IllegalStateException(
                            "Question type is not implemented: "
                                    + this
                    );
        };
    }

    public String displayValue(Artwork artwork) {
        if (artwork == null) {
            throw new IllegalArgumentException(
                    "An artwork is required"
            );
        }

        return switch (this) {
            case OLDER_ARTWORK ->
                    artwork.getDateDisplay();

            case ARTIST_BORN_EARLIER ->
                    formatYear(
                            artwork.getArtistBeginYear()
                    );

            default ->
                    throw new IllegalStateException(
                            "Question type is not implemented: "
                                    + this
                    );
        };
    }

    private String formatYear(Integer year) {
        if (year == null) {
            return "Unknown";
        }

        if (year < 0) {
            return Math.abs(year) + " BCE";
        }

        return Integer.toString(year);
    }
}