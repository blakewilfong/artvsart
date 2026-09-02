package com.artvsart.controller;

import com.artvsart.dto.DailyGameScore;
import com.artvsart.model.DailyGame;
import com.artvsart.model.Matchup;
import com.artvsart.model.Vote;
import com.artvsart.service.ArtworkStatisticsService;
import com.artvsart.service.GameProgressService;
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
    private final ArtworkStatisticsService statisticsService;
    private final GameProgressService gameProgressService;

    public HomeController(
            MatchupService matchupService,
            VoteService voteService,
            ArtworkStatisticsService statisticsService,
            GameProgressService gameProgressService
    ) {
        this.matchupService = matchupService;
        this.voteService = voteService;
        this.statisticsService = statisticsService;
        this.gameProgressService = gameProgressService;
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
        DailyGame dailyGame = matchupService.getTodaysGame();

        boolean validVoter = isValidVoterId(voterId);

        DailyGameScore score = validVoter
                ? gameProgressService.getScore(
                dailyGame,
                voterId
        )
                : null;

        if (roundNumber < 1
                || roundNumber > dailyGame.getTotalRounds()) {
            return redirectToCurrentRound(
                    dailyGame,
                    score
            );
        }

        Matchup matchup = matchupService
                .getTodaysMatchup(roundNumber);

        Optional<Vote> existingVote = Optional.empty();

        if (validVoter) {
            existingVote = voteService.findVote(
                    matchup.getId(),
                    voterId
            );
        }

        if (existingVote.isEmpty()) {
            int permittedRound = score == null
                    ? 1
                    : score.nextRoundNumber();

            if (permittedRound == 0) {
                permittedRound = dailyGame.getTotalRounds();
            }

            if (roundNumber != permittedRound) {
                return "redirect:/round/" + permittedRound;
            }
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

            model.addAttribute(
                    "artworkOneStats",
                    statisticsService.getStats(
                            matchup.getArtworkOne()
                    )
            );

            model.addAttribute(
                    "artworkTwoStats",
                    statisticsService.getStats(
                            matchup.getArtworkTwo()
                    )
            );
        });

        return "home";
    }

    @GetMapping("/results")
    public String showResults(
            @CookieValue(
                    name = VOTER_COOKIE_NAME,
                    required = false
            ) String voterId,
            Model model
    ) {
        if (!isValidVoterId(voterId)) {
            return "redirect:/round/1";
        }

        DailyGame dailyGame = matchupService.getTodaysGame();

        DailyGameScore score = gameProgressService.getScore(
                dailyGame,
                voterId
        );

        if (!score.complete()) {
            return "redirect:/round/"
                    + score.nextRoundNumber();
        }

        model.addAttribute("dailyGame", dailyGame);
        model.addAttribute("score", score);

        return "results";
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

        Matchup matchup = matchupService
                .getTodaysMatchupById(matchupId);

        Vote vote = voteService.castVote(
                matchup,
                artworkId,
                voterId
        );

        return "redirect:/round/"
                + vote.getMatchup().getRoundNumber()
                + "?reveal=true";
    }

    private String redirectToCurrentRound(
            DailyGame dailyGame,
            DailyGameScore score
    ) {
        if (score == null) {
            return "redirect:/round/1";
        }

        if (score.complete()) {
            return "redirect:/round/"
                    + dailyGame.getTotalRounds();
        }

        return "redirect:/round/"
                + score.nextRoundNumber();
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