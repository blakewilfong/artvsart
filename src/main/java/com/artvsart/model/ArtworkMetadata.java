package com.artvsart.model;

public record ArtworkMetadata(
        String originalImageUrl,
        String department,
        String artistNationality,
        Integer artistBeginYear,
        Integer artistEndYear,
        Integer objectBeginYear,
        Integer objectEndYear,
        String culture,
        String country,
        String medium
) {
}