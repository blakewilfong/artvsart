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

    private final StreakDifficultyPolicy difficultyPolicy;

    public BeforeHistoricalEventQuestionStrategy(
            StreakDifficultyPolicy difficultyPolicy
    ) {
        this.difficultyPolicy = difficultyPolicy;
    }

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
        HistoricalEvent event = selectEvent(
                artworkOne,
                artworkTwo
        );

        if (event == null) {
            return false;
        }

        long firstDistance = distanceFromEvent(
                singleYearOf(artworkOne),
                event
        );
        long secondDistance = distanceFromEvent(
                singleYearOf(artworkTwo),
                event
        );

        return difficultyPolicy
                .isHistoricalEventDistanceEligible(
                        Math.min(firstDistance, secondDistance),
                        Math.max(firstDistance, secondDistance),
                        roundNumber
                );
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

        return distanceFromEvent(
                singleYearOf(artworkOne),
                event
        ) < distanceFromEvent(
                singleYearOf(artworkTwo),
                event
        )
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
                    "The artworks must be different distances from a historical event between them"
            );
        }

        return event;
    }

    private HistoricalEvent selectEvent(
            Artwork artworkOne,
            Artwork artworkTwo
    ) {
        Integer firstYear = artworkOne == null
                ? null
                : artworkOne.findSingleCreationYear().orElse(null);
        Integer secondYear = artworkTwo == null
                ? null
                : artworkTwo.findSingleCreationYear().orElse(null);

        if (firstYear == null || secondYear == null
                || firstYear.equals(secondYear)) {
            return null;
        }

        return Arrays.stream(HistoricalEvent.values())
                .filter(event -> isBefore(firstYear, event)
                        != isBefore(secondYear, event))
                .filter(event -> distanceFromEvent(firstYear, event)
                        != distanceFromEvent(secondYear, event))
                .max(Comparator.comparingLong(event ->
                        differenceBetweenDistances(
                                firstYear,
                                secondYear,
                                event
                        )))
                .orElse(null);
    }

    private int singleYearOf(Artwork artwork) {
        return artwork.findSingleCreationYear().orElseThrow(() ->
                new IllegalArgumentException(
                        "Artwork must have one creation year"
                )
        );
    }

    private long differenceBetweenDistances(
            int firstYear,
            int secondYear,
            HistoricalEvent event
    ) {
        return Math.abs(
                distanceFromEvent(firstYear, event)
                        - distanceFromEvent(secondYear, event)
        );
    }

    private long distanceFromEvent(
            int artworkYear,
            HistoricalEvent event
    ) {
        return Math.abs((long) artworkYear - event.getYear());
    }

    private boolean isBefore(
            int artworkYear,
            HistoricalEvent event
    ) {
        return artworkYear < event.getYear();
    }
}
