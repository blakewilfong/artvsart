package com.artvsart.service;

import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Component;

import static org.junit.jupiter.api.Assertions.assertFalse;

class ArtworkCulturalOriginQuestionStrategyTest {

    @Test
    void isNotRegisteredForQuestionGeneration() {
        assertFalse(
                ArtworkCulturalOriginQuestionStrategy.class
                        .isAnnotationPresent(Component.class)
        );
    }
}
