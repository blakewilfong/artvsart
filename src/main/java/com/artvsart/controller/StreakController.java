package com.artvsart.controller;

import com.artvsart.model.ArtworkAnswer;
import com.artvsart.model.ArtworkQuestion;
import com.artvsart.model.GameRun;
import com.artvsart.service.StreakGameService;
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
public class StreakController {

    private final StreakGameService streakGameService;
    private final VoterCookieManager voterCookieManager;

    public StreakController(
            StreakGameService streakGameService,
            VoterCookieManager voterCookieManager
    ) {
        this.streakGameService = streakGameService;
        this.voterCookieManager = voterCookieManager;
    }

    @GetMapping("/streak")
    public String startNewStreak(
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

        ArtworkQuestion question =
                streakGameService.startNew(
                        voterId
                );

        return "redirect:/streak/"
                + question.getId();
    }

    @GetMapping("/streak/continue")
    public String continueStreak(
            @CookieValue(
                    name = VoterCookieManager.COOKIE_NAME,
                    required = false
            ) String voterId
    ) {
        if (!voterCookieManager.isValid(voterId)) {
            return "redirect:/streak";
        }

        ArtworkQuestion question =
                streakGameService.continueRun(voterId);

        return "redirect:/streak/"
                + question.getId();
    }

    @GetMapping("/streak/{questionId}")
    public String showQuestion(
            @PathVariable Long questionId,
            @CookieValue(
                    name = VoterCookieManager.COOKIE_NAME,
                    required = false
            ) String voterId,
            @RequestParam(
                    name = "reveal",
                    defaultValue = "false"
            ) boolean reveal,
            Model model
    ) {
        if (!voterCookieManager.isValid(voterId)) {
            return "redirect:/streak";
        }

        ArtworkQuestion question =
                streakGameService.getQuestion(
                        questionId,
                        voterId
                );

        GameRun run = question.getGameRun();

        Optional<ArtworkAnswer> existingAnswer =
                streakGameService.findAnswer(
                        questionId,
                        voterId
                );

        model.addAttribute(
                "question",
                question
        );

        model.addAttribute(
                "run",
                run
        );

        model.addAttribute(
                "highScore",
                streakGameService.getGlobalHighScore()
        );

        model.addAttribute(
                "dailyHighScore",
                streakGameService.getDailyHighScore()
        );

        model.addAttribute(
                "answered",
                existingAnswer.isPresent()
        );

        existingAnswer.ifPresent(answer -> {
            model.addAttribute(
                    "selectedArtworkId",
                    answer.getSelectedArtwork().getId()
            );

            model.addAttribute(
                    "correctArtworkId",
                    question.getCorrectArtwork().getId()
            );

            model.addAttribute(
                    "answerCorrect",
                    answer.isCorrect()
            );

            if (!run.isActive() && !reveal) {
                model.addAttribute(
                        "leaderboard",
                        streakGameService.getLeaderboard(
                                run.getId(),
                                voterId
                        )
                );
            }
        });

        return "streak";
    }

    @PostMapping("/streak/answer")
    public String answerQuestion(
            @RequestParam Long questionId,
            @RequestParam Long artworkId,
            @CookieValue(
                    name = VoterCookieManager.COOKIE_NAME,
                    required = false
            ) String voterId
    ) {
        if (!voterCookieManager.isValid(voterId)) {
            return "redirect:/streak";
        }

        ArtworkAnswer answer =
                streakGameService.answerQuestion(
                        questionId,
                        artworkId,
                        voterId
                );

        return "redirect:/streak/"
                + answer.getQuestion().getId()
                + "?reveal=true";
    }

    @PostMapping("/streak/reroll")
    public String rerollQuestion(
            @RequestParam Long questionId,
            @CookieValue(
                    name = VoterCookieManager.COOKIE_NAME,
                    required = false
            ) String voterId
    ) {
        if (!voterCookieManager.isValid(voterId)) {
            return "redirect:/streak";
        }

        ArtworkQuestion question =
                streakGameService.rerollQuestion(
                        questionId,
                        voterId
                );

        return "redirect:/streak/" + question.getId();
    }

    @PostMapping("/streak/score/name")
    public String nameScore(
            @RequestParam Long questionId,
            @RequestParam Long runId,
            @RequestParam String displayName,
            @CookieValue(
                    name = VoterCookieManager.COOKIE_NAME,
                    required = false
            ) String voterId
    ) {
        if (!voterCookieManager.isValid(voterId)) {
            return "redirect:/streak";
        }

        streakGameService.nameScore(
                runId,
                voterId,
                displayName
        );

        return "redirect:/streak/" + questionId;
    }
}
