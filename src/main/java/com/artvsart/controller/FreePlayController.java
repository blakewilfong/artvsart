package com.artvsart.controller;

import com.artvsart.model.Matchup;
import com.artvsart.model.Vote;
import com.artvsart.service.ArtworkStatisticsService;
import com.artvsart.service.FreePlayService;
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
public class FreePlayController {

    private final FreePlayService freePlayService;
    private final VoteService voteService;
    private final ArtworkStatisticsService statisticsService;
    private final VoterCookieManager voterCookieManager;

    public FreePlayController(
            FreePlayService freePlayService,
            VoteService voteService,
            ArtworkStatisticsService statisticsService,
            VoterCookieManager voterCookieManager
    ) {
        this.freePlayService = freePlayService;
        this.voteService = voteService;
        this.statisticsService = statisticsService;
        this.voterCookieManager = voterCookieManager;
    }

    @GetMapping("/free-play")
    public String startFreePlay() {
        Matchup matchup = freePlayService.createMatchup();

        return "redirect:/free-play/" + matchup.getId();
    }

    @GetMapping("/free-play/{matchupId}")
    public String showMatchup(
            @PathVariable Long matchupId,
            @CookieValue(
                    name = VoterCookieManager.COOKIE_NAME,
                    required = false
            ) String voterId,
            Model model
    ) {
        Matchup matchup =
                freePlayService.getMatchup(matchupId);

        Optional<Vote> existingVote = Optional.empty();

        if (voterCookieManager.isValid(voterId)) {
            existingVote = voteService.findVote(
                    matchup.getId(),
                    voterId
            );
        }

        model.addAttribute("matchup", matchup);
        model.addAttribute("voted", existingVote.isPresent());
        model.addAttribute("voteAction", "/free-play/vote");

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

        return "free-play";
    }

    @PostMapping("/free-play/vote")
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

        Matchup matchup =
                freePlayService.getMatchup(matchupId);

        Vote vote = voteService.castVote(
                matchup,
                artworkId,
                voterId
        );

        return "redirect:/free-play/"
                + vote.getMatchup().getId()
                + "?reveal=true";
    }
}