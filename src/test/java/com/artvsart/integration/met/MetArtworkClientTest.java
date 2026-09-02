package com.artvsart.integration.met;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class MetArtworkClientTest {

    @Test
    void fetchesAndMapsPublicDomainArtwork() {
        RestClient.Builder builder = RestClient.builder();

        MockRestServiceServer server =
                MockRestServiceServer
                        .bindTo(builder)
                        .build();

        MetArtworkClient client =
                new MetArtworkClient(builder);

        server.expect(requestTo(
                "https://collectionapi.metmuseum.org"
                        + "/public/collection/v1/objects/436535"
        )).andRespond(withSuccess(
                """
                {
                  "objectID": 436535,
                  "isPublicDomain": true,
                  "primaryImageSmall": "https://images.metmuseum.org/art.jpg",
                  "title": "Wheat Field with Cypresses",
                  "artistDisplayName": "Vincent van Gogh",
                  "objectDate": "1889",
                  "objectURL": "https://www.metmuseum.org/art/collection/search/436535"
                }
                """,
                MediaType.APPLICATION_JSON
        ));

        MetArtworkResponse artwork =
                client.fetchArtwork(436535L);

        assertEquals(436535L, artwork.objectId());
        assertEquals(
                "Wheat Field with Cypresses",
                artwork.title()
        );
        assertEquals(
                "Vincent van Gogh",
                artwork.artistDisplayName()
        );
        assertEquals("1889", artwork.objectDate());
        assertTrue(artwork.publicDomain());
        assertTrue(artwork.isUsable());

        server.verify();
    }
}