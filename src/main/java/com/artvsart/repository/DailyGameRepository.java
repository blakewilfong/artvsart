package com.artvsart.repository;

import com.artvsart.model.DailyGame;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyGameRepository
        extends JpaRepository<DailyGame, Long> {

    Optional<DailyGame> findByGameDate(LocalDate gameDate);
}