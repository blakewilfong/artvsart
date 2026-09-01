package com.artvsart.repository;

import com.artvsart.model.Matchup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface MatchupRepository extends JpaRepository<Matchup, Long> {

    Optional<Matchup> findByMatchupDate(LocalDate matchupDate);
}