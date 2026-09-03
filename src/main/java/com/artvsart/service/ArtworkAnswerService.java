package com.artvsart.service;

import com.artvsart.model.Artwork;
import com.artvsart.model.ArtworkAnswer;
import com.artvsart.model.ArtworkQuestion;
import com.artvsart.repository.ArtworkAnswerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ArtworkAnswerService {

    private final ArtworkAnswerRepository answerRepository;

    public ArtworkAnswerService(
            ArtworkAnswerRepository answerRepository
    ) {
        this.answerRepository = answerRepository;
    }

    @Transactional
    public ArtworkAnswer answerQuestion(
            ArtworkQuestion question,
            Long selectedArtworkId,
            String voterId
    ) {
        if (question == null || question.getId() == null) {
            throw new IllegalArgumentException(
                    "A saved question is required"
            );
        }

        if (voterId == null || voterId.isBlank()) {
            throw new IllegalArgumentException(
                    "A voter ID is required"
            );
        }

        Optional<ArtworkAnswer> existingAnswer =
                answerRepository
                        .findByQuestionIdAndVoterId(
                                question.getId(),
                                voterId
                        );

        if (existingAnswer.isPresent()) {
            return existingAnswer.get();
        }

        Artwork selectedArtwork = resolveSelectedArtwork(
                question,
                selectedArtworkId
        );

        ArtworkAnswer answer = new ArtworkAnswer(
                question,
                selectedArtwork,
                voterId
        );

        return answerRepository.save(answer);
    }

    @Transactional(readOnly = true)
    public Optional<ArtworkAnswer> findAnswer(
            Long questionId,
            String voterId
    ) {
        if (questionId == null
                || voterId == null
                || voterId.isBlank()) {
            return Optional.empty();
        }

        return answerRepository
                .findByQuestionIdAndVoterId(
                        questionId,
                        voterId
                );
    }

    private Artwork resolveSelectedArtwork(
            ArtworkQuestion question,
            Long selectedArtworkId
    ) {
        if (selectedArtworkId == null) {
            throw new IllegalArgumentException(
                    "A selected artwork ID is required"
            );
        }

        if (selectedArtworkId.equals(
                question.getArtworkOne().getId()
        )) {
            return question.getArtworkOne();
        }

        if (selectedArtworkId.equals(
                question.getArtworkTwo().getId()
        )) {
            return question.getArtworkTwo();
        }

        throw new IllegalArgumentException(
                "Selected artwork is not part of this question"
        );
    }
}