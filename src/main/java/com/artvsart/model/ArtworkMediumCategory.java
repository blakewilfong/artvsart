package com.artvsart.model;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public enum ArtworkMediumCategory {
    FRESCO("fresco", "fresco"),
    GOUACHE("gouache", "gouache"),
    GRAPHITE("graphite", "graphite"),
    INK("ink", "ink"),
    OIL("oil paint", "oil"),
    PASTEL("pastel", "pastel"),
    TEMPERA("tempera", "tempera"),
    WATERCOLOR("watercolor", "watercolor", "watercolour"),
    WOODBLOCK_PRINT("woodblock printing", "woodblock print");

    private final String displayName;
    private final List<String> searchTerms;

    ArtworkMediumCategory(
            String displayName,
            String... searchTerms
    ) {
        this.displayName = displayName;
        this.searchTerms = Arrays.asList(searchTerms);
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean matches(Artwork artwork) {
        if (artwork == null
                || artwork.getMedium() == null) {
            return false;
        }

        String medium = artwork.getMedium()
                .toLowerCase(Locale.ROOT);

        return searchTerms.stream().anyMatch(medium::contains);
    }
}
