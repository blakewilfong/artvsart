package com.artvsart.service;

import com.artvsart.model.Artwork;
import org.springframework.stereotype.Service;

@Service
public class ArtistBornEarlierQuestionService {

    public static final int MINIMUM_BIRTH_YEAR_DIFFERENCE = 5;

    public boolean isEligiblePair(
            Artwork artworkOne,
            Artwork artworkTwo
    ) {
        if (artworkOne == null
                || artworkTwo == null
                || sameArtwork(artworkOne, artworkTwo)) {
            return false;
        }

        Integer firstBirthYear =
                artworkOne.getArtistBeginYear();

        Integer secondBirthYear =
                artworkTwo.getArtistBeginYear();

        if (firstBirthYear == null
                || secondBirthYear == null) {
            return false;
        }

        String firstArtist =
                artworkOne.getArtistName();

        String secondArtist =
                artworkTwo.getArtistName();

        if (!hasText(firstArtist)
                || !hasText(secondArtist)
                || firstArtist.trim().equalsIgnoreCase(
                secondArtist.trim()
        )) {
            return false;
        }

        long difference = Math.abs(
                (long) firstBirthYear
                        - secondBirthYear
        );

        return difference
                >= MINIMUM_BIRTH_YEAR_DIFFERENCE;
    }

    public Artwork getCorrectArtwork(
            Artwork artworkOne,
            Artwork artworkTwo
    ) {
        if (!isEligiblePair(artworkOne, artworkTwo)) {
            throw new IllegalArgumentException(
                    "Artists must be different and have birth years at least "
                            + MINIMUM_BIRTH_YEAR_DIFFERENCE
                            + " years apart"
            );
        }

        if (artworkOne.getArtistBeginYear()
                < artworkTwo.getArtistBeginYear()) {
            return artworkOne;
        }

        return artworkTwo;
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