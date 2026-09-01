package com.artvsart.controller;

import com.artvsart.model.Matchup;
import com.artvsart.service.MatchupService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final MatchupService matchupService;

    public HomeController(MatchupService matchupService) {
        this.matchupService = matchupService;
    }

    @GetMapping("/")
    public String home(Model model) {
        Matchup matchup = matchupService.getTodaysMatchup();
        model.addAttribute("matchup", matchup);

        return "home";
    }
}