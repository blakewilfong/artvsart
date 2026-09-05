package com.artvsart.model;

import org.junit.jupiter.api.Test;
import java.net.URI;
import static org.junit.jupiter.api.Assertions.*;

class HistoricalEventTest {
    @Test
    void everyEventHasAnExplicitTierAndDirectWikipediaLink() {
        int previousYear = Integer.MIN_VALUE;
        for (HistoricalEvent event : HistoricalEvent.values()) {
            assertNotNull(event.getFamiliarity());
            URI uri = URI.create(event.getWikipediaUrl());
            assertEquals("https", uri.getScheme());
            assertEquals("en.wikipedia.org", uri.getHost());
            assertTrue(uri.getPath().startsWith("/wiki/"));
            assertTrue(uri.getPath().length() > 6);
            assertTrue(event.getYear() >= previousYear);
            previousYear = event.getYear();
        }
        assertEquals(106, HistoricalEvent.values().length);
        assertEquals(1858, HistoricalEvent.GREAT_STINK.getYear());
        assertNotNull(HistoricalEvent.GREAT_STINK.getSummary());
        assertEquals(1848, HistoricalEvent.REVOLUTIONS_OF_1848.getYear());
        assertTrue(HistoricalEvent.DURERS_RHINOCEROS.getWikipediaUrl().contains("%C3%BC"));
    }
}
