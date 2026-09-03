package com.artvsart.repository;

import com.artvsart.model.ArtworkQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ArtworkQuestionRepository
        extends JpaRepository<ArtworkQuestion, Long> {

    Optional<ArtworkQuestion>
    findByGameRunIdAndRoundNumber(
            Long gameRunId,
            int roundNumber
    );
}