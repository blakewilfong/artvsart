package com.artvsart.service;

import com.artvsart.model.Artwork;
import com.artvsart.model.HistoricalEvent;
import com.artvsart.model.QuestionType;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class ArtistAliveDuringEventQuestionStrategy implements ArtworkQuestionStrategy {

    @Override
    public QuestionType getQuestionType() {
        return QuestionType.ARTIST_ALIVE_DURING_EVENT;
    }

    @Override
    public boolean isEligiblePair(Artwork first, Artwork second, int round) {
        return !eligibleEvents(first, second).isEmpty();
    }

    @Override
    public Artwork getCorrectArtwork(Artwork first, Artwork second) {
        return getCorrectArtwork(first, second, 1);
    }

    @Override
    public Artwork getCorrectArtwork(Artwork first, Artwork second, int round) {
        return wasAlive(first, requiredEvent(first, second, round).getYear())
                ? first : second;
    }

    @Override
    public String getQuestionParameter(Artwork first, Artwork second, int round) {
        return requiredEvent(first, second, round).name();
    }

    private HistoricalEvent requiredEvent(Artwork first, Artwork second, int round) {
        HistoricalEvent event = HistoricalEventSelector.select(
                eligibleEvents(first, second), first, second, round);
        if (event == null) {
            throw new IllegalArgumentException(
                    "Two complete lifespans with one artist alive at an event are required");
        }
        return event;
    }

    private List<HistoricalEvent> eligibleEvents(Artwork first, Artwork second) {
        if (!hasCompleteLifespan(first) || !hasCompleteLifespan(second)) {
            return List.of();
        }
        return Arrays.stream(HistoricalEvent.values())
                .filter(event -> wasAlive(first, event.getYear())
                        != wasAlive(second, event.getYear()))
                .toList();
    }

    private boolean hasCompleteLifespan(Artwork artwork) {
        return artwork != null
                && artwork.getArtistBeginYear() != null
                && artwork.getArtistEndYear() != null
                && artwork.getArtistBeginYear() != 0
                && artwork.getArtistEndYear() != 0
                && artwork.getArtistBeginYear() <= artwork.getArtistEndYear();
    }

    private boolean wasAlive(Artwork artwork, int year) {
        return artwork.getArtistBeginYear() <= year && year <= artwork.getArtistEndYear();
    }
}
