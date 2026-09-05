package com.artvsart.service;

import com.artvsart.model.Artwork;
import com.artvsart.model.EventFamiliarity;
import com.artvsart.model.HistoricalEvent;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HistoricalEventSelectorTest {
    @Test
    void weightsMoveSmoothlyAcrossTheTenRoundGuideposts() {
        assertEquals(85, HistoricalEventSelector.weight(EventFamiliarity.FAMILIAR, 1));
        assertEquals(45, HistoricalEventSelector.weight(EventFamiliarity.RECOGNIZABLE, 10));
        assertEquals(55, HistoricalEventSelector.weight(EventFamiliarity.RECOGNIZABLE, 20));
        assertEquals(70, HistoricalEventSelector.weight(EventFamiliarity.OBSCURE, 30));
        for (int round = 1; round <= 100; round++) {
            double total = 0;
            for (EventFamiliarity tier : EventFamiliarity.values()) {
                double weight = HistoricalEventSelector.weight(tier, round);
                assertTrue(weight > 0);
                assertTrue(Math.abs(weight - HistoricalEventSelector.weight(tier, round + 1)) < 5);
                total += weight;
            }
            assertEquals(100, total, 0.00001);
            assertTrue(HistoricalEventSelector.weight(EventFamiliarity.OBSCURE, round + 1)
                    >= HistoricalEventSelector.weight(EventFamiliarity.OBSCURE, round));
        }
        assertThrows(IllegalArgumentException.class,
                () -> HistoricalEventSelector.weight(EventFamiliarity.FAMILIAR, 0));
    }

    @Test
    void selectionIsStableSymmetricAndIndependentOfListOrder() {
        Artwork first = artwork("1");
        Artwork second = artwork("2");
        List<HistoricalEvent> events = Arrays.asList(HistoricalEvent.values());
        for (int round : new int[]{1, 10, 11, 20, 21, 40}) {
            HistoricalEvent selected = HistoricalEventSelector.select(events, first, second, round);
            assertSame(selected, HistoricalEventSelector.select(events, second, first, round));
            assertSame(selected, HistoricalEventSelector.select(events.reversed(), first, second, round));
        }
    }

    @Test
    void observedMixTracksTierWeightsRatherThanTierSizes() {
        List<HistoricalEvent> events = Arrays.asList(HistoricalEvent.values());
        for (int round : new int[]{1, 10, 20, 30}) {
            EnumMap<EventFamiliarity, Integer> counts = new EnumMap<>(EventFamiliarity.class);
            for (int i = 0; i < 10000; i++) {
                HistoricalEvent selected = HistoricalEventSelector.select(
                        events, artwork("left-" + i), artwork("right-" + i), round);
                counts.merge(selected.getFamiliarity(), 1, Integer::sum);
            }
            for (EventFamiliarity tier : EventFamiliarity.values()) {
                assertEquals(HistoricalEventSelector.weight(tier, round),
                        counts.getOrDefault(tier, 0) / 100.0, 2.0);
            }
        }
    }

    @Test
    void fallsBackToAvailableTiersAndReachesEveryEvent() {
        assertNull(HistoricalEventSelector.select(List.of(), artwork("1"), artwork("2"), 1));
        assertSame(HistoricalEvent.GREAT_STINK, HistoricalEventSelector.select(
                List.of(HistoricalEvent.GREAT_STINK), artwork("1"), artwork("2"), 1));
        HashSet<HistoricalEvent> selected = new HashSet<>();
        for (int i = 0; i < 10000; i++) {
            selected.add(HistoricalEventSelector.select(Arrays.asList(HistoricalEvent.values()),
                    artwork("left-" + i), artwork("right-" + i), 25));
        }
        assertEquals(HistoricalEvent.values().length, selected.size());
    }

    private Artwork artwork(String id) {
        return new Artwork("test", id, "Title", "Artist", "1800", "image.jpg");
    }
}
