package com.artvsart.service;

import com.artvsart.model.Artwork;
import com.artvsart.model.HistoricalEvent;
import com.artvsart.model.QuestionType;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Comparator;

@Component
public class ArtistAliveDuringEventQuestionStrategy
        implements ArtworkQuestionStrategy {

    @Override
    public QuestionType getQuestionType() {
        return QuestionType.ARTIST_ALIVE_DURING_EVENT;
    }

    @Override
    public boolean isEligiblePair(
            Artwork artworkOne,
            Artwork artworkTwo,
            int roundNumber
    ) {
        return selectEvent(artworkOne, artworkTwo) != null;
    }

    @Override
    public Artwork getCorrectArtwork(
            Artwork artworkOne,
            Artwork artworkTwo
    ) {
        HistoricalEvent event = requiredEvent(
                artworkOne,
                artworkTwo
        );

        return wasAlive(artworkOne, event.getYear())
                ? artworkOne
                : artworkTwo;
    }

    @Override
    public String getQuestionParameter(
            Artwork artworkOne,
            Artwork artworkTwo,
            int roundNumber
    ) {
        return requiredEvent(artworkOne, artworkTwo).name();
    }

    private HistoricalEvent requiredEvent(
            Artwork artworkOne,
            Artwork artworkTwo
    ) {
        HistoricalEvent event = selectEvent(
                artworkOne,
                artworkTwo
        );

        if (event == null) {
            throw new IllegalArgumentException(
                    "Two complete lifespans with one artist alive at an event are required"
            );
        }

        return event;
    }

    private HistoricalEvent selectEvent(
            Artwork artworkOne,
            Artwork artworkTwo
    ) {
        if (!hasCompleteLifespan(artworkOne)
                || !hasCompleteLifespan(artworkTwo)) {
            return null;
        }

        return Arrays.stream(HistoricalEvent.values())
                .filter(event -> wasAlive(
                        artworkOne,
                        event.getYear()
                ) != wasAlive(
                        artworkTwo,
                        event.getYear()
                ))
                .max(Comparator
                        .<HistoricalEvent>comparingLong(event -> separationScore(
                                artworkOne,
                                artworkTwo,
                                event.getYear()
                        ))
                        .thenComparingInt(HistoricalEvent::getYear))
                .orElse(null);
    }

    private boolean hasCompleteLifespan(Artwork artwork) {
        return artwork != null
                && artwork.getArtistBeginYear() != null
                && artwork.getArtistEndYear() != null
                && artwork.getArtistBeginYear() != 0
                && artwork.getArtistEndYear() != 0
                && artwork.getArtistBeginYear()
                <= artwork.getArtistEndYear();
    }

    private boolean wasAlive(Artwork artwork, int year) {
        return artwork.getArtistBeginYear() <= year
                && year <= artwork.getArtistEndYear();
    }

    private long separationScore(
            Artwork artworkOne,
            Artwork artworkTwo,
            int year
    ) {
        return distanceFromLifespanBoundary(artworkOne, year)
                + distanceFromLifespanBoundary(artworkTwo, year);
    }

    private long distanceFromLifespanBoundary(
            Artwork artwork,
            int year
    ) {
        int birthYear = artwork.getArtistBeginYear();
        int deathYear = artwork.getArtistEndYear();

        if (year < birthYear) {
            return (long) birthYear - year;
        }

        if (year > deathYear) {
            return (long) year - deathYear;
        }

        return Math.min(
                (long) year - birthYear,
                (long) deathYear - year
        );
    }
}
