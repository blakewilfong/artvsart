package com.artvsart.controller;

import com.artvsart.model.Matchup;
import com.artvsart.model.Vote;
import com.artvsart.service.MatchupService;
import com.artvsart.service.VoteService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Controller
public class HomeController {

    private static final String VOTER_COOKIE_NAME =
            "artvsart_voter";

    private final MatchupService matchupService;
    private final VoteService voteService;

    public HomeController(
            MatchupService matchupService,
            VoteService voteService
    ) {
        this.matchupService = matchupService;
        this.voteService = voteService;
    }

    @GetMapping("/")
    public String startGame() {
        return "redirect:/round/1";
    }

    @GetMapping("/round/{roundNumber}")
    public String showRound(
            @PathVariable int roundNumber,
            @CookieValue(
                    name = VOTER_COOKIE_NAME,
                    required = false
            ) String voterId,
            Model model
    ) {
        Matchup matchup = matchupService
                .getTodaysMatchup(roundNumber);

        Optional<Vote> existingVote = Optional.empty();

        if (isValidVoterId(voterId)) {
            existingVote = voteService.findVote(
                    matchup.getId(),
                    voterId
            );
        }

        model.addAttribute(
                "matchup",
                matchup
        );

        model.addAttribute(
                "voted",
                existingVote.isPresent()
        );

        existingVote.ifPresent(vote -> {
            model.addAttribute(
                    "selectedArtworkId",
                    vote.getSelectedArtwork().getId()
            );

            model.addAttribute(
                    "outcome",
                    vote.getOutcome()
            );
        });

        return "home";
    }

    @PostMapping("/vote")
    public String vote(
            @RequestParam Long matchupId,
            @RequestParam Long artworkId,
            @CookieValue(
                    name = VOTER_COOKIE_NAME,
                    required = false
            ) String voterId,
            HttpServletResponse response
    ) {
        if (!isValidVoterId(voterId)) {
            voterId = UUID.randomUUID().toString();

            ResponseCookie voterCookie = ResponseCookie
                    .from(VOTER_COOKIE_NAME, voterId)
                    .httpOnly(true)
                    .sameSite("Lax")
                    .path("/")
                    .maxAge(Duration.ofDays(365))
                    .build();

            response.addHeader(
                    HttpHeaders.SET_COOKIE,
                    voterCookie.toString()
            );
        }

        Vote vote = voteService.castVote(
                matchupId,
                artworkId,
                voterId
        );

        return "redirect:/round/"
                + vote.getMatchup().getRoundNumber();
    }

    private boolean isValidVoterId(String voterId) {
        if (voterId == null) {
            return false;
        }

        try {
            UUID.fromString(voterId);
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}