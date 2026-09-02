package com.artvsart.dto;

public record DailyGameScore(
        int totalRounds,
        int completedRounds,
        int correctPredictions,
        int incorrectPredictions,
        int nextRoundNumber
) {

    public boolean complete() {
        return nextRoundNumber == 0;
    }
}