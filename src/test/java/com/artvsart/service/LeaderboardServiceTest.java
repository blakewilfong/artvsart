package com.artvsart.service;

import com.artvsart.model.GameMode;
import com.artvsart.model.GameRun;
import com.artvsart.model.LeaderboardEntry;
import com.artvsart.repository.LeaderboardEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeaderboardServiceTest {

    private static final ZoneId GAME_ZONE =
            ZoneId.of("America/Chicago");

    @Mock
    private LeaderboardEntryRepository entryRepository;

    private LeaderboardService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(
                Instant.parse("2026-09-03T17:00:00Z"),
                GAME_ZONE
        );
        service = new LeaderboardService(entryRepository, clock);
    }

    @Test
    void recordsEachCompletedRunOnlyOnce() {
        GameRun run = completedRun(42L, "voter-1", 3);
        LeaderboardEntry existing = new LeaderboardEntry(run, 3);

        when(entryRepository.findByGameRunId(42L))
                .thenReturn(Optional.of(existing));

        LeaderboardEntry result = service.recordCompletedRun(run, 3);

        assertSame(existing, result);
    }

    @Test
    void marksTopTenAndRecordPlacementsAtAchievement() {
        GameRun run = completedRun(42L, "voter-1", 3);
        LeaderboardEntry betterDaily = entry(
                completedRun(41L, "voter-2", 5),
                5,
                1L
        );

        when(entryRepository.findByGameRunId(42L))
                .thenReturn(Optional.empty());

        when(entryRepository.saveAndFlush(any(LeaderboardEntry.class)))
                .thenAnswer(invocation -> {
                    LeaderboardEntry saved = invocation.getArgument(0);
                    ReflectionTestUtils.setField(saved, "id", 2L);
                    return saved;
                });

        when(entryRepository
                .findByGameModeAndDisplayNameIsNotNullAndAchievedAtGreaterThanEqualAndAchievedAtLessThanOrderByScoreDescAchievedAtAscIdAsc(
                        eq(GameMode.STREAK),
                        any(Instant.class),
                        any(Instant.class),
                        any(Pageable.class)
                ))
                .thenReturn(List.of(betterDaily));

        when(entryRepository
                .findByGameModeAndDisplayNameIsNotNullOrderByScoreDescAchievedAtAscIdAsc(
                        eq(GameMode.STREAK),
                        any(Pageable.class)
                ))
                .thenReturn(List.of());

        when(entryRepository.save(any(LeaderboardEntry.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LeaderboardEntry result = service.recordCompletedRun(run, 3);

        assertTrue(result.isNameEligible());
        assertFalse(result.isDailyRecordAtAchievement());
        assertTrue(result.isAllTimeRecordAtAchievement());
        verify(entryRepository).save(result);
    }

    private LeaderboardEntry entry(
            GameRun run,
            int score,
            long entryId
    ) {
        LeaderboardEntry entry = new LeaderboardEntry(run, score);
        ReflectionTestUtils.setField(entry, "id", entryId);
        return entry;
    }

    private GameRun completedRun(
            long runId,
            String voterId,
            int score
    ) {
        GameRun run = GameRun.startStreak(voterId);
        for (int answer = 0; answer < score; answer++) {
            run.recordStreakAnswer(true);
        }
        run.recordStreakAnswer(false);
        ReflectionTestUtils.setField(run, "id", runId);
        return run;
    }
}
