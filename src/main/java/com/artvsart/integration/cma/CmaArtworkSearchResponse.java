package com.artvsart.integration.cma;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CmaArtworkSearchResponse(
        Info info,
        List<CmaArtworkResponse> data
) {

    public List<CmaArtworkResponse> artworks() {
        return data == null ? List.of() : data;
    }

    public int total() {
        return info == null ? 0 : info.total();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Info(int total) {
    }
}
