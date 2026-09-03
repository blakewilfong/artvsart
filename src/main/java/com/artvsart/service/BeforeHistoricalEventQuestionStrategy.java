package com.artvsart.service;

import com.artvsart.model.Artwork;
import com.artvsart.model.HistoricalEvent;
import com.artvsart.model.QuestionType;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Comparator;

@Component
public class BeforeHistoricalEventQuestionStrategy
        implements ArtworkQuestionStrategy {

    @Override
    public QuestionType getQuestionType() {
        return QuestionType.BEFORE_HISTORICAL_EVENT;
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

        return artworkOne.getObjectBeginYear() < event.getYear()
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
        HistoricalEvent event = selectEvent(artworkOne, artworkTwo);

        if (event == null) {
            throw new IllegalArgumentException(
                    "The artworks must straddle a historical event"
            );
        }

        return event;
    }

    private HistoricalEvent selectEvent(
            Artwork artworkOne,
            Artwork artworkTwo
    ) {
        Integer firstYear = artworkOne.getObjectBeginYear();
        Integer secondYear = artworkTwo.getObjectBeginYear();

        if (firstYear == null || secondYear == null
                || firstYear.equals(secondYear)) {
            return null;
        }

        return Arrays.stream(HistoricalEvent.values())
                .filter(event -> isBefore(firstYear, event)
                        != isBefore(secondYear, event))
                .min(Comparator.comparingLong(event ->
                        distanceFromMidpoint(
                                firstYear,
                                secondYear,
                                event
                        )))
                .orElse(null);
    }

    private long distanceFromMidpoint(
            int firstYear,
            int secondYear,
            HistoricalEvent event
    ) {
        return Math.abs(
                (long) firstYear
                        + secondYear
                        - 2L * event.getYear()
        );
    }

    private boolean isBefore(
            int artworkYear,
            HistoricalEvent event
    ) {
        return artworkYear < event.getYear();
    }
}
