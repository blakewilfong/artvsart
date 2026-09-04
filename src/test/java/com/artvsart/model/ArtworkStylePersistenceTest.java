package com.artvsart.model;

import com.artvsart.repository.ArtworkRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
class ArtworkStylePersistenceTest {

    @Autowired
    private ArtworkRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void refreshesAnExistingStyleWithoutInsertingADuplicate() {
        Artwork artwork = new Artwork(
                "nga",
                "style-refresh-test",
                "Test Artwork",
                "Test Artist",
                "1400",
                "image.jpg"
        );
        artwork.replaceStylesFromSource(
                "nga",
                List.of(new Artwork.StyleDefinition(
                        ArtworkStyleType.STYLE,
                        "Gothic",
                        "nga"
                ))
        );
        repository.saveAndFlush(artwork);
        entityManager.clear();

        Artwork persisted = repository
                .findBySourceAndSourceArtworkId(
                        "nga",
                        "style-refresh-test"
                )
                .orElseThrow();
        persisted.replaceStylesFromSource(
                "nga",
                List.of(new Artwork.StyleDefinition(
                        ArtworkStyleType.STYLE,
                        "GOTHIC",
                        "nga"
                ))
        );

        repository.saveAndFlush(persisted);

        assertEquals(1, persisted.getStyles().size());
        assertEquals(
                "GOTHIC",
                persisted.getStyles().getFirst().getDisplayLabel()
        );
    }
}
