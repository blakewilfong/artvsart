package com.artvsart.service;

import com.artvsart.model.Artwork;
import com.artvsart.model.QuestionType;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class ArtistNationalityQuestionStrategy
        implements ArtworkQuestionStrategy {

    @Override
    public QuestionType getQuestionType() {
        return QuestionType.ARTIST_NATIONALITY;
    }

    @Override
    public boolean isEligiblePair(
            Artwork artworkOne,
            Artwork artworkTwo,
            int roundNumber
    ) {
        String first = nationality(artworkOne);
        String second = nationality(artworkTwo);

        return first != null
                && second != null
                && !first.equalsIgnoreCase(second);
    }

    @Override
    public Artwork getCorrectArtwork(
            Artwork artworkOne,
            Artwork artworkTwo
    ) {
        String parameter = selectNationality(
                artworkOne,
                artworkTwo
        );

        return parameter.equalsIgnoreCase(nationality(artworkOne))
                ? artworkOne
                : artworkTwo;
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
        return selectNationality(artworkOne, artworkTwo);
    }

    private String selectNationality(
            Artwork artworkOne,
            Artwork artworkTwo
    ) {
        String first = nationality(artworkOne);
        String second = nationality(artworkTwo);

        if (first == null || second == null
                || first.equalsIgnoreCase(second)) {
            throw new IllegalArgumentException(
                    "Two different known nationalities are required"
            );
        }

        return first.compareToIgnoreCase(second) <= 0
                ? first
                : second;
    }

    private String nationality(Artwork artwork) {
        if (artwork == null
                || artwork.getArtistNationality() == null) {
            return null;
        }

        String nationality = artwork.getArtistNationality()
                .split("[,;]", 2)[0]
                .trim();

        if (nationality.isBlank()
                || "unknown".equals(nationality
                .toLowerCase(Locale.ROOT))) {
            return null;
        }

        return nationality;
    }
}
