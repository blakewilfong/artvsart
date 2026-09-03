package com.artvsart.integration.nga;

import com.artvsart.model.Artwork;
import com.artvsart.model.ArtworkStyleType;
import com.artvsart.repository.ArtworkRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NgaArtworkStyleImportServiceTest {

    @Test
    void importsStylesAndReportsCoverageForExistingNgaArtworks()
            throws Exception {
        NgaOpenDataClient client = mock(NgaOpenDataClient.class);
        ArtworkRepository repository = mock(ArtworkRepository.class);
        Artwork covered = artwork("1");
        Artwork uncovered = artwork("2");

        when(repository.findAllBySourceOrderByIdAsc("nga"))
                .thenReturn(List.of(covered, uncovered));

        when(client.read(eq("terms.csv"), any()))
                .thenAnswer(invocation -> {
                    NgaOpenDataClient.ReaderHandler<?> handler =
                            invocation.getArgument(1);

                    return handler.read(new StringReader(
                            "objectID,termType,term,visualBrowserStyle\n"
                                    + "1,Style,Impressionist,Impressionism\n"
                                    + "1,School,French School,\n"
                                    + "2,Theme,Landscape,\n"
                                    + "999,Style,Realist,Realism\n"
                    ));
                });

        NgaArtworkStyleImportService service =
                new NgaArtworkStyleImportService(
                        client,
                        repository,
                        "terms.csv"
                );

        NgaArtworkStyleImportService.Coverage coverage =
                service.importStyles();

        assertEquals(2, coverage.totalArtworkCount());
        assertEquals(1, coverage.coveredArtworkCount());
        assertEquals(2, coverage.distinctLabelCount());
        assertEquals(50.0, coverage.percentage());
        assertEquals(2, covered.getStyles().size());
        assertEquals(
                1,
                covered.getStyles().stream()
                        .filter(style -> style.getType()
                                == ArtworkStyleType.STYLE)
                        .count()
        );
        assertEquals(0, uncovered.getStyles().size());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Artwork>> saved =
                ArgumentCaptor.forClass(List.class);
        verify(repository).saveAll(saved.capture());
        assertEquals(2, saved.getValue().size());
    }

    private Artwork artwork(String sourceId) {
        return new Artwork(
                "nga", sourceId, "Title", "Artist", "1870", "image.jpg"
        );
    }
}
