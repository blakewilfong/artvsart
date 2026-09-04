package com.artvsart.service;

import com.artvsart.model.GameMode;
import com.artvsart.model.GameRun;
import com.artvsart.repository.GameRunRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GameRunLifecycleService {

    private final GameRunRepository gameRunRepository;

    public GameRunLifecycleService(
            GameRunRepository gameRunRepository
    ) {
        this.gameRunRepository = gameRunRepository;
    }

    @Transactional
    public GameRun startNew(
            String voterId,
            GameMode gameMode
    ) {
        validate(voterId, gameMode);

        gameRunRepository
                .findFirstByVoterIdAndGameModeAndActiveTrueOrderByStartedAtDesc(
                        voterId,
                        gameMode
                )
                .ifPresent(run -> {
                    run.abandon();
                    gameRunRepository.save(run);
                });

        return gameRunRepository.save(
                createRun(voterId, gameMode)
        );
    }

    @Transactional
    public GameRun resumeOrStart(
            String voterId,
            GameMode gameMode
    ) {
        validate(voterId, gameMode);

        return gameRunRepository
                .findFirstByVoterIdAndGameModeAndActiveTrueOrderByStartedAtDesc(
                        voterId,
                        gameMode
                )
                .orElseGet(() -> gameRunRepository.save(
                        createRun(voterId, gameMode)
                ));
    }

    private GameRun createRun(
            String voterId,
            GameMode gameMode
    ) {
        return switch (gameMode) {
            case STREAK -> GameRun.startStreak(voterId);
            case WAGER -> GameRun.startWager(voterId);
            case CROWD -> throw new IllegalArgumentException(
                    "Crowd mode does not use game runs"
            );
        };
    }

    private void validate(
            String voterId,
            GameMode gameMode
    ) {
        if (voterId == null || voterId.isBlank()) {
            throw new IllegalArgumentException(
                    "A voter ID is required"
            );
        }

        if (gameMode == null) {
            throw new IllegalArgumentException(
                    "A game mode is required"
            );
        }
    }
}
