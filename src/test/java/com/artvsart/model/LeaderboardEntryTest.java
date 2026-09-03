package com.artvsart.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LeaderboardEntryTest {

    @Test
    void acceptsANameForATopTenScore() {
        LeaderboardEntry entry = completedEntry();
        entry.recordQualification(4, 0);

        entry.name("  Art   Fan  ");

        assertEquals("Art Fan", entry.getDisplayName());
    }

    @Test
    void rejectsANameForScoreOutsideBothTopTens() {
        LeaderboardEntry entry = completedEntry();
        entry.recordQualification(0, 0);

        assertThrows(
                IllegalStateException.class,
                () -> entry.name("Art Fan")
        );
    }

    @Test
    void remembersRecordQualificationAtAchievement() {
        LeaderboardEntry entry = completedEntry();

        entry.recordQualification(1, 1);

        assertTrue(entry.isNameEligible());
        assertTrue(entry.isDailyRecordAtAchievement());
        assertTrue(entry.isAllTimeRecordAtAchievement());
    }

    @Test
    void rejectsNamesLongerThanTwentyFourCharacters() {
        LeaderboardEntry entry = completedEntry();
        entry.recordQualification(1, 1);

        assertThrows(
                IllegalArgumentException.class,
                () -> entry.name("1234567890123456789012345")
        );
    }

    private LeaderboardEntry completedEntry() {
        GameRun run = GameRun.startStreak("voter-1");
        run.recordStreakAnswer(false);
        return new LeaderboardEntry(run, 0);
    }
}
