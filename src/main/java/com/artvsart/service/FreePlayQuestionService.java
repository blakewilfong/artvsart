package com.artvsart.service;

import com.artvsart.model.Artwork;
import com.artvsart.model.ArtworkAnswer;
import com.artvsart.model.ArtworkQuestion;
import com.artvsart.model.GameMode;
import com.artvsart.model.QuestionType;
import com.artvsart.repository.ArtworkAnswerRepository;
import com.artvsart.repository.ArtworkQuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class FreePlayQuestionService {

    private final ArtworkService artworkService;
    private final OlderArtworkQuestionService olderArtworkService;
    private final ArtworkQuestionRepository questionRepository;
    private final ArtworkAnswerRepository answerRepository;

    public FreePlayQuestionService(
            ArtworkService artworkService,
            OlderArtworkQuestionService olderArtworkService,
            ArtworkQuestionRepository questionRepository,
            ArtworkAnswerRepository answerRepository
    ) {
        this.artworkService = artworkService;
        this.olderArtworkService = olderArtworkService;
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
    }

    @Transactional
    public ArtworkQuestion createQuestion() {
        List<Artwork> artworks =
                artworkService.getPlayableArtworks();

        List<ArtworkPair> eligiblePairs =
                createEligiblePairs(artworks);

        if (eligiblePairs.isEmpty()) {
            throw new IllegalStateException(
                    "No artwork pairs satisfy the minimum date difference"
            );
        }

        ArtworkPair selectedPair = eligiblePairs.get(
                ThreadLocalRandom.current().nextInt(
                        eligiblePairs.size()
                )
        );

        Artwork correctArtwork =
                olderArtworkService.getCorrectArtwork(
                        selectedPair.artworkOne(),
                        selectedPair.artworkTwo()
                );

        ArtworkQuestion question =
                new ArtworkQuestion(
                        GameMode.FREE_PLAY,
                        QuestionType.OLDER_ARTWORK,
                        selectedPair.artworkOne(),
                        selectedPair.artworkTwo(),
                        correctArtwork
                );

        return questionRepository.save(question);
    }

    @Transactional(readOnly = true)
    public ArtworkQuestion getQuestion(Long questionId) {
        ArtworkQuestion question = questionRepository
                .findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Question does not exist"
                ));

        if (question.getGameMode() != GameMode.FREE_PLAY) {
            throw new IllegalArgumentException(
                    "Question is not a Free Play question"
            );
        }

        return question;
    }

    @Transactional
    public ArtworkAnswer answerQuestion(
            Long questionId,
            Long selectedArtworkId,
            String voterId
    ) {
        if (voterId == null || voterId.isBlank()) {
            throw new IllegalArgumentException(
                    "A voter ID is required"
            );
        }

        ArtworkQuestion question =
                getQuestion(questionId);

        Optional<ArtworkAnswer> existingAnswer =
                answerRepository
                        .findByQuestionIdAndVoterId(
                                questionId,
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
        if (voterId == null || voterId.isBlank()) {
            return Optional.empty();
        }

        return answerRepository
                .findByQuestionIdAndVoterId(
                        questionId,
                        voterId
                );
    }

    private List<ArtworkPair> createEligiblePairs(
            List<Artwork> artworks
    ) {
        List<ArtworkPair> eligiblePairs =
                new ArrayList<>();

        for (int firstIndex = 0;
             firstIndex < artworks.size();
             firstIndex++) {

            for (int secondIndex = firstIndex + 1;
                 secondIndex < artworks.size();
                 secondIndex++) {

                Artwork artworkOne =
                        artworks.get(firstIndex);

                Artwork artworkTwo =
                        artworks.get(secondIndex);

                if (olderArtworkService.isEligiblePair(
                        artworkOne,
                        artworkTwo
                )) {
                    eligiblePairs.add(
                            new ArtworkPair(
                                    artworkOne,
                                    artworkTwo
                            )
                    );
                }
            }
        }

        return eligiblePairs;
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

    private record ArtworkPair(
            Artwork artworkOne,
            Artwork artworkTwo
    ) {
    }
}