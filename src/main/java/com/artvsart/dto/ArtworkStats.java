package com.artvsart.dto;

public record ArtworkStats(
        long selections,
        long presentations
) {

    public long nonSelections() {
        return presentations - selections;
    }

    public double selectionPercentage() {
        if (presentations == 0) {
            return 50.0;
        }

        return selections * 100.0 / presentations;
    }

    public ArtworkStats afterSelection() {
        return new ArtworkStats(
                selections + 1,
                presentations + 1
        );
    }

    public ArtworkStats afterNonSelection() {
        return new ArtworkStats(
                selections,
                presentations + 1
        );
    }
}