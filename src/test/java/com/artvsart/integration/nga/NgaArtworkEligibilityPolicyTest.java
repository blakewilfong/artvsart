package com.artvsart.integration.nga;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NgaArtworkEligibilityPolicyTest {

    private final NgaArtworkEligibilityPolicy policy =
            new NgaArtworkEligibilityPolicy();

    @Test
    void acceptsDatedPainting() {
        assertTrue(policy.isEligible(
                "River",
                "1900",
                1900,
                "Painting",
                null,
                null,
                "Oil on canvas",
                false
        ));
    }

    @Test
    void acceptsCollage() {
        assertTrue(policy.isEligible(
                "Composition",
                "1965",
                1965,
                null,
                "Collage",
                null,
                "Paper on board",
                false
        ));
    }

    @Test
    void rejectsPaintedSculpture() {
        assertFalse(policy.isEligible(
                "Figure",
                "1900",
                1900,
                "Sculpture",
                null,
                null,
                "Painted bronze",
                false
        ));
    }

    @Test
    void rejectsVirtualOrUndatedRecords() {
        assertFalse(policy.isEligible(
                "Set",
                "1900",
                1900,
                "Painting",
                null,
                null,
                "Oil on canvas",
                true
        ));

        assertFalse(policy.isEligible(
                "Unknown",
                null,
                null,
                "Painting",
                null,
                null,
                "Oil on canvas",
                false
        ));
    }
}
