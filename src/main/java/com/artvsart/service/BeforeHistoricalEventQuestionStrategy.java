package com.artvsart.service;

import com.artvsart.model.Artwork;
import com.artvsart.model.HistoricalEvent;
import com.artvsart.model.QuestionType;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class BeforeHistoricalEventQuestionStrategy implements ArtworkQuestionStrategy {

    private final StreakDifficultyPolicy difficultyPolicy;

    public BeforeHistoricalEventQuestionStrategy(StreakDifficultyPolicy difficultyPolicy) {
        this.difficultyPolicy = difficultyPolicy;
    }

    @Override
    public QuestionType getQuestionType() {
        return QuestionType.BEFORE_HISTORICAL_EVENT;
    }

    @Override
    public boolean isEligiblePair(Artwork first, Artwork second, int round) {
        return !eligibleEvents(first, second, round).isEmpty();
    }

    @Override
    public Artwork getCorrectArtwork(Artwork first, Artwork second) {
        return getCorrectArtwork(first, second, 1);
    }

    @Override
    public Artwork getCorrectArtwork(Artwork first, Artwork second, int round) {
        HistoricalEvent event = requiredEvent(first, second, round);
        return distance(first.findSingleCreationYear().orElseThrow(), event)
                < distance(second.findSingleCreationYear().orElseThrow(), event)
                ? first : second;
    }

    @Override
    public String getQuestionParameter(Artwork first, Artwork second, int round) {
        return requiredEvent(first, second, round).name();
    }

    private HistoricalEvent requiredEvent(Artwork first, Artwork second, int round) {
        HistoricalEvent event = HistoricalEventSelector.select(
                eligibleEvents(first, second, round), first, second, round);
        if (event == null) {
            throw new IllegalArgumentException(
                    "No historical event meets the artwork dates and round difficulty");
        }
        return event;
    }

    private List<HistoricalEvent> eligibleEvents(Artwork first, Artwork second, int round) {
        Integer firstYear = first == null ? null
                : first.findSingleCreationYear().orElse(null);
        Integer secondYear = second == null ? null
                : second.findSingleCreationYear().orElse(null);
        if (firstYear == null || secondYear == null || firstYear.equals(secondYear)) {
            return List.of();
        }
        return Arrays.stream(HistoricalEvent.values())
                .filter(event -> (firstYear < event.getYear())
                        != (secondYear < event.getYear()))
                .filter(event -> difficultyPolicy.isHistoricalEventDistanceEligible(
                        Math.min(distance(firstYear, event), distance(secondYear, event)),
                        Math.max(distance(firstYear, event), distance(secondYear, event)),
                        round))
                .toList();
    }

    private long distance(int year, HistoricalEvent event) {
        return Math.abs((long) year - event.getYear());
    }
}
