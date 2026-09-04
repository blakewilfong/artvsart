package com.artvsart.service;

import com.artvsart.model.Artwork;
import com.artvsart.repository.ArtworkRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ArtworkImageServiceTest {

    @Test
    void loadsAnAllowlistedMuseumImageThroughTheApplication() {
        ArtworkRepository repository = mock(ArtworkRepository.class);
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer
                .bindTo(builder)
                .build();
        ArtworkImageService service = new ArtworkImageService(
                repository,
                builder
        );
        byte[] imageBytes = {1, 2, 3};
        String imageUrl = "https://api.nga.gov/iiif/example/image.jpg";

        when(repository.findById(10L)).thenReturn(Optional.of(
                artwork("nga", imageUrl)
        ));
        server.expect(requestTo(imageUrl))
                .andExpect(header("User-Agent", "ArtVsArt/1.0"))
                .andRespond(withSuccess(
                        imageBytes,
                        MediaType.IMAGE_JPEG
                ));

        ArtworkImageService.ArtworkImage image = service.load(10L)
                .orElseThrow();

        assertArrayEquals(imageBytes, image.content());
        assertEquals(MediaType.IMAGE_JPEG, image.contentType());
        server.verify();
    }

    @Test
    void rejectsAnImageHostThatDoesNotMatchTheMuseum() {
        ArtworkRepository repository = mock(ArtworkRepository.class);
        ArtworkImageService service = new ArtworkImageService(
                repository,
                RestClient.builder()
        );

        when(repository.findById(10L)).thenReturn(Optional.of(
                artwork("nga", "https://example.com/image.jpg")
        ));

        assertTrue(service.load(10L).isEmpty());
    }

    private Artwork artwork(String source, String imageUrl) {
        return new Artwork(
                source,
                "source-id",
                "Title",
                "Artist",
                "1900",
                imageUrl
        );
    }
}
