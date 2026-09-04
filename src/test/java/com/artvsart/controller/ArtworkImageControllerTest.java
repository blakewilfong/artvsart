package com.artvsart.controller;

import com.artvsart.service.ArtworkImageService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ArtworkImageControllerTest {

    @Test
    void servesMuseumImagesWithBrowserCaching() throws Exception {
        ArtworkImageService service = mock(ArtworkImageService.class);
        ArtworkImageController controller = new ArtworkImageController(
                service
        );
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
        byte[] imageBytes = {1, 2, 3};

        when(service.load(10L)).thenReturn(Optional.of(
                new ArtworkImageService.ArtworkImage(
                        imageBytes,
                        MediaType.IMAGE_JPEG
                )
        ));

        mockMvc.perform(get("/artworks/10/image"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andExpect(content().bytes(imageBytes))
                .andExpect(header().string(
                        "Cache-Control",
                        "max-age=86400, public"
                ));
    }
}
