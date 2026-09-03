package com.artvsart.service;

import com.artvsart.model.Artwork;
import com.artvsart.model.ArtworkAnswer;
import com.artvsart.model.ArtworkQuestion;
import com.artvsart.model.GameMode;
import com.artvsart.model.QuestionType;
import com.artvsart.repository.ArtworkQuestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FreePlayQuestionServiceTest {

    private static final String VOTER_ID = "voter-1";

    @Mock
    private ArtworkService artworkService;

    @Mock
    private OlderArtworkQuestionService olderArtworkService;

    @Mock
    private ArtworkQuestionRepository questionRepository;

    @Mock
    private ArtworkAnswerService answerService;

    private FreePlayQuestionService service;

    @BeforeEach
    void setUp() {
        service = new FreePlayQuestionService(
                artworkService,
                olderArtworkService,
                questionRepository,
                answerService
        );
    }

    @Test
    void createsOlderArtworkQuestionFromEligiblePair() {
        Artwork older = artwork(1L);
        Artwork newer = artwork(2L);

        when(artworkService.getPlayableArtworks())
                .thenReturn(List.of(older, newer));

        when(olderArtworkService.isEligiblePair(
                older,
                newer
        )).thenReturn(true);

        when(olderArtworkService.getCorrectArtwork(
                older,
                newer
        )).thenReturn(older);

        when(questionRepository.save(
                any(ArtworkQuestion.class)
        )).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        ArtworkQuestion question =
                service.createQuestion();

        assertSame(
                GameMode.STREAK,
                question.getGameMode()
        );

        assertSame(
                QuestionType.OLDER_ARTWORK,
                question.getQuestionType()
        );

        assertSame(
                older,
                question.getArtworkOne()
        );

        assertSame(
                newer,
                question.getArtworkTwo()
        );

        assertSame(
                older,
                question.getCorrectArtwork()
        );
    }

    @Test
    void delegatesAnswerRecording() {
        ArtworkQuestion question =
                mock(ArtworkQuestion.class);

        ArtworkAnswer answer =
                mock(ArtworkAnswer.class);

        when(question.getGameMode())
                .thenReturn(GameMode.STREAK);

        when(questionRepository.findById(10L))
                .thenReturn(Optional.of(question));

        when(answerService.answerQuestion(
                question,
                1L,
                VOTER_ID
        )).thenReturn(answer);

        ArtworkAnswer result =
                service.answerQuestion(
                        10L,
                        1L,
                        VOTER_ID
                );

        assertSame(answer, result);
    }

    @Test
    void delegatesAnswerLookup() {
        ArtworkAnswer answer =
                mock(ArtworkAnswer.class);

        when(answerService.findAnswer(
                10L,
                VOTER_ID
        )).thenReturn(Optional.of(answer));

        Optional<ArtworkAnswer> result =
                service.findAnswer(
                        10L,
                        VOTER_ID
                );

        assertSame(
                answer,
                result.orElseThrow()
        );
    }

    private Artwork artwork(Long id) {
        Artwork artwork = mock(Artwork.class);

        when(artwork.getId()).thenReturn(id);

        return artwork;
    }
}