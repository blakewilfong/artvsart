package com.artvsart.controller;

import com.artvsart.model.ArtworkAnswer;
import com.artvsart.model.ArtworkQuestion;
import com.artvsart.model.GameRun;
import com.artvsart.service.WagerGameService;
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
public class WagerController {

    private final WagerGameService wagerGameService;
    private final VoterCookieManager voterCookieManager;

    public WagerController(
            WagerGameService wagerGameService,
            VoterCookieManager voterCookieManager
    ) {
        this.wagerGameService = wagerGameService;
        this.voterCookieManager = voterCookieManager;
    }

    @GetMapping("/wager")
    public String startNewWager(
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
                wagerGameService.startNew(
                        voterId
                );

        return "redirect:/wager/"
                + question.getId();
    }

    @GetMapping("/wager/continue")
    public String continueWager(
            @CookieValue(
                    name = VoterCookieManager.COOKIE_NAME,
                    required = false
            ) String voterId
    ) {
        if (!voterCookieManager.isValid(voterId)) {
            return "redirect:/wager";
        }

        ArtworkQuestion question =
                wagerGameService.continueRun(voterId);

        return "redirect:/wager/"
                + question.getId();
    }

    @GetMapping("/wager/{questionId}")
    public String showQuestion(
            @PathVariable Long questionId,
            @CookieValue(
                    name = VoterCookieManager.COOKIE_NAME,
                    required = false
            ) String voterId,
            Model model
    ) {
        if (!voterCookieManager.isValid(voterId)) {
            return "redirect:/wager";
        }

        ArtworkQuestion question =
                wagerGameService.getQuestion(
                        questionId,
                        voterId
                );

        GameRun run = question.getGameRun();

        Optional<ArtworkAnswer> existingAnswer =
                wagerGameService.findAnswer(
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
                wagerGameService.getGlobalHighScore()
        );

        model.addAttribute(
                "answered",
                existingAnswer.isPresent()
        );

        model.addAttribute(
                "minimumWager",
                run.getMinimumWager()
        );

        model.addAttribute(
                "rakePercentage",
                run.getRakePercentage()
        );

        existingAnswer.ifPresent(answer ->
                addAnswerResult(
                        model,
                        question,
                        run,
                        answer
                )
        );

        return "wager";
    }

    @PostMapping("/wager/answer")
    public String answerQuestion(
            @RequestParam Long questionId,
            @RequestParam Long artworkId,
            @RequestParam int wagerAmount,
            @CookieValue(
                    name = VoterCookieManager.COOKIE_NAME,
                    required = false
            ) String voterId
    ) {
        if (!voterCookieManager.isValid(voterId)) {
            return "redirect:/wager";
        }

        ArtworkAnswer answer =
                wagerGameService.answerQuestion(
                        questionId,
                        artworkId,
                        wagerAmount,
                        voterId
                );

        return "redirect:/wager/"
                + answer.getQuestion().getId()
                + "?reveal=true";
    }

    private void addAnswerResult(
            Model model,
            ArtworkQuestion question,
            GameRun run,
            ArtworkAnswer answer
    ) {
        int answeredRound =
                question.getRoundNumber();

        int answeredRakePercentage =
                run.getRakePercentageForRound(
                        answeredRound
                );

        int nextRoundRakePercentage =
                run.getRakePercentageForRound(
                        answeredRound + 1
                );

        int profitAmount =
                run.calculateProfitForRound(
                        answer.getWagerAmount(),
                        answeredRound
                );

        int pointChange = answer.isCorrect()
                ? profitAmount
                : -answer.getWagerAmount();

        int balanceBeforeAnswer =
                run.getPointBalance() - pointChange;

        boolean rakeIncreasedAfterAnswer =
                run.isActive()
                        && nextRoundRakePercentage
                        > answeredRakePercentage;

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

        model.addAttribute(
                "pointChange",
                pointChange
        );

        model.addAttribute(
                "balanceBeforeAnswer",
                balanceBeforeAnswer
        );

        model.addAttribute(
                "rakeIncreasedAfterAnswer",
                rakeIncreasedAfterAnswer
        );

        model.addAttribute(
                "newRakePercentage",
                nextRoundRakePercentage
        );
    }

    @PostMapping("/wager/reroll")
    public String rerollQuestion(
            @RequestParam Long questionId,
            @CookieValue(
                    name = VoterCookieManager.COOKIE_NAME,
                    required = false
            ) String voterId
    ) {
        if (!voterCookieManager.isValid(voterId)) {
            return "redirect:/wager";
        }

        ArtworkQuestion question =
                wagerGameService.rerollQuestion(
                        questionId,
                        voterId
                );

        return "redirect:/wager/" + question.getId();
    }
}
