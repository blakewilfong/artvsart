package com.artvsart.controller;

import com.artvsart.service.ArtworkImageService;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.Duration;

@Controller
public class ArtworkImageController {

    private final ArtworkImageService artworkImageService;

    public ArtworkImageController(
            ArtworkImageService artworkImageService
    ) {
        this.artworkImageService = artworkImageService;
    }

    @GetMapping("/artworks/{artworkId}/image")
    @ResponseBody
    public ResponseEntity<byte[]> showArtworkImage(
            @PathVariable long artworkId
    ) {
        return artworkImageService.load(artworkId)
                .map(image -> ResponseEntity.ok()
                        .cacheControl(CacheControl
                                .maxAge(Duration.ofDays(1))
                                .cachePublic()
                        )
                        .contentType(image.contentType())
                        .body(image.content())
                )
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
