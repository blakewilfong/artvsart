package com.artvsart.integration.cma;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CmaArtworkResponse(
        Long id,
        @JsonProperty("share_license_status")
        String shareLicenseStatus,
        String title,
        @JsonProperty("creation_date")
        String creationDate,
        @JsonProperty("creation_date_earliest")
        Integer creationDateEarliest,
        @JsonProperty("creation_date_latest")
        Integer creationDateLatest,
        List<Creator> creators,
        List<String> culture,
        String technique,
        String department,
        String type,
        String url,
        Images images,
        @JsonProperty("record_type")
        String recordType
) {

    public Optional<Creator> primaryArtist() {
        if (creators == null) {
            return Optional.empty();
        }

        return creators.stream()
                .filter(Creator::isArtist)
                .findFirst();
    }

    public String cultureDisplay() {
        if (culture == null || culture.isEmpty()) {
            return null;
        }

        return String.join("; ", culture);
    }

    public String webImageUrl() {
        return images == null || images.web() == null
                ? null
                : images.web().url();
    }

    public String printImageUrl() {
        if (images == null || images.print() == null
                || !hasText(images.print().url())) {
            return webImageUrl();
        }

        return images.print().url();
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Creator(
            Long id,
            String description,
            String qualifier,
            String role,
            @JsonProperty("birth_year")
            String birthYear,
            @JsonProperty("death_year")
            String deathYear
    ) {

        public boolean isArtist() {
            return "artist".equalsIgnoreCase(role)
                    && hasText(description);
        }

        public String displayName() {
            if (!hasText(description)) {
                return null;
            }

            int detailsStart = description.lastIndexOf(" (");

            return detailsStart > 0
                    ? description.substring(0, detailsStart).trim()
                    : description.trim();
        }

        public String nationality() {
            if (!hasText(description)) {
                return null;
            }

            int detailsStart = description.lastIndexOf(" (");
            int detailsEnd = description.endsWith(")")
                    ? description.length() - 1
                    : -1;

            if (detailsStart < 0 || detailsEnd <= detailsStart + 2) {
                return null;
            }

            String details = description.substring(
                    detailsStart + 2,
                    detailsEnd
            );

            String nationality = details.split("[,;]", 2)[0].trim();
            String normalized = nationality.toLowerCase(Locale.ROOT);

            if (nationality.isBlank()
                    || normalized.equals("unknown")
                    || normalized.startsWith("active ")
                    || normalized.startsWith("born ")
                    || Character.isDigit(nationality.charAt(0))) {
                return null;
            }

            return nationality;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Images(Image web, Image print) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Image(String url) {
    }
}
