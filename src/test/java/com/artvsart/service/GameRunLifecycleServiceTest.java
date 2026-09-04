package com.artvsart.service;

import com.artvsart.model.GameMode;
import com.artvsart.model.GameRun;
import com.artvsart.repository.GameRunRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameRunLifecycleServiceTest {

    private static final String VOTER_ID = "voter-1";

    @Mock
    private GameRunRepository gameRunRepository;

    private GameRunLifecycleService service;

    @BeforeEach
    void setUp() {
        service = new GameRunLifecycleService(
                gameRunRepository
        );
    }

    @Test
    void startingNewAbandonsTheActiveRun() {
        GameRun activeRun = GameRun.startStreak(VOTER_ID);

        when(gameRunRepository
                .findFirstByVoterIdAndGameModeAndActiveTrueOrderByStartedAtDesc(
                        VOTER_ID,
                        GameMode.STREAK
                ))
                .thenReturn(Optional.of(activeRun));
        when(gameRunRepository.save(any(GameRun.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        GameRun newRun = service.startNew(
                VOTER_ID,
                GameMode.STREAK
        );

        assertFalse(activeRun.isActive());
        assertTrue(newRun.isActive());
        assertNotSame(activeRun, newRun);
        verify(gameRunRepository).save(activeRun);
        verify(gameRunRepository).save(newRun);
    }

    @Test
    void continuingReturnsTheActiveRun() {
        GameRun activeRun = GameRun.startWager(VOTER_ID);

        when(gameRunRepository
                .findFirstByVoterIdAndGameModeAndActiveTrueOrderByStartedAtDesc(
                        VOTER_ID,
                        GameMode.WAGER
                ))
                .thenReturn(Optional.of(activeRun));

        GameRun result = service.resumeOrStart(
                VOTER_ID,
                GameMode.WAGER
        );

        assertSame(activeRun, result);
    }

    @Test
    void continuingStartsWhenNoActiveRunExists() {
        when(gameRunRepository
                .findFirstByVoterIdAndGameModeAndActiveTrueOrderByStartedAtDesc(
                        VOTER_ID,
                        GameMode.WAGER
                ))
                .thenReturn(Optional.empty());
        when(gameRunRepository.save(any(GameRun.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        GameRun result = service.resumeOrStart(
                VOTER_ID,
                GameMode.WAGER
        );

        assertTrue(result.isActive());
        assertSame(GameMode.WAGER, result.getGameMode());
    }
}
