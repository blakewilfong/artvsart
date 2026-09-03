package com.artvsart.integration.nga;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

@Component
public class NgaOpenDataClient {

    private final RestClient client;

    public NgaOpenDataClient(RestClient.Builder builder) {
        this.client = builder.build();
    }

    public <T> T read(
            String url,
            ReaderHandler<T> handler
    ) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException(
                    "An NGA dataset URL is required"
            );
        }

        return client.get()
                .uri(url)
                .exchange((request, response) -> {
                    if (!response.getStatusCode()
                            .is2xxSuccessful()) {
                        throw new IllegalStateException(
                                "NGA returned HTTP "
                                        + response.getStatusCode().value()
                                        + " for "
                                        + url
                        );
                    }

                    InputStream body = response.getBody();

                    if (body == null) {
                        throw new IllegalStateException(
                                "NGA returned an empty dataset: " + url
                        );
                    }

                    try (Reader reader = new BufferedReader(
                            new InputStreamReader(
                                    body,
                                    StandardCharsets.UTF_8
                            )
                    )) {
                        return handler.read(reader);
                    } catch (IOException exception) {
                        throw new UncheckedIOException(
                                "Could not read NGA dataset: " + url,
                                exception
                        );
                    }
                });
    }

    @FunctionalInterface
    public interface ReaderHandler<T> {

        T read(Reader reader) throws IOException;
    }
}
