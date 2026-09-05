package com.artvsart.integration.cma;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Optional;

@Component
public class CmaArtworkClient {

    private static final String BASE_URL =
            "https://openaccess-api.clevelandart.org";

    private static final String RESPONSE_FIELDS = String.join(",",
            "id",
            "share_license_status",
            "title",
            "creation_date",
            "creation_date_earliest",
            "creation_date_latest",
            "creators",
            "culture",
            "technique",
            "department",
            "type",
            "url",
            "images",
            "description",
            "record_type"
    );

    private final RestClient restClient;

    public CmaArtworkClient(RestClient.Builder builder) {
        this.restClient = builder
                .baseUrl(BASE_URL)
                .defaultHeader(
                        HttpHeaders.USER_AGENT,
                        "ArtVsArt/1.0"
                )
                .build();
    }

    public CmaArtworkSearchResponse searchOpenAccessPaintings(
            Integer createdAfterYear,
            int skip,
            int limit
    ) {
        if (skip < 0) {
            throw new IllegalArgumentException(
                    "CMA result offset cannot be negative"
            );
        }

        if (limit <= 0 || limit > 1000) {
            throw new IllegalArgumentException(
                    "CMA result limit must be between 1 and 1000"
            );
        }

        CmaArtworkSearchResponse response = restClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/artworks/")
                        .queryParam("cc0", "")
                        .queryParam("has_image", 1)
                        .queryParam("type", "Painting")
                        .queryParamIfPresent(
                                "created_after",
                                Optional.ofNullable(createdAfterYear)
                        )
                        .queryParam("skip", skip)
                        .queryParam("limit", limit)
                        .queryParam("fields", RESPONSE_FIELDS)
                        .build()
                )
                .retrieve()
                .body(CmaArtworkSearchResponse.class);

        if (response == null) {
            throw new IllegalStateException(
                    "The Cleveland Museum of Art returned an empty response"
            );
        }

        return response;
    }
}
