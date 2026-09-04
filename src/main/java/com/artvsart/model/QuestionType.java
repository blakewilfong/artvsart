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
    ARTWORK_STYLE,
    LARGER_ARTWORK,
    BEFORE_HISTORICAL_EVENT;

    public String getPrompt() {
        return getPrompt(null);
    }

    public String getPrompt(String parameter) {
        return switch (this) {
            case OLDER_ARTWORK ->
                    "Which artwork is older?";

            case ARTIST_BORN_EARLIER ->
                    "Which artist was born earlier?";

            case ARTIST_YOUNGER_AT_CREATION ->
                    "Which artist was younger when they created this artwork?";

            case ARTIST_NATIONALITY ->
                    "Which artwork was created by "
                            + indefiniteArticle(parameter)
                            + " "
                            + required(parameter)
                            + " artist?";

            case ARTWORK_MEDIUM ->
                    "Which artwork was made with "
                            + medium(parameter).getDisplayName()
                            + "?";

            case ARTWORK_STYLE ->
                    "Which artwork is associated with the "
                            + required(parameter)
                            + " tradition?";

            case BEFORE_HISTORICAL_EVENT ->
                    "Which artwork was created closer to when "
                            + event(parameter).getDisplayName()
                            + "?";

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

            case ARTIST_NATIONALITY,
                 ARTWORK_MEDIUM,
                 ARTWORK_STYLE -> "Matches";

            case BEFORE_HISTORICAL_EVENT -> "Closer";

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

            case ARTIST_NATIONALITY,
                 ARTWORK_MEDIUM,
                 ARTWORK_STYLE -> "Does not match";

            case BEFORE_HISTORICAL_EVENT -> "Farther away";

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

            case ARTIST_NATIONALITY -> "Artist nationality";

            case ARTWORK_MEDIUM -> "Medium";

            case ARTWORK_STYLE -> "Style or school";

            case BEFORE_HISTORICAL_EVENT -> "Artwork date";

            default ->
                    throw unsupported();
        };
    }

    public String displayValue(Artwork artwork) {
        return displayValue(artwork, null);
    }

    public String displayValue(
            Artwork artwork,
            String parameter
    ) {
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

            case ARTIST_NATIONALITY ->
                    valueOrUnknown(artwork.getArtistNationality());

            case ARTWORK_MEDIUM ->
                    valueOrUnknown(artwork.getMedium());

            case ARTWORK_STYLE -> artwork.getStyles().isEmpty()
                    ? "No style recorded"
                    : artwork.getStyles().stream()
                    .map(ArtworkStyle::getDisplayLabel)
                    .distinct()
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .reduce((first, second) -> first + ", " + second)
                    .orElse("No style recorded");

            case BEFORE_HISTORICAL_EVENT ->
                    artwork.getDateDisplay();

            default ->
                    throw unsupported();
        };
    }

    public String getAnswerCaption(
            Artwork artwork,
            String parameter
    ) {
        return switch (this) {
            case OLDER_ARTWORK,
                 BEFORE_HISTORICAL_EVENT ->
                    "Created " + displayValue(artwork, parameter);

            case ARTIST_BORN_EARLIER -> {
                String year = displayValue(artwork, parameter);
                yield "Unknown".equals(year)
                        ? "Birth year unknown"
                        : "Born " + year;
            }

            case ARTIST_YOUNGER_AT_CREATION ->
                    formatArtistAgeCaption(artwork);

            case ARTIST_NATIONALITY,
                 ARTWORK_MEDIUM,
                 ARTWORK_STYLE -> displayValue(artwork, parameter);

            default -> throw unsupported();
        };
    }

    public boolean requiresParameter() {
        return this == ARTIST_NATIONALITY
                || this == ARTWORK_MEDIUM
                || this == ARTWORK_STYLE
                || this == BEFORE_HISTORICAL_EVENT;
    }

    private ArtworkMediumCategory medium(String parameter) {
        return ArtworkMediumCategory.valueOf(required(parameter));
    }

    private HistoricalEvent event(String parameter) {
        return HistoricalEvent.valueOf(required(parameter));
    }

    private String required(String parameter) {
        if (parameter == null || parameter.isBlank()) {
            throw new IllegalStateException(
                    "Question type requires a parameter: " + this
            );
        }

        return parameter;
    }

    private String indefiniteArticle(String value) {
        String requiredValue = required(value);
        char firstLetter = Character.toLowerCase(
                requiredValue.charAt(0)
        );

        return "aeiou".indexOf(firstLetter) >= 0
                ? "an"
                : "a";
    }

    private String valueOrUnknown(String value) {
        return value == null || value.isBlank()
                ? "Unknown"
                : value;
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

    private String formatArtistAgeCaption(
            Artwork artwork
    ) {
        Integer artistBirthYear = artwork.getArtistBeginYear();
        Integer artworkYear = artwork.getObjectBeginYear();

        if (artistBirthYear == null || artworkYear == null) {
            return "Age unknown";
        }

        return "Age " + (artworkYear - artistBirthYear);
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
