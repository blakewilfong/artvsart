package com.artvsart;

import com.artvsart.controller.VoterCookieManager;
import com.artvsart.model.*;
import com.artvsart.service.StreakGameService;
import com.artvsart.service.WagerGameService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EventRevealTemplateTest {
    private static final String VOTER = "b5dc6924-9ea7-4c5c-b58b-1eca5167f864";
    @Autowired MockMvc mvc;
    @MockitoBean StreakGameService streak;
    @MockitoBean WagerGameService wager;

    @ParameterizedTest
    @CsvSource({"streak,false,true", "streak,true,true", "streak,true,false",
            "wager,false,true", "wager,true,true", "wager,true,false"})
    void onlyRevealsEventDetailsAfterAnswer(String mode, boolean answered, boolean active) throws Exception {
        Artwork first = mock(Artwork.class);
        Artwork second = mock(Artwork.class);
        when(first.getId()).thenReturn(1L);
        when(second.getId()).thenReturn(2L);
        GameRun run = mock(GameRun.class);
        when(run.isActive()).thenReturn(active);
        when(run.getMinimumWager()).thenReturn(5);
        ArtworkQuestion question = mock(ArtworkQuestion.class);
        when(question.getId()).thenReturn(12L);
        when(question.getGameRun()).thenReturn(run);
        when(question.getRoundNumber()).thenReturn(1);
        when(question.getArtworkOne()).thenReturn(first);
        when(question.getArtworkTwo()).thenReturn(second);
        when(question.getCorrectArtwork()).thenReturn(first);
        when(question.getPrompt()).thenReturn("Which artwork was closer to London's Great Stink?");
        when(question.getHistoricalEvent()).thenReturn(HistoricalEvent.GREAT_STINK);
        when(question.getAnswerContext()).thenReturn("1858");
        ArtworkAnswer answer = mock(ArtworkAnswer.class);
        when(answer.getSelectedArtwork()).thenReturn(first);
        when(answer.isCorrect()).thenReturn(active);
        Optional<ArtworkAnswer> result = answered ? Optional.of(answer) : Optional.empty();
        when(streak.getQuestion(12L, VOTER)).thenReturn(question);
        when(streak.findAnswer(12L, VOTER)).thenReturn(result);
        when(wager.getQuestion(12L, VOTER)).thenReturn(question);
        when(wager.findAnswer(12L, VOTER)).thenReturn(result);
        String html = mvc.perform(get("/" + mode + "/12").param("reveal", "true")
                .cookie(new Cookie(VoterCookieManager.COOKIE_NAME, VOTER)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertEquals(answered, html.contains("https://en.wikipedia.org/wiki/Great_Stink"));
        assertEquals(answered, html.contains(HistoricalEvent.GREAT_STINK.getSummary()));
        assertEquals(answered, html.contains("(1858)"));
        assertEquals(answered, html.contains("data-auto-advance-control"));
        if (answered) {
            assertTrue(html.contains("target=\"_blank\""));
            assertTrue(html.contains("rel=\"noopener noreferrer\""));
        }
        if (mode.equals("wager")) {
            assertEquals(answered && active, html.contains("data-next-url=\"/wager/continue\""));
        }
        if (Boolean.getBoolean("artvsart.preview")) {
            java.nio.file.Path directory = java.nio.file.Path.of("target", "event-previews");
            java.nio.file.Files.createDirectories(directory);
            java.nio.file.Files.writeString(directory.resolve(
                    mode + "-" + answered + "-" + active + ".html"), html);
        }
    }
}
