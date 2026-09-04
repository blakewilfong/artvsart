package com.artvsart;

import com.artvsart.controller.VoterCookieManager;
import com.artvsart.model.Artwork;
import com.artvsart.model.ArtworkAnswer;
import com.artvsart.model.ArtworkQuestion;
import com.artvsart.model.GameRun;
import com.artvsart.service.LeaderboardView;
import com.artvsart.service.StreakGameService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class StreakTemplateTest {

    private static final String VOTER_ID =
            "b5dc6924-9ea7-4c5c-b58b-1eca5167f864";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StreakGameService streakGameService;

    @Test
    void offersRerollsForAnUnansweredQuestion()
            throws Exception {
        Artwork artworkOne = artwork(
                1L,
                "First work",
                "First artist"
        );
        Artwork artworkTwo = artwork(
                2L,
                "Second work",
                "Second artist"
        );
        ArtworkQuestion question = question(
                artworkOne,
                artworkTwo
        );
        GameRun run = mock(GameRun.class);

        when(run.isActive()).thenReturn(true);
        when(run.getCorrectAnswers()).thenReturn(2);
        when(run.getRerollsRemaining()).thenReturn(3);
        when(question.getGameRun()).thenReturn(run);
        when(streakGameService.getQuestion(12L, VOTER_ID))
                .thenReturn(question);
        when(streakGameService.findAnswer(12L, VOTER_ID))
                .thenReturn(Optional.empty());

        mockMvc.perform(
                        get("/streak/12")
                                .cookie(new Cookie(
                                        VoterCookieManager.COOKIE_NAME,
                                        VOTER_ID
                                ))
                )
                .andExpect(status().isOk())
                .andExpect(content().string(
                        org.hamcrest.Matchers.allOf(
                                org.hamcrest.Matchers.containsString(
                                        "action=\"/streak/reroll\""
                                ),
                                org.hamcrest.Matchers.containsString(
                                        "Reroll (3)"
                                )
                        )
                ));
    }

    @Test
    void rendersCompletedRunLeaderboardAndNameForm()
            throws Exception {
        Artwork artworkOne = artwork(
                1L,
                "First work",
                "First artist"
        );
        Artwork artworkTwo = artwork(
                2L,
                "Second work",
                "Second artist"
        );

        ArtworkQuestion question = question(
                artworkOne,
                artworkTwo
        );

        GameRun run = mock(GameRun.class);
        when(run.getId()).thenReturn(7L);
        when(run.getCorrectAnswers()).thenReturn(7);
        when(run.isActive()).thenReturn(false);
        when(question.getGameRun()).thenReturn(run);

        ArtworkAnswer answer = mock(ArtworkAnswer.class);
        when(answer.getSelectedArtwork()).thenReturn(artworkOne);
        when(answer.isCorrect()).thenReturn(false);

        when(streakGameService.getQuestion(12L, VOTER_ID))
                .thenReturn(question);
        when(streakGameService.findAnswer(12L, VOTER_ID))
                .thenReturn(Optional.of(answer));
        when(streakGameService.getDailyHighScore()).thenReturn(7);
        when(streakGameService.getGlobalHighScore()).thenReturn(7);
        when(streakGameService.getLeaderboard(7L, VOTER_ID))
                .thenReturn(leaderboard());

        mockMvc.perform(
                        get("/streak/12")
                                .cookie(new Cookie(
                                        VoterCookieManager.COOKIE_NAME,
                                        VOTER_ID
                                ))
                )
                .andExpect(status().isOk())
                .andExpect(content().string(
                        org.hamcrest.Matchers.allOf(
                                org.hamcrest.Matchers.containsString(
                                        "Final score"
                                ),
                                org.hamcrest.Matchers.containsString(
                                        "New all-time record"
                                ),
                                org.hamcrest.Matchers.containsString(
                                        "Add your name"
                                ),
                                org.hamcrest.Matchers.containsString(
                                        "Today's top 10"
                                ),
                                org.hamcrest.Matchers.containsString(
                                        "All-time top 10"
                                ),
                                org.hamcrest.Matchers.containsString(
                                        "Sep 3, 2026"
                                ),
                                org.hamcrest.Matchers.containsString(
                                        "src=\"/artworks/1/image\""
                                ),
                                org.hamcrest.Matchers.containsString(
                                        "Created 1900"
                                ),
                                org.hamcrest.Matchers.containsString(
                                        "(1874)"
                                )
                        )
                ));
    }

    @Test
    void delaysTheLeaderboardWhileTheFinalAnswerIsVisible()
            throws Exception {
        Artwork artworkOne = artwork(
                1L,
                "First work",
                "First artist"
        );
        Artwork artworkTwo = artwork(
                2L,
                "Second work",
                "Second artist"
        );
        ArtworkQuestion question = question(
                artworkOne,
                artworkTwo
        );

        GameRun run = mock(GameRun.class);
        when(run.getId()).thenReturn(7L);
        when(run.getCorrectAnswers()).thenReturn(7);
        when(run.isActive()).thenReturn(false);
        when(question.getGameRun()).thenReturn(run);

        ArtworkAnswer answer = mock(ArtworkAnswer.class);
        when(answer.getSelectedArtwork()).thenReturn(artworkOne);
        when(answer.isCorrect()).thenReturn(false);

        when(streakGameService.getQuestion(12L, VOTER_ID))
                .thenReturn(question);
        when(streakGameService.findAnswer(12L, VOTER_ID))
                .thenReturn(Optional.of(answer));

        mockMvc.perform(
                        get("/streak/12")
                                .param("reveal", "true")
                                .cookie(new Cookie(
                                        VoterCookieManager.COOKIE_NAME,
                                        VOTER_ID
                                ))
                )
                .andExpect(status().isOk())
                .andExpect(content().string(
                        org.hamcrest.Matchers.allOf(
                                org.hamcrest.Matchers.containsString(
                                        "Streak over"
                                ),
                                org.hamcrest.Matchers.containsString(
                                        "data-next-url=\"/streak/12\""
                                ),
                                org.hamcrest.Matchers.containsString(
                                        "Created 1900"
                                ),
                                org.hamcrest.Matchers.containsString(
                                        "(1874)"
                                ),
                                org.hamcrest.Matchers.not(
                                        org.hamcrest.Matchers.containsString(
                                                "Final score"
                                        )
                                )
                        )
                ));

        verify(streakGameService, never()).getLeaderboard(
                7L,
                VOTER_ID
        );
    }

    private ArtworkQuestion question(
            Artwork artworkOne,
            Artwork artworkTwo
    ) {
        ArtworkQuestion question = mock(ArtworkQuestion.class);
        when(question.getId()).thenReturn(12L);
        when(question.getRoundNumber()).thenReturn(8);
        when(question.getPrompt()).thenReturn(
                "Which artwork was created closer in time to this event: the first Impressionist exhibition?"
        );
        when(question.getAnswerContext()).thenReturn("1874");
        when(question.getArtworkOne()).thenReturn(artworkOne);
        when(question.getArtworkTwo()).thenReturn(artworkTwo);
        when(question.getCorrectArtwork()).thenReturn(artworkTwo);
        when(question.getAnswerCaption(artworkOne))
                .thenReturn("Created 1900");
        when(question.getAnswerCaption(artworkTwo))
                .thenReturn("Created 1950");
        return question;
    }

    private LeaderboardView leaderboard() {
        LocalDate achievedOn = LocalDate.of(2026, 9, 3);
        LeaderboardView.LeaderboardRow row =
                new LeaderboardView.LeaderboardRow(
                        1,
                        "Blake",
                        7,
                        achievedOn,
                        true,
                        true
                );

        return new LeaderboardView(
                20L,
                7,
                7,
                7,
                true,
                true,
                true,
                false,
                List.of(row),
                List.of(row),
                new LeaderboardView.PlayerBest(7, achievedOn)
        );
    }

    private Artwork artwork(
            long id,
            String title,
            String artistName
    ) {
        Artwork artwork = mock(Artwork.class);
        when(artwork.getId()).thenReturn(id);
        when(artwork.getTitle()).thenReturn(title);
        when(artwork.getArtistName()).thenReturn(artistName);
        when(artwork.getImageUrl()).thenReturn(
                "https://example.com/image.jpg"
        );
        return artwork;
    }
}
