package com.artvsart.service;

import com.artvsart.model.Artwork;
import com.artvsart.model.GameMode;
import com.artvsart.model.GameRun;
import com.artvsart.model.QuestionType;
import org.springframework.stereotype.Component;

@Component
public class ArtworkCenturyQuestionStrategy
        implements ArtworkQuestionStrategy {

    private final StreakDifficultyPolicy streakDifficultyPolicy;

    public ArtworkCenturyQuestionStrategy(
            StreakDifficultyPolicy streakDifficultyPolicy
    ) {
        this.streakDifficultyPolicy = streakDifficultyPolicy;
    }

    @Override
    public QuestionType getQuestionType() {
        return QuestionType.ARTWORK_CENTURY;
    }

    @Override
    public boolean isEligiblePair(
            Artwork artworkOne,
            Artwork artworkTwo,
            GameRun run
    ) {
        if (!isEligiblePair(
                artworkOne,
                artworkTwo,
                run.getRoundNumber()
        )) {
            return false;
        }

        if (run.getGameMode() != GameMode.STREAK) {
            return true;
        }

        long difference = Math.abs(
                (long) artworkOne.getObjectBeginYear()
                        - artworkTwo.getObjectBeginYear()
        );

        return streakDifficultyPolicy
                .isArtworkYearDifferenceEligible(
                        difference,
                        run.getRoundNumber()
                );
    }

    @Override
    public boolean isEligiblePair(
            Artwork artworkOne,
            Artwork artworkTwo,
            int roundNumber
    ) {
        Integer firstCentury = centuryOf(artworkOne);
        Integer secondCentury = centuryOf(artworkTwo);

        return firstCentury != null
                && secondCentury != null
                && !firstCentury.equals(secondCentury)
                && !identityOf(artworkOne)
                .equals(identityOf(artworkTwo));
    }

    @Override
    public Artwork getCorrectArtwork(
            Artwork artworkOne,
            Artwork artworkTwo
    ) {
        requireEligiblePair(artworkOne, artworkTwo);
        return selectTargetArtwork(artworkOne, artworkTwo);
    }

    @Override
    public String getQuestionParameter(
            Artwork artworkOne,
            Artwork artworkTwo,
            int roundNumber
    ) {
        requireEligiblePair(artworkOne, artworkTwo);
        return formatCentury(centuryOf(
                selectTargetArtwork(artworkOne, artworkTwo)
        ));
    }

    private void requireEligiblePair(
            Artwork artworkOne,
            Artwork artworkTwo
    ) {
        if (!isEligiblePair(artworkOne, artworkTwo, 1)) {
            throw new IllegalArgumentException(
                    "Two unambiguously dated artworks from different centuries are required"
            );
        }
    }

    private Artwork selectTargetArtwork(
            Artwork artworkOne,
            Artwork artworkTwo
    ) {
        String firstIdentity = identityOf(artworkOne);
        String secondIdentity = identityOf(artworkTwo);
        boolean firstSortsEarlier = firstIdentity.compareTo(
                secondIdentity
        ) < 0;

        Artwork lowerIdentityArtwork = firstSortsEarlier
                ? artworkOne
                : artworkTwo;
        Artwork higherIdentityArtwork = firstSortsEarlier
                ? artworkTwo
                : artworkOne;

        String signature = identityOf(lowerIdentityArtwork)
                + "|"
                + identityOf(higherIdentityArtwork);

        return Math.floorMod(signature.hashCode(), 2) == 0
                ? lowerIdentityArtwork
                : higherIdentityArtwork;
    }

    private String identityOf(Artwork artwork) {
        if (artwork == null
                || artwork.getSource() == null
                || artwork.getSourceArtworkId() == null) {
            return "";
        }

        return artwork.getSource()
                + ":"
                + artwork.getSourceArtworkId();
    }

    private Integer centuryOf(Artwork artwork) {
        if (artwork == null) {
            return null;
        }

        return artwork.findSingleCreationYear()
                .map(this::centuryOfYear)
                .orElse(null);
    }

    private int centuryOfYear(int year) {
        int century = (Math.abs(year) - 1) / 100 + 1;
        return year < 0 ? -century : century;
    }

    private String formatCentury(int century) {
        int number = Math.abs(century);
        int remainder = number % 100;
        String suffix;

        if (remainder >= 11 && remainder <= 13) {
            suffix = "th";
        } else {
            suffix = switch (number % 10) {
                case 1 -> "st";
                case 2 -> "nd";
                case 3 -> "rd";
                default -> "th";
            };
        }

        return number
                + suffix
                + " century"
                + (century < 0 ? " BCE" : "");
    }
}
