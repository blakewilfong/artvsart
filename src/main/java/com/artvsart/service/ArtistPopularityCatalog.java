package com.artvsart.service;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Component
public class ArtistPopularityCatalog {

    private static final int MAXIMUM_CATALOG_RANK = 30;

    private static final List<List<String>> POPULAR_ARTISTS = List.of(
            List.of("Vincent van Gogh", "Vincent Willem van Gogh"),
            List.of("Leonardo da Vinci"),
            List.of("Pablo Picasso"),
            List.of("Claude Monet"),
            List.of("Rembrandt", "Rembrandt van Rijn"),
            List.of("Michelangelo", "Michelangelo Buonarroti"),
            List.of("Salvador Dali", "Salvador Dali Domenech"),
            List.of("Frida Kahlo"),
            List.of("Johannes Vermeer", "Jan Vermeer"),
            List.of("Edgar Degas"),
            List.of("Paul Cezanne"),
            List.of("Georgia O'Keeffe"),
            List.of("Raphael", "Raffaello Sanzio"),
            List.of("Sandro Botticelli"),
            List.of("Pierre-Auguste Renoir", "Auguste Renoir"),
            List.of("Edouard Manet"),
            List.of("Francisco Goya", "Francisco de Goya"),
            List.of("Paul Gauguin"),
            List.of("Henri Matisse"),
            List.of("Gustav Klimt"),
            List.of(
                    "J. M. W. Turner",
                    "Joseph Mallord William Turner"
            ),
            List.of("Katsushika Hokusai"),
            List.of("Piet Mondrian"),
            List.of("Georges Seurat"),
            List.of("El Greco", "Domenikos Theotokopoulos"),
            List.of("Titian", "Tiziano Vecellio"),
            List.of("Caravaggio", "Michelangelo Merisi da Caravaggio"),
            List.of("Wassily Kandinsky"),
            List.of("Jackson Pollock"),
            List.of("Andy Warhol"),
            List.of("Edward Hopper"),
            List.of("John Singer Sargent"),
            List.of("Eugene Delacroix"),
            List.of("Diego Rivera"),
            List.of("Joan Miro"),
            List.of("Henri Rousseau"),
            List.of("Marc Chagall"),
            List.of("Edvard Munch"),
            List.of("Jean-Michel Basquiat"),
            List.of("Roy Lichtenstein"),
            List.of("Mary Cassatt"),
            List.of("Berthe Morisot"),
            List.of("Camille Pissarro"),
            List.of("Alfred Sisley"),
            List.of("Winslow Homer"),
            List.of("Thomas Gainsborough"),
            List.of("John Constable"),
            List.of("Anthony van Dyck", "Sir Anthony van Dyck"),
            List.of("Frans Hals"),
            List.of("Hans Holbein the Younger")
    );

    private final Map<String, CatalogEntry> entries;

    public ArtistPopularityCatalog() {
        Map<String, CatalogEntry> catalog = new LinkedHashMap<>();

        for (int index = 0; index < POPULAR_ARTISTS.size(); index++) {
            List<String> aliases = POPULAR_ARTISTS.get(index);
            CatalogEntry entry = new CatalogEntry(
                    normalize(aliases.getFirst()),
                    1 + (int) Math.floor(
                            index * (MAXIMUM_CATALOG_RANK - 1.0)
                                    / (POPULAR_ARTISTS.size() - 1)
                    )
            );

            aliases.forEach(alias -> catalog.put(
                    normalize(alias),
                    entry
            ));
        }

        entries = Map.copyOf(catalog);
    }

    public Optional<CatalogEntry> find(String artistName) {
        return Optional.ofNullable(entries.get(normalize(artistName)));
    }

    public String normalize(String artistName) {
        if (artistName == null) {
            return "";
        }

        String withoutDetails = artistName
                .replaceAll("\\s*\\([^)]*\\)\\s*$", "")
                .replaceFirst("(?i)^sir\\s+", "");

        return Normalizer.normalize(
                        withoutDetails,
                        Normalizer.Form.NFD
                )
                .replaceAll("\\p{M}", "")
                .replaceAll("[^\\p{L}\\p{N}]+", " ")
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    public record CatalogEntry(
            String canonicalName,
            int rank
    ) {
    }
}
