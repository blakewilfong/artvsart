package com.artvsart.repository;

import com.artvsart.model.ArtworkAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ArtworkAnswerRepository
        extends JpaRepository<ArtworkAnswer, Long> {

    Optional<ArtworkAnswer> findByQuestionIdAndVoterId(
            Long questionId,
            String voterId
    );
}