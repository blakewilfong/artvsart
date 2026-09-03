package com.artvsart.repository;

import com.artvsart.model.ArtworkQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArtworkQuestionRepository
        extends JpaRepository<ArtworkQuestion, Long> {
}