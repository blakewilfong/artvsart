package com.artvsart.controller;

import com.artvsart.service.ArtworkService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final ArtworkService artworkService;

    public HomeController(ArtworkService artworkService) {
        this.artworkService = artworkService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("artworks", artworkService.getAllArtworks());
        return "home";
    }
}