package com.artvsart.model;

import java.util.Locale;
import java.util.Optional;

public record ArtworkCulturalOrigin(
        Kind kind,
        String displayName
) {

    private static final String PARAMETER_SEPARATOR = "|";

    public ArtworkCulturalOrigin {
        if (kind == null
                || displayName == null
                || displayName.isBlank()) {
            throw new IllegalArgumentException(
                    "A cultural origin kind and name are required"
            );
        }

        displayName = displayName.trim();
    }

    public static Optional<ArtworkCulturalOrigin> find(
            Artwork artwork,
            Kind kind
    ) {
        if (artwork == null || kind == null) {
            return Optional.empty();
        }

        String value = switch (kind) {
            case CULTURE -> artwork.getCulture();
            case COUNTRY -> artwork.getCountry();
        };

        return knownValue(value).map(displayName ->
                new ArtworkCulturalOrigin(kind, displayName)
        );
    }

    public static ArtworkCulturalOrigin fromParameter(
            String parameter
    ) {
        if (parameter == null || parameter.isBlank()) {
            throw new IllegalStateException(
                    "A cultural origin parameter is required"
            );
        }

        int separatorIndex = parameter.indexOf(
                PARAMETER_SEPARATOR
        );

        if (separatorIndex <= 0
                || separatorIndex == parameter.length() - 1) {
            throw new IllegalStateException(
                    "The cultural origin parameter is invalid"
            );
        }

        try {
            return new ArtworkCulturalOrigin(
                    Kind.valueOf(parameter.substring(
                            0,
                            separatorIndex
                    )),
                    parameter.substring(separatorIndex + 1)
            );
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(
                    "The cultural origin parameter is invalid",
                    exception
            );
        }
    }

    public String toParameter() {
        return kind.name()
                + PARAMETER_SEPARATOR
                + displayName;
    }

    public String getPromptSubject() {
        return kind == Kind.CULTURE
                ? displayName + " culture"
                : displayName;
    }

    public boolean matches(Artwork artwork) {
        return find(artwork, kind)
                .map(origin -> displayName.equalsIgnoreCase(
                        origin.displayName()
                ))
                .orElse(false);
    }

    public String displayValue(Artwork artwork) {
        return find(artwork, kind)
                .map(ArtworkCulturalOrigin::displayName)
                .orElse("Unknown");
    }

    private static Optional<String> knownValue(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }

        String firstValue = value.split("[,;]", 2)[0].trim();
        String normalizedValue = firstValue.toLowerCase(Locale.ROOT);

        if (firstValue.isBlank()
                || normalizedValue.equals("unknown")
                || normalizedValue.equals("unidentified")
                || normalizedValue.equals("not recorded")
                || normalizedValue.equals("not applicable")) {
            return Optional.empty();
        }

        return Optional.of(firstValue);
    }

    public enum Kind {
        CULTURE,
        COUNTRY
    }
}
