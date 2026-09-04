package com.artvsart.controller;

import com.artvsart.model.ArtworkQuestion;
import com.artvsart.service.StreakGameService;
import com.artvsart.service.WagerGameService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class GameEntryControllerTest {

    private static final String VOTER_ID =
            "b5dc6924-9ea7-4c5c-b58b-1eca5167f864";

    @Mock
    private StreakGameService streakGameService;

    @Mock
    private WagerGameService wagerGameService;

    @Mock
    private VoterCookieManager voterCookieManager;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                new StreakController(
                        streakGameService,
                        voterCookieManager
                ),
                new WagerController(
                        wagerGameService,
                        voterCookieManager
                )
        ).build();
    }

    @Test
    void modeEntryStartsNewRuns() throws Exception {
        ArtworkQuestion streakQuestion = question(11L);
        ArtworkQuestion wagerQuestion = question(12L);

        when(voterCookieManager.getOrCreate(
                eq(VOTER_ID),
                any()
        )).thenReturn(VOTER_ID);
        when(streakGameService.startNew(VOTER_ID))
                .thenReturn(streakQuestion);
        when(wagerGameService.startNew(VOTER_ID))
                .thenReturn(wagerQuestion);

        mockMvc.perform(get("/streak").cookie(voterCookie()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/streak/11"));
        mockMvc.perform(get("/wager").cookie(voterCookie()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/wager/12"));

        verify(streakGameService).startNew(VOTER_ID);
        verify(wagerGameService).startNew(VOTER_ID);
    }

    @Test
    void internalNavigationContinuesActiveRuns() throws Exception {
        ArtworkQuestion streakQuestion = question(21L);
        ArtworkQuestion wagerQuestion = question(22L);

        when(voterCookieManager.isValid(VOTER_ID))
                .thenReturn(true);
        when(streakGameService.continueRun(VOTER_ID))
                .thenReturn(streakQuestion);
        when(wagerGameService.continueRun(VOTER_ID))
                .thenReturn(wagerQuestion);

        mockMvc.perform(
                        get("/streak/continue")
                                .cookie(voterCookie())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/streak/21"));
        mockMvc.perform(
                        get("/wager/continue")
                                .cookie(voterCookie())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/wager/22"));

        verify(streakGameService).continueRun(VOTER_ID);
        verify(wagerGameService).continueRun(VOTER_ID);
    }

    private ArtworkQuestion question(Long id) {
        ArtworkQuestion question = mock(ArtworkQuestion.class);
        when(question.getId()).thenReturn(id);
        return question;
    }

    private Cookie voterCookie() {
        return new Cookie(
                VoterCookieManager.COOKIE_NAME,
                VOTER_ID
        );
    }
}
