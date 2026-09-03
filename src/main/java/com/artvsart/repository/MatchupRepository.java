package com.artvsart.repository;

import com.artvsart.model.Matchup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchupRepository
        extends JpaRepository<Matchup, Long> {
}