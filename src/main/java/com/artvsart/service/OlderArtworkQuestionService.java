package com.artvsart.service;

import com.artvsart.model.Artwork;
import org.springframework.stereotype.Service;

@Service
public class OlderArtworkQuestionService {

    public static final int MINIMUM_YEAR_DIFFERENCE = 25;

    public boolean isEligiblePair(
            Artwork artworkOne,
            Artwork artworkTwo
    ) {
        if (artworkOne == null
                || artworkTwo == null
                || artworkOne == artworkTwo) {
            return false;
        }

        Integer firstYear =
                artworkOne.getObjectBeginYear();

        Integer secondYear =
                artworkTwo.getObjectBeginYear();

        if (firstYear == null || secondYear == null) {
            return false;
        }

        long difference = Math.abs(
                (long) firstYear - secondYear
        );

        return difference >= MINIMUM_YEAR_DIFFERENCE;
    }

    public Artwork getCorrectArtwork(
            Artwork artworkOne,
            Artwork artworkTwo
    ) {
        if (!isEligiblePair(artworkOne, artworkTwo)) {
            throw new IllegalArgumentException(
                    "Artworks must have dates at least "
                            + MINIMUM_YEAR_DIFFERENCE
                            + " years apart"
            );
        }

        if (artworkOne.getObjectBeginYear()
                < artworkTwo.getObjectBeginYear()) {
            return artworkOne;
        }

        return artworkTwo;
    }

    public boolean isCorrect(
            Long selectedArtworkId,
            Artwork artworkOne,
            Artwork artworkTwo
    ) {
        if (selectedArtworkId == null) {
            throw new IllegalArgumentException(
                    "A selected artwork ID is required"
            );
        }

        Artwork correctArtwork = getCorrectArtwork(
                artworkOne,
                artworkTwo
        );

        return selectedArtworkId.equals(
                correctArtwork.getId()
        );
    }
}