package com.artvsart.service;

import com.artvsart.dto.DailyGameScore;
import com.artvsart.model.DailyGame;
import com.artvsart.model.PredictionOutcome;
import com.artvsart.model.Vote;
import com.artvsart.repository.VoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class GameProgressService {

    private final VoteRepository voteRepository;

    public GameProgressService(
            VoteRepository voteRepository
    ) {
        this.voteRepository = voteRepository;
    }

    @Transactional(readOnly = true)
    public DailyGameScore getScore(
            DailyGame dailyGame,
            String voterId
    ) {
        List<Vote> votes = voteRepository.findGameVotes(
                dailyGame.getId(),
                voterId
        );

        int correctPredictions = (int) votes.stream()
                .filter(vote -> vote.getOutcome()
                        == PredictionOutcome.CORRECT)
                .count();

        int incorrectPredictions = (int) votes.stream()
                .filter(vote -> vote.getOutcome()
                        == PredictionOutcome.INCORRECT)
                .count();

        int ties = (int) votes.stream()
                .filter(vote -> vote.getOutcome()
                        == PredictionOutcome.TIE)
                .count();

        Set<Integer> completedRounds = votes.stream()
                .map(vote -> vote
                        .getMatchup()
                        .getRoundNumber())
                .collect(Collectors.toSet());

        int nextRoundNumber = IntStream
                .rangeClosed(
                        1,
                        dailyGame.getTotalRounds()
                )
                .filter(round -> !completedRounds.contains(round))
                .findFirst()
                .orElse(0);

        return new DailyGameScore(
                dailyGame.getTotalRounds(),
                votes.size(),
                correctPredictions,
                incorrectPredictions,
                ties,
                nextRoundNumber
        );
    }
}