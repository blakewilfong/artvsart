package com.artvsart.service;

import com.artvsart.model.Artwork;
import com.artvsart.model.EventFamiliarity;
import com.artvsart.model.HistoricalEvent;

import java.util.Comparator;
import java.util.List;
import java.util.SplittableRandom;

/** Stable weighted selection: checking an answer must never reroll its event. */
public final class HistoricalEventSelector {

    private HistoricalEventSelector() {
    }

    public static double weight(EventFamiliarity tier, int round) {
        if (round < 1) {
            throw new IllegalArgumentException("Round must be positive");
        }
        // Guideposts, not gates. Interpolate every round; all tiers stay possible.
        int[] rounds = {1, 10, 20, 30};
        double[][] weights = {{85, 14, 1}, {50, 45, 5},
                {15, 55, 30}, {5, 25, 70}};
        for (int i = 1; i < rounds.length; i++) {
            if (round <= rounds[i]) {
                double progress = (double) (round - rounds[i - 1])
                        / (rounds[i] - rounds[i - 1]);
                return weights[i - 1][tier.ordinal()] * (1 - progress)
                        + weights[i][tier.ordinal()] * progress;
            }
        }
        return weights[3][tier.ordinal()];
    }

    public static HistoricalEvent select(List<HistoricalEvent> eligible,
                                         Artwork first, Artwork second, int round) {
        if (round < 1) {
            throw new IllegalArgumentException("Round must be positive");
        }
        if (eligible.isEmpty()) {
            return null;
        }
        // A symmetric seed makes left/right order irrelevant. SplittableRandom
        // mixes nearby seeds, so sequential artwork IDs do not cluster choices.
        String firstKey = key(first);
        String secondKey = key(second);
        String pair = firstKey.compareTo(secondKey) <= 0
                ? firstKey + "|" + secondKey : secondKey + "|" + firstKey;
        SplittableRandom random = new SplittableRandom(
                ((long) pair.hashCode() << 32) ^ round);
        List<EventFamiliarity> tiers = eligible.stream()
                .map(HistoricalEvent::getFamiliarity).distinct().sorted().toList();
        double ticket = random.nextDouble(tiers.stream()
                .mapToDouble(tier -> weight(tier, round)).sum());
        EventFamiliarity selectedTier = tiers.getLast();
        for (EventFamiliarity tier : tiers) {
            ticket -= weight(tier, round);
            if (ticket < 0) {
                selectedTier = tier;
                break;
            }
        }
        EventFamiliarity tier = selectedTier;
        List<HistoricalEvent> choices = eligible.stream()
                .filter(event -> event.getFamiliarity() == tier)
                .sorted(Comparator.comparing(HistoricalEvent::name)).toList();
        return choices.get(random.nextInt(choices.size()));
    }

    private static String key(Artwork artwork) {
        return artwork.getSource() + ":" + artwork.getSourceArtworkId()
                + ":" + artwork.getId();
    }
}
