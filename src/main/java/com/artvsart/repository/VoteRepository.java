package com.artvsart.repository;

import com.artvsart.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface VoteRepository extends JpaRepository<Vote, Long> {

    Optional<Vote> findByMatchupIdAndVoterId(
            Long matchupId,
            String voterId
    );

    long countByMatchupIdAndSelectedArtworkId(
            Long matchupId,
            Long selectedArtworkId
    );

    long countBySelectedArtworkId(Long artworkId);

    @Query("""
        SELECT COUNT(v)
        FROM Vote v
        WHERE v.matchup.artworkOne.id = :artworkId
           OR v.matchup.artworkTwo.id = :artworkId
        """)
    long countPresentationsByArtworkId(
            @Param("artworkId") Long artworkId
    );
}