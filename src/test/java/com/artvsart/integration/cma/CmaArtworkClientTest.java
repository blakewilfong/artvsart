package com.artvsart.integration.cma;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class CmaArtworkClientTest {

    @Test
    void omitsTheDateFilterForAllEraImports() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        CmaArtworkClient client = new CmaArtworkClient(builder);
        server.expect(requestTo(startsWith(
                        "https://openaccess-api.clevelandart.org/api/artworks/")))
                .andExpect(request -> org.junit.jupiter.api.Assertions.assertFalse(
                        request.getURI().getQuery().contains("created_after")))
                .andExpect(queryParam("cc0", ""))
                .andExpect(queryParam("has_image", "1"))
                .andRespond(withSuccess("{\"info\":{\"total\":0},\"data\":[]}",
                        MediaType.APPLICATION_JSON));

        assertEquals(0, client.searchOpenAccessPaintings(null, 0, 1000).total());
        server.verify();
    }

    @Test
    void fetchesCc0PaintingsWithModernArtworkMetadata() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer
                .bindTo(builder)
                .build();
        CmaArtworkClient client = new CmaArtworkClient(builder);

        server.expect(requestTo(startsWith(
                        "https://openaccess-api.clevelandart.org/api/artworks/"
                )))
                .andExpect(header("User-Agent", "ArtVsArt/1.0"))
                .andExpect(queryParam("cc0", ""))
                .andExpect(queryParam("has_image", "1"))
                .andExpect(queryParam("type", "Painting"))
                .andExpect(queryParam("created_after", "1850"))
                .andExpect(queryParam("skip", "0"))
                .andExpect(queryParam("limit", "1000"))
                .andRespond(withSuccess(
                        """
                        {
                          "info": {"total": 1},
                          "data": [{
                            "id": 92937,
                            "share_license_status": "CC0",
                            "title": "Stag at Sharkey's",
                            "creation_date": "1909",
                            "creation_date_earliest": 1909,
                            "creation_date_latest": 1909,
                            "creators": [{
                              "id": 3005,
                              "description": "George Bellows (American, 1882-1925)",
                              "role": "artist",
                              "birth_year": "1882",
                              "death_year": "1925"
                            }],
                            "department": "American Painting and Sculpture",
                            "type": "Painting",
                            "technique": "oil on canvas",
                            "url": "https://clevelandart.org/art/1922.1133",
                            "images": {
                              "web": {"url": "https://example.test/web.jpg"},
                              "print": {"url": "https://example.test/print.jpg"}
                            },
                            "record_type": "object"
                          }]
                        }
                        """,
                        MediaType.APPLICATION_JSON
                ));

        CmaArtworkSearchResponse response =
                client.searchOpenAccessPaintings(1850, 0, 1000);

        assertEquals(1, response.total());
        assertEquals(92937L, response.artworks().getFirst().id());
        assertEquals(
                "George Bellows",
                response.artworks().getFirst()
                        .primaryArtist()
                        .orElseThrow()
                        .displayName()
        );

        server.verify();
    }
}
