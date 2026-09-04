package com.artvsart.service;

import com.artvsart.model.Artwork;
import com.artvsart.repository.ArtworkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class ArtistPopularityRankingService {

    private static final int FIRST_UNCATALOGED_RANK = 31;
    private static final int LAST_POPULARITY_RANK = 100;
    private static final int UNCATALOGED_RANK = Integer.MAX_VALUE;

    private final ArtworkRepository artworkRepository;
    private final ArtistPopularityCatalog catalog;

    public ArtistPopularityRankingService(
            ArtworkRepository artworkRepository,
            ArtistPopularityCatalog catalog
    ) {
        this.artworkRepository = artworkRepository;
        this.catalog = catalog;
    }

    @Transactional
    public int rankArtists() {
        List<Artwork> artworks = artworkRepository.findAll();
        Map<String, ArtistProfile> profiles = new HashMap<>();
        List<Artwork> changed = new ArrayList<>();

        for (Artwork artwork : artworks) {
            String artistName = artwork.getArtistName();

            if (!isIndividualArtist(artistName)) {
                if (artwork.getArtistPopularityRank() != null) {
                    artwork.clearArtistPopularityRank();
                    changed.add(artwork);
                }
                continue;
            }

            ArtistPopularityCatalog.CatalogEntry entry =
                    catalog.find(artistName).orElse(null);

            String identity = entry == null
                    ? catalog.normalize(artistName)
                    : entry.canonicalName();

            ArtistProfile profile = profiles.computeIfAbsent(
                    identity,
                    ignored -> new ArtistProfile(
                            identity,
                            entry == null
                                    ? UNCATALOGED_RANK
                                    : entry.rank()
                    )
            );

            profile.add(artwork);
        }

        List<ArtistProfile> rankedProfiles = new ArrayList<>(
                profiles.values()
        );

        rankedProfiles.sort(Comparator
                .comparingInt(ArtistProfile::catalogRank)
                .thenComparing(
                        ArtistProfile::sourceCount,
                        Comparator.reverseOrder()
                )
                .thenComparing(
                        ArtistProfile::artworkCount,
                        Comparator.reverseOrder()
                )
                .thenComparing(ArtistProfile::identity));

        Map<String, Integer> ranks = percentileRanks(rankedProfiles);

        for (ArtistProfile profile : rankedProfiles) {
            int rank = ranks.get(profile.identity());

            for (Artwork artwork : profile.artworks()) {
                if (!Integer.valueOf(rank).equals(
                        artwork.getArtistPopularityRank()
                )) {
                    artwork.rankArtistPopularity(rank);
                    changed.add(artwork);
                }
            }
        }

        if (!changed.isEmpty()) {
            artworkRepository.saveAll(changed);
        }

        return changed.size();
    }

    private Map<String, Integer> percentileRanks(
            List<ArtistProfile> profiles
    ) {
        Map<String, Integer> ranks = new HashMap<>();
        List<ArtistProfile> uncataloged = profiles.stream()
                .filter(profile -> profile.catalogRank()
                        == UNCATALOGED_RANK)
                .toList();
        int denominator = Math.max(1, uncataloged.size() - 1);
        int uncatalogedIndex = 0;

        for (ArtistProfile profile : profiles) {
            int rank;

            if (profile.catalogRank() != UNCATALOGED_RANK) {
                rank = profile.catalogRank();
            } else {
                rank = FIRST_UNCATALOGED_RANK
                        + (int) Math.floor(
                                uncatalogedIndex
                                        * (LAST_POPULARITY_RANK
                                        - FIRST_UNCATALOGED_RANK)
                                        / (double) denominator
                        );
                uncatalogedIndex++;
            }

            ranks.put(profile.identity(), rank);
        }

        return ranks;
    }

    private boolean isIndividualArtist(String artistName) {
        String normalized = catalog.normalize(artistName);

        return !normalized.isBlank()
                && !normalized.contains("unknown")
                && !normalized.contains("anonymous")
                && !normalized.contains("unidentified")
                && !normalized.contains("various artists")
                && !normalized.contains("century")
                && !normalized.endsWith(" painter")
                && !normalized.equals("italian")
                && !normalized.equals("american")
                && !normalized.equals("dutch")
                && !normalized.equals("french")
                && !normalized.equals("german")
                && !normalized.equals("spanish");
    }

    private static final class ArtistProfile {

        private final String identity;
        private final int catalogRank;
        private final List<Artwork> artworks = new ArrayList<>();
        private final Set<String> sources = new HashSet<>();

        private ArtistProfile(
                String identity,
                int catalogRank
        ) {
            this.identity = identity;
            this.catalogRank = catalogRank;
        }

        private void add(Artwork artwork) {
            artworks.add(artwork);

            if (artwork.getSource() != null) {
                sources.add(artwork.getSource().toLowerCase(Locale.ROOT));
            }
        }

        private String identity() {
            return identity;
        }

        private int catalogRank() {
            return catalogRank;
        }

        private Integer sourceCount() {
            return sources.size();
        }

        private Integer artworkCount() {
            return artworks.size();
        }

        private List<Artwork> artworks() {
            return artworks;
        }
    }
}
