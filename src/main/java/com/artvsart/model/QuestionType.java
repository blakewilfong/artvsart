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

            case ARTIST_YOUNGER_AT_CREATION ->
                    "Which artist was younger when they created this artwork?";

            default ->
                    throw unsupported();
        };
    }

    public String getCorrectAnswerLabel() {
        return switch (this) {
            case OLDER_ARTWORK ->
                    "Older";

            case ARTIST_BORN_EARLIER ->
                    "Born earlier";

            case ARTIST_YOUNGER_AT_CREATION ->
                    "Younger at creation";

            default ->
                    throw unsupported();
        };
    }

    public String getIncorrectAnswerLabel() {
        return switch (this) {
            case OLDER_ARTWORK ->
                    "Newer";

            case ARTIST_BORN_EARLIER ->
                    "Born later";

            case ARTIST_YOUNGER_AT_CREATION ->
                    "Older at creation";

            default ->
                    throw unsupported();
        };
    }

    public String getValueLabel() {
        return switch (this) {
            case OLDER_ARTWORK ->
                    "Artwork date";

            case ARTIST_BORN_EARLIER ->
                    "Artist birth year";

            case ARTIST_YOUNGER_AT_CREATION ->
                    "Artist age at creation";

            default ->
                    throw unsupported();
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

            case ARTIST_YOUNGER_AT_CREATION ->
                    formatArtistAgeAtCreation(artwork);

            default ->
                    throw unsupported();
        };
    }

    private String formatArtistAgeAtCreation(
            Artwork artwork
    ) {
        Integer artistBirthYear =
                artwork.getArtistBeginYear();

        Integer artworkYear =
                artwork.getObjectBeginYear();

        if (artistBirthYear == null
                || artworkYear == null) {
            return "Unknown";
        }

        int age = artworkYear - artistBirthYear;

        return age + " years old";
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

    private IllegalStateException unsupported() {
        return new IllegalStateException(
                "Question type is not implemented: "
                        + this
        );
    }
}