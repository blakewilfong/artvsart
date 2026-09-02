package com.artvsart.integration.met;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class MetArtworkClient {

    private static final String BASE_URL =
            "https://collectionapi.metmuseum.org"
                    + "/public/collection/v1";

    private final RestClient restClient;

    public MetArtworkClient(RestClient.Builder builder) {
        this.restClient = builder
                .baseUrl(BASE_URL)
                .build();
    }

    public MetArtworkResponse fetchArtwork(long objectId) {
        MetArtworkResponse response = restClient
                .get()
                .uri("/objects/{objectId}", objectId)
                .retrieve()
                .body(MetArtworkResponse.class);

        if (response == null) {
            throw new IllegalStateException(
                    "The Met returned an empty response for object "
                            + objectId
            );
        }

        return response;
    }
}