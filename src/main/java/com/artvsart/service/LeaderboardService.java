package com.artvsart.service;

import com.artvsart.model.GameMode;
import com.artvsart.model.GameRun;
import com.artvsart.model.LeaderboardEntry;
import com.artvsart.repository.LeaderboardEntryRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class LeaderboardService {

    private static final int LEADERBOARD_SIZE = 10;

    private final LeaderboardEntryRepository entryRepository;
    private final Clock clock;

    public LeaderboardService(
            LeaderboardEntryRepository entryRepository,
            Clock leaderboardClock
    ) {
        this.entryRepository = entryRepository;
        this.clock = leaderboardClock;
    }

    @Transactional
    public LeaderboardEntry recordCompletedRun(
            GameRun run,
            int score
    ) {
        if (run.getId() == null) {
            throw new IllegalArgumentException(
                    "The completed run must be saved first"
            );
        }

        return entryRepository.findByGameRunId(run.getId())
                .orElseGet(() -> createEntry(run, score));
    }

    private LeaderboardEntry createEntry(
            GameRun run,
            int score
    ) {
        LeaderboardEntry entry = entryRepository.saveAndFlush(
                new LeaderboardEntry(run, score)
        );

        int dailyRank = rankAmongNamedEntries(
                getDailyEntries(
                        run.getGameMode(),
                        localDateOf(run.getCompletedAt())
                ),
                entry
        );

        int allTimeRank = rankAmongNamedEntries(
                getAllTimeEntries(run.getGameMode()),
                entry
        );

        entry.recordQualification(
                dailyRank,
                allTimeRank
        );

        return entryRepository.save(entry);
    }

    @Transactional(readOnly = true)
    public LeaderboardView getView(
            GameMode gameMode,
            String voterId,
            Long runId
    ) {
        LeaderboardEntry currentEntry = entryRepository
                .findByGameRunId(runId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Leaderboard entry does not exist"
                ));

        if (currentEntry.getGameMode() != gameMode
                || !currentEntry.getVoterId().equals(voterId)) {
            throw new IllegalArgumentException(
                    "Leaderboard entry does not belong to this player"
            );
        }

        LocalDate today = LocalDate.now(clock);
        List<LeaderboardEntry> dailyEntries =
                getDailyEntries(gameMode, today);
        List<LeaderboardEntry> allTimeEntries =
                getAllTimeEntries(gameMode);

        LeaderboardEntry playerBest = entryRepository
                .findFirstByGameModeAndVoterIdOrderByScoreDescAchievedAtAscIdAsc(
                        gameMode,
                        voterId
                )
                .orElse(currentEntry);

        return new LeaderboardView(
                currentEntry.getId(),
                currentEntry.getScore(),
                highScore(dailyEntries),
                highScore(allTimeEntries),
                currentEntry.isDailyRecordAtAchievement(),
                currentEntry.isAllTimeRecordAtAchievement(),
                currentEntry.isNameEligible(),
                currentEntry.getDisplayName() != null,
                toRows(dailyEntries, voterId, currentEntry.getId()),
                toRows(allTimeEntries, voterId, currentEntry.getId()),
                new LeaderboardView.PlayerBest(
                        playerBest.getScore(),
                        localDateOf(playerBest.getAchievedAt())
                )
        );
    }

    @Transactional
    public void nameScore(
            GameMode gameMode,
            Long runId,
            String voterId,
            String displayName
    ) {
        LeaderboardEntry entry = entryRepository
                .findByGameRunId(runId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Leaderboard entry does not exist"
                ));

        if (entry.getGameMode() != gameMode
                || !entry.getVoterId().equals(voterId)) {
            throw new IllegalArgumentException(
                    "Leaderboard entry does not belong to this player"
            );
        }

        entry.name(displayName);
        entryRepository.save(entry);
    }

    @Transactional(readOnly = true)
    public int getAllTimeHighScore(GameMode gameMode) {
        return highScore(getAllTimeEntries(gameMode));
    }

    @Transactional(readOnly = true)
    public int getDailyHighScore(GameMode gameMode) {
        return highScore(
                getDailyEntries(gameMode, LocalDate.now(clock))
        );
    }

    private List<LeaderboardEntry> getDailyEntries(
            GameMode gameMode,
            LocalDate date
    ) {
        ZoneId zone = clock.getZone();
        Instant start = date.atStartOfDay(zone).toInstant();
        Instant end = date.plusDays(1)
                .atStartOfDay(zone)
                .toInstant();

        return entryRepository
                .findByGameModeAndDisplayNameIsNotNullAndAchievedAtGreaterThanEqualAndAchievedAtLessThanOrderByScoreDescAchievedAtAscIdAsc(
                        gameMode,
                        start,
                        end,
                        PageRequest.of(0, LEADERBOARD_SIZE)
                );
    }

    private List<LeaderboardEntry> getAllTimeEntries(
            GameMode gameMode
    ) {
        return entryRepository
                .findByGameModeAndDisplayNameIsNotNullOrderByScoreDescAchievedAtAscIdAsc(
                        gameMode,
                        PageRequest.of(0, LEADERBOARD_SIZE)
                );
    }

    private int rankAmongNamedEntries(
            List<LeaderboardEntry> namedEntries,
            LeaderboardEntry target
    ) {
        List<LeaderboardEntry> candidates = new ArrayList<>(
                namedEntries
        );
        candidates.add(target);
        candidates.sort(
                Comparator.comparingInt(LeaderboardEntry::getScore)
                        .reversed()
                        .thenComparing(LeaderboardEntry::getAchievedAt)
                        .thenComparing(LeaderboardEntry::getId)
        );

        return rankOf(candidates, target);
    }

    private int rankOf(
            List<LeaderboardEntry> entries,
            LeaderboardEntry target
    ) {
        for (int index = 0; index < entries.size(); index++) {
            if (entries.get(index).getId().equals(target.getId())) {
                return index + 1;
            }
        }

        return 0;
    }

    private int highScore(List<LeaderboardEntry> entries) {
        return entries.isEmpty() ? 0 : entries.getFirst().getScore();
    }

    private List<LeaderboardView.LeaderboardRow> toRows(
            List<LeaderboardEntry> entries,
            String voterId,
            Long currentEntryId
    ) {
        return java.util.stream.IntStream
                .range(0, entries.size())
                .mapToObj(index -> {
                    LeaderboardEntry entry = entries.get(index);

                    return new LeaderboardView.LeaderboardRow(
                            index + 1,
                            entry.getDisplayName(),
                            entry.getScore(),
                            localDateOf(entry.getAchievedAt()),
                            entry.getVoterId().equals(voterId),
                            entry.getId().equals(currentEntryId)
                    );
                })
                .toList();
    }

    private LocalDate localDateOf(Instant instant) {
        return instant.atZone(clock.getZone()).toLocalDate();
    }
}
