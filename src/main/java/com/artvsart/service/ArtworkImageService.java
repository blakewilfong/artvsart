package com.artvsart.service;

import com.artvsart.model.Artwork;
import com.artvsart.repository.ArtworkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.URI;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Service
public class ArtworkImageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(
            ArtworkImageService.class
    );

    private final ArtworkRepository artworkRepository;
    private final RestClient restClient;

    public ArtworkImageService(
            ArtworkRepository artworkRepository,
            RestClient.Builder restClientBuilder
    ) {
        this.artworkRepository = artworkRepository;
        this.restClient = restClientBuilder
                .defaultHeader(HttpHeaders.USER_AGENT, "ArtVsArt/1.0")
                .build();
    }

    public Optional<ArtworkImage> load(long artworkId) {
        Optional<Artwork> artwork = artworkRepository.findById(
                artworkId
        );

        if (artwork.isEmpty()) {
            return Optional.empty();
        }

        URI imageUri = trustedImageUri(artwork.get());

        if (imageUri == null) {
            LOGGER.warn(
                    "Rejected untrusted image URL for artwork {}",
                    artworkId
            );
            return Optional.empty();
        }

        try {
            ResponseEntity<byte[]> response = restClient
                    .get()
                    .uri(imageUri)
                    .accept(MediaType.IMAGE_JPEG, MediaType.IMAGE_PNG)
                    .retrieve()
                    .toEntity(byte[].class);
            byte[] body = response.getBody();
            MediaType contentType = response.getHeaders()
                    .getContentType();

            if (body == null || body.length == 0
                    || contentType == null
                    || !"image".equalsIgnoreCase(
                    contentType.getType()
            )) {
                return Optional.empty();
            }

            return Optional.of(new ArtworkImage(body, contentType));
        } catch (RestClientException exception) {
            LOGGER.warn(
                    "Could not load image for artwork {}: {}",
                    artworkId,
                    exception.getMessage()
            );
            return Optional.empty();
        }
    }

    private URI trustedImageUri(Artwork artwork) {
        String imageUrl = artwork.getImageUrl();

        if (imageUrl == null || imageUrl.isBlank()) {
            return null;
        }

        URI imageUri;

        try {
            imageUri = URI.create(imageUrl);
        } catch (IllegalArgumentException exception) {
            return null;
        }

        if (!"https".equalsIgnoreCase(imageUri.getScheme())
                || imageUri.getHost() == null) {
            return null;
        }

        String source = artwork.getSource() == null
                ? ""
                : artwork.getSource().toLowerCase(Locale.ROOT);
        Set<String> allowedHosts = switch (source) {
            case "met" -> Set.of("images.metmuseum.org");
            case "nga" -> Set.of("api.nga.gov");
            case "cma" -> Set.of(
                    "openaccess-cdn.clevelandart.org"
            );
            default -> Set.of();
        };

        String host = imageUri.getHost().toLowerCase(Locale.ROOT);
        return allowedHosts.contains(host) ? imageUri : null;
    }

    public record ArtworkImage(
            byte[] content,
            MediaType contentType
    ) {
    }
}
