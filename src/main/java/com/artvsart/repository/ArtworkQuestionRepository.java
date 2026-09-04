package com.artvsart.repository;

import com.artvsart.model.ArtworkQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ArtworkQuestionRepository
        extends JpaRepository<ArtworkQuestion, Long> {

    Optional<ArtworkQuestion>
    findByGameRunIdAndRoundNumber(
            Long gameRunId,
            int roundNumber
    );

    List<ArtworkQuestion>
    findAllByGameRunIdOrderByRoundNumberAsc(
            Long gameRunId
    );
}
