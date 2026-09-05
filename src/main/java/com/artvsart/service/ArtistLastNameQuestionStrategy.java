package com.artvsart.service;

import com.artvsart.model.Artwork;
import com.artvsart.model.QuestionType;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;

@Component
public class ArtistLastNameQuestionStrategy
        implements ArtworkQuestionStrategy {

    private static final Set<String> NAME_PARTICLES = Set.of(
            "da", "de", "del", "della", "der", "di", "du",
            "la", "le", "ten", "ter", "van", "von"
    );

    private static final Set<String> NAME_SUFFIXES = Set.of(
            "ii", "iii", "iv", "jr", "jr.", "sr", "sr."
    );

    private static final Set<String> ATTRIBUTION_PREFIXES = Set.of(
            "after ",
            "artist unknown",
            "attributed to ",
            "circle of ",
            "follower of ",
            "formerly attributed to ",
            "manner of ",
            "master of ",
            "possibly ",
            "school of ",
            "studio of ",
            "workshop of "
    );

    @Override
    public QuestionType getQuestionType() {
        return QuestionType.ARTIST_LAST_NAME;
    }

    @Override
    public boolean isEligiblePair(
            Artwork artworkOne,
            Artwork artworkTwo,
            int roundNumber
    ) {
        return selectArtist(artworkOne, artworkTwo) != null;
    }

    @Override
    public Artwork getCorrectArtwork(
            Artwork artworkOne,
            Artwork artworkTwo
    ) {
        return requiredArtist(artworkOne, artworkTwo).artwork();
    }

    @Override
    public boolean usesArtistPopularityDifficulty() {
        return true;
    }

    @Override
    public String getQuestionParameter(
            Artwork artworkOne,
            Artwork artworkTwo,
            int roundNumber
    ) {
        return requiredArtist(artworkOne, artworkTwo).lastName();
    }

    private ArtistSelection requiredArtist(
            Artwork artworkOne,
            Artwork artworkTwo
    ) {
        ArtistSelection selection = selectArtist(
                artworkOne,
                artworkTwo
        );

        if (selection == null) {
            throw new IllegalArgumentException(
                    "Two artists with different known last names are required"
            );
        }

        return selection;
    }

    private ArtistSelection selectArtist(
            Artwork artworkOne,
            Artwork artworkTwo
    ) {
        String first = lastName(artworkOne);
        String second = lastName(artworkTwo);

        if (first == null || second == null) {
            return null;
        }

        String firstKey = comparisonKey(first);
        String secondKey = comparisonKey(second);

        if (firstKey.equals(secondKey)) {
            return null;
        }

        return firstKey.compareTo(secondKey) <= 0
                ? new ArtistSelection(artworkOne, first)
                : new ArtistSelection(artworkTwo, second);
    }

    private String comparisonKey(String lastName) {
        return Normalizer.normalize(lastName, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT);
    }

    private String lastName(Artwork artwork) {
        if (artwork == null || artwork.getArtistName() == null) {
            return null;
        }

        String name = artwork.getArtistName().trim();
        String normalized = name.toLowerCase(Locale.ROOT);

        if (name.isBlank()
                || normalized.contains("unknown")
                || normalized.contains("anonymous")
                || normalized.contains("unidentified")
                || normalized.equals("various artists")
                || normalized.contains(" and ")
                || normalized.contains(";")
                || normalized.contains("&")
                || ATTRIBUTION_PREFIXES.stream()
                .anyMatch(normalized::startsWith)) {
            return null;
        }

        int detailsStart = name.lastIndexOf(" (");

        if (detailsStart > 0 && name.endsWith(")")) {
            name = name.substring(0, detailsStart).trim();
        }

        int comma = name.indexOf(',');

        if (comma > 0) {
            return validLastName(name.substring(0, comma));
        }

        String[] parts = name.split("\\s+");
        int lastIndex = parts.length - 1;

        while (lastIndex > 0 && NAME_SUFFIXES.contains(
                parts[lastIndex].toLowerCase(Locale.ROOT)
        )) {
            lastIndex--;
        }

        int firstIndex = lastIndex;

        while (firstIndex > 0 && NAME_PARTICLES.contains(
                parts[firstIndex - 1].toLowerCase(Locale.ROOT)
        )) {
            firstIndex--;
        }

        return validLastName(String.join(
                " ",
                Arrays.copyOfRange(parts, firstIndex, lastIndex + 1)
        ));
    }

    private String validLastName(String candidate) {
        String lastName = candidate.trim();

        return lastName.length() >= 2
                && lastName.codePoints().anyMatch(Character::isLetter)
                ? lastName
                : null;
    }

    private record ArtistSelection(
            Artwork artwork,
            String lastName
    ) {
    }
}
