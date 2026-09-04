package com.artvsart.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BalancedPoolSelectorTest {

    private final BalancedPoolSelector selector =
            new BalancedPoolSelector();

    @Test
    void takesFromEveryBucketBeforeRepeatingOne() {
        List<Candidate> candidates = List.of(
                new Candidate("portrait", 1),
                new Candidate("portrait", 2),
                new Candidate("portrait", 3),
                new Candidate("landscape", 4),
                new Candidate("landscape", 5),
                new Candidate("abstract", 6)
        );

        assertEquals(
                List.of(1, 4, 6, 2, 5),
                selector.select(
                                candidates,
                                5,
                                Candidate::bucket
                        )
                        .stream()
                        .map(Candidate::id)
                        .toList()
        );
    }

    @Test
    void rejectsNegativeLimits() {
        assertThrows(
                IllegalArgumentException.class,
                () -> selector.select(
                        List.of(),
                        -1,
                        value -> value
                )
        );
    }

    private record Candidate(String bucket, int id) {
    }
}
