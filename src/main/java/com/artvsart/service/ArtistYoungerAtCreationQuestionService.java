package com.artvsart.service;

import com.artvsart.model.Artwork;
import org.springframework.stereotype.Service;

@Service
public class ArtistYoungerAtCreationQuestionService {

    public static final int MINIMUM_AGE_DIFFERENCE = 5;

    private static final int MINIMUM_PLAUSIBLE_AGE = 15;
    private static final int MAXIMUM_PLAUSIBLE_AGE = 100;

    public boolean isEligiblePair(
            Artwork artworkOne,
            Artwork artworkTwo
    ) {
        if (artworkOne == null
                || artworkTwo == null
                || sameArtwork(artworkOne, artworkTwo)) {
            return false;
        }

        if (!hasKnownDistinctArtists(
                artworkOne,
                artworkTwo
        )) {
            return false;
        }

        Integer firstAge =
                findArtistAgeAtCreation(artworkOne);

        Integer secondAge =
                findArtistAgeAtCreation(artworkTwo);

        if (firstAge == null || secondAge == null) {
            return false;
        }

        long difference = Math.abs(
                (long) firstAge - secondAge
        );

        return difference >= MINIMUM_AGE_DIFFERENCE;
    }

    public Artwork getCorrectArtwork(
            Artwork artworkOne,
            Artwork artworkTwo
    ) {
        if (!isEligiblePair(artworkOne, artworkTwo)) {
            throw new IllegalArgumentException(
                    "Artworks must have valid artist ages at least "
                            + MINIMUM_AGE_DIFFERENCE
                            + " years apart"
            );
        }

        int firstAge =
                getArtistAgeAtCreation(artworkOne);

        int secondAge =
                getArtistAgeAtCreation(artworkTwo);

        if (firstAge < secondAge) {
            return artworkOne;
        }

        return artworkTwo;
    }

    public int getArtistAgeAtCreation(
            Artwork artwork
    ) {
        Integer age = findArtistAgeAtCreation(artwork);

        if (age == null) {
            throw new IllegalArgumentException(
                    "Artwork does not have a plausible artist age"
            );
        }

        return age;
    }

    private Integer findArtistAgeAtCreation(
            Artwork artwork
    ) {
        if (artwork == null
                || artwork.getArtistBeginYear() == null) {
            return null;
        }

        Integer creationYear = artwork.findSingleCreationYear()
                .orElse(null);

        if (creationYear == null) {
            return null;
        }

        int age = creationYear
                - artwork.getArtistBeginYear();

        if (age < MINIMUM_PLAUSIBLE_AGE
                || age > MAXIMUM_PLAUSIBLE_AGE) {
            return null;
        }

        return age;
    }

    private boolean hasKnownDistinctArtists(
            Artwork artworkOne,
            Artwork artworkTwo
    ) {
        String firstArtist =
                artworkOne.getArtistName();

        String secondArtist =
                artworkTwo.getArtistName();

        return hasText(firstArtist)
                && hasText(secondArtist)
                && !firstArtist.trim().equalsIgnoreCase(
                secondArtist.trim()
        );
    }

    private boolean sameArtwork(
            Artwork first,
            Artwork second
    ) {
        if (first == second) {
            return true;
        }

        return first.getId() != null
                && first.getId().equals(second.getId());
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
