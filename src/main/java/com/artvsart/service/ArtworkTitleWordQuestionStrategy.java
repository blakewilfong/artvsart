package com.artvsart.service;

import com.artvsart.model.Artwork;
import com.artvsart.model.QuestionType;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ArtworkTitleWordQuestionStrategy
        implements ArtworkQuestionStrategy {

    private static final Pattern WORD = Pattern.compile(
            "[\\p{L}\\p{M}]+(?:['’][\\p{L}\\p{M}]+)*"
    );

    private static final Set<String> COMMON_WORDS = Set.of(
            "a", "an", "and", "are", "as", "at", "be", "been",
            "being", "but", "by", "de", "des", "du", "el", "for",
            "from", "her", "hers", "his", "i", "il", "in", "into",
            "is", "its", "la", "las", "le", "les", "lo", "los",
            "my", "no", "nor", "not", "of", "on", "onto", "or",
            "our", "ours", "over", "per", "sketch", "so", "study",
            "than", "that", "the", "their", "theirs", "these",
            "this", "those", "through", "title", "to", "under",
            "un", "una", "une", "unknown", "untitled", "up", "upon",
            "version",
            "via", "was", "were", "with", "within", "without",
            "yet", "your", "yours"
    );

    @Override
    public QuestionType getQuestionType() {
        return QuestionType.ARTWORK_TITLE_WORD;
    }

    @Override
    public boolean isEligiblePair(
            Artwork artworkOne,
            Artwork artworkTwo,
            int roundNumber
    ) {
        return selectClue(artworkOne, artworkTwo) != null;
    }

    @Override
    public Artwork getCorrectArtwork(
            Artwork artworkOne,
            Artwork artworkTwo
    ) {
        return requiredClue(artworkOne, artworkTwo).artwork();
    }

    @Override
    public String getQuestionParameter(
            Artwork artworkOne,
            Artwork artworkTwo,
            int roundNumber
    ) {
        return requiredClue(artworkOne, artworkTwo).word();
    }

    private TitleClue requiredClue(
            Artwork artworkOne,
            Artwork artworkTwo
    ) {
        TitleClue clue = selectClue(artworkOne, artworkTwo);

        if (clue == null) {
            throw new IllegalArgumentException(
                    "The artworks require one exclusive meaningful title word"
            );
        }

        return clue;
    }

    private TitleClue selectClue(
            Artwork artworkOne,
            Artwork artworkTwo
    ) {
        Map<String, String> firstWords = titleWords(artworkOne);
        Map<String, String> secondWords = titleWords(artworkTwo);

        if (firstWords.isEmpty() || secondWords.isEmpty()) {
            return null;
        }

        List<TitleClue> clues = new ArrayList<>();

        firstWords.forEach((normalized, display) -> {
            if (!secondWords.containsKey(normalized)) {
                clues.add(new TitleClue(
                        artworkOne,
                        display,
                        normalized
                ));
            }
        });

        secondWords.forEach((normalized, display) -> {
            if (!firstWords.containsKey(normalized)) {
                clues.add(new TitleClue(
                        artworkTwo,
                        display,
                        normalized
                ));
            }
        });

        return clues.stream()
                .sorted(Comparator
                        .comparingInt((TitleClue clue) ->
                                clue.normalized().length())
                        .reversed()
                        .thenComparing(TitleClue::normalized))
                .findFirst()
                .orElse(null);
    }

    private Map<String, String> titleWords(Artwork artwork) {
        Map<String, String> words = new LinkedHashMap<>();

        if (artwork == null || artwork.getTitle() == null) {
            return words;
        }

        Matcher matcher = WORD.matcher(artwork.getTitle());

        while (matcher.find()) {
            String display = matcher.group();
            String normalized = normalize(display);

            if (normalized.length() >= 3
                    && !COMMON_WORDS.contains(normalized)) {
                words.putIfAbsent(normalized, display);
            }
        }

        return words;
    }

    private String normalize(String word) {
        return Normalizer.normalize(word, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }

    private record TitleClue(
            Artwork artwork,
            String word,
            String normalized
    ) {
    }
}
