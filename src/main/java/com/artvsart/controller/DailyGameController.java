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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class DailyGameController {

    private final MatchupService matchupService;
    private final VoteService voteService;
    private final ArtworkStatisticsService statisticsService;
    private final GameProgressService gameProgressService;
    private final VoterCookieManager voterCookieManager;

    public DailyGameController(
            MatchupService matchupService,
            VoteService voteService,
            ArtworkStatisticsService statisticsService,
            GameProgressService gameProgressService,
            VoterCookieManager voterCookieManager
    ) {
        this.matchupService = matchupService;
        this.voteService = voteService;
        this.statisticsService = statisticsService;
        this.gameProgressService = gameProgressService;
        this.voterCookieManager = voterCookieManager;
    }

    @GetMapping("/round/{roundNumber}")
    public String showRound(
            @PathVariable int roundNumber,
            @CookieValue(
                    name = VoterCookieManager.COOKIE_NAME,
                    required = false
            ) String voterId,
            Model model
    ) {
        DailyGame dailyGame = matchupService.getTodaysGame();

        boolean validVoter =
                voterCookieManager.isValid(voterId);

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
                    name = VoterCookieManager.COOKIE_NAME,
                    required = false
            ) String voterId,
            Model model
    ) {
        if (!voterCookieManager.isValid(voterId)) {
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
                    name = VoterCookieManager.COOKIE_NAME,
                    required = false
            ) String voterId,
            HttpServletResponse response
    ) {
        voterId = voterCookieManager.getOrCreate(
                voterId,
                response
        );

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
}