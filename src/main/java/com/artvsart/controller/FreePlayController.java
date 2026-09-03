package com.artvsart.controller;

import com.artvsart.model.ArtworkAnswer;
import com.artvsart.model.ArtworkQuestion;
import com.artvsart.service.FreePlayQuestionService;
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

    private final FreePlayQuestionService questionService;
    private final VoterCookieManager voterCookieManager;

    public FreePlayController(
            FreePlayQuestionService questionService,
            VoterCookieManager voterCookieManager
    ) {
        this.questionService = questionService;
        this.voterCookieManager = voterCookieManager;
    }

    @GetMapping("/free-play")
    public String startFreePlay() {
        ArtworkQuestion question =
                questionService.createQuestion();

        return "redirect:/free-play/"
                + question.getId();
    }

    @GetMapping("/free-play/{questionId}")
    public String showQuestion(
            @PathVariable Long questionId,
            @CookieValue(
                    name = VoterCookieManager.COOKIE_NAME,
                    required = false
            ) String voterId,
            Model model
    ) {
        ArtworkQuestion question =
                questionService.getQuestion(questionId);

        Optional<ArtworkAnswer> existingAnswer =
                Optional.empty();

        if (voterCookieManager.isValid(voterId)) {
            existingAnswer = questionService.findAnswer(
                    questionId,
                    voterId
            );
        }

        model.addAttribute(
                "question",
                question
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
        });

        return "free-play";
    }

    @PostMapping("/free-play/answer")
    public String answerQuestion(
            @RequestParam Long questionId,
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

        ArtworkAnswer answer =
                questionService.answerQuestion(
                        questionId,
                        artworkId,
                        voterId
                );

        return "redirect:/free-play/"
                + answer.getQuestion().getId()
                + "?reveal=true";
    }
}