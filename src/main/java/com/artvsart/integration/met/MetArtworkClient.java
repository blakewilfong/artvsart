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

    public MetSearchResponse searchPaintings() {
        MetSearchResponse response = restClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search")
                        .queryParam("q", "painting")
                        .queryParam("hasImages", true)
                        .queryParam("medium", "Paintings")
                        .build()
                )
                .retrieve()
                .body(MetSearchResponse.class);

        return requireResponse(response);
    }

    public MetSearchResponse searchDepartmentPaintings(
            int departmentId
    ) {
        validateDepartmentId(departmentId);

        MetSearchResponse response = restClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search")
                        .queryParam(
                                "departmentId",
                                departmentId
                        )
                        .queryParam("hasImages", true)
                        .queryParam("medium", "Paintings")
                        .queryParam("q", "painting")
                        .build()
                )
                .retrieve()
                .body(MetSearchResponse.class);

        return requireResponse(response);
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

    private void validateDepartmentId(
            int departmentId
    ) {
        if (departmentId <= 0) {
            throw new IllegalArgumentException(
                    "Department ID must be positive"
            );
        }
    }

    private MetSearchResponse requireResponse(
            MetSearchResponse response
    ) {
        if (response == null) {
            throw new IllegalStateException(
                    "The Met returned an empty object ID response"
            );
        }

        return response;
    }
}
