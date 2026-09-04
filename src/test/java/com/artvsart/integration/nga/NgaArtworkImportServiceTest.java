package com.artvsart.integration.nga;

import com.artvsart.model.Artwork;
import com.artvsart.repository.ArtworkRepository;
import com.artvsart.service.ArtworkGenreClassifier;
import com.artvsart.service.BalancedPoolSelector;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.StringReader;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NgaArtworkImportServiceTest {

    @Test
    void importsEligiblePaintingWithPrimaryArtistAndIiifImage() {
        NgaOpenDataClient client = mock(NgaOpenDataClient.class);
        ArtworkRepository repository = mock(ArtworkRepository.class);

        stubDatasets(
                client,
                Map.of(
                        "objects.csv",
                        "objectID,title,displayDate,beginYear,endYear,medium,departmentAbbr,classification,subClassification,visualBrowserClassification,isVirtual\n"
                                + "1,<p>Blue Vase</p>,c. 1890,1890,1900,Oil on canvas,PP,Painting,,,0\n",
                        "constituents.csv",
                        "constituentID,forwardDisplayName,nationality,beginYear,endYear\n"
                                + "7,Jane Artist,American,1860,1930\n",
                        "objects_constituents.csv",
                        "objectID,constituentID,displayOrder,roleType\n"
                                + "1,7,1,artist\n",
                        "published_images.csv",
                        "depictstmsobjectid,openaccess,viewtype,iiifurl,iiifthumburl\n"
                                + "1,1,primary,https://api.nga.gov/iiif/abc,https://example.test/thumb.jpg\n"
                        ,
                        "terms.csv",
                        "objectID,termType,term,visualBrowserTheme\n"
                                + "1,Theme,Landscape,landscape\n"
                )
        );

        when(repository.findAllBySourceOrderByIdAsc("nga"))
                .thenReturn(List.of());
        when(repository.saveAll(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        NgaArtworkImportService service = createService(
                client,
                repository
        );

        assertEquals(1, service.importPaintingPool(1));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Artwork>> saved =
                ArgumentCaptor.forClass(List.class);

        verify(repository).saveAll(saved.capture());

        Artwork artwork = saved.getValue().getFirst();

        assertEquals("Blue Vase", artwork.getTitle());
        assertEquals("Jane Artist", artwork.getArtistName());
        assertEquals(1860, artwork.getArtistBeginYear());
        assertEquals(1890, artwork.getObjectBeginYear());
        assertEquals("PP", artwork.getDepartment());
        assertEquals(
                com.artvsart.model.ArtworkGenre.LANDSCAPE,
                artwork.getGenre()
        );
        assertEquals(
                "https://api.nga.gov/iiif/abc/full/!1600,1600/0/default.jpg",
                artwork.getImageUrl()
        );
        assertEquals(
                "https://api.nga.gov/iiif/abc/full/full/0/default.jpg",
                artwork.getOriginalImageUrl()
        );
    }

    @Test
    void rejectsNonFlatArtworkEvenWhenItsMediumMentionsPaint() {
        NgaOpenDataClient client = mock(NgaOpenDataClient.class);
        ArtworkRepository repository = mock(ArtworkRepository.class);

        stubDatasets(
                client,
                Map.of(
                        "objects.csv",
                        "objectID,title,displayDate,beginYear,medium,classification,isVirtual\n"
                                + "1,Painted Figure,1900,1900,Painted bronze,Sculpture,0\n",
                        "constituents.csv",
                        "constituentID,forwardDisplayName\n7,Artist\n",
                        "objects_constituents.csv",
                        "objectID,constituentID,displayOrder,roleType\n1,7,1,artist\n",
                        "published_images.csv",
                        "depictstmsobjectid,openaccess,viewtype,iiifthumburl\n"
                                + "1,1,primary,https://example.test/thumb.jpg\n"
                )
        );

        when(repository.findAllBySourceOrderByIdAsc("nga"))
                .thenReturn(List.of());

        NgaArtworkImportService service = createService(
                client,
                repository
        );

        assertEquals(0, service.importPaintingPool(1));
        verify(repository).saveAll(List.of());
    }

    @Test
    void skipsDownloadWhenNgaPoolAlreadyMeetsTarget() {
        NgaOpenDataClient client = mock(NgaOpenDataClient.class);
        ArtworkRepository repository = mock(ArtworkRepository.class);
        Artwork existing = new Artwork(
                "nga",
                "1",
                "Existing",
                "Artist",
                "1900",
                "https://example.test/image.jpg"
        );

        when(repository.findAllBySourceOrderByIdAsc("nga"))
                .thenReturn(List.of(existing));

        NgaArtworkImportService service = createService(
                client,
                repository
        );

        assertEquals(0, service.importPaintingPool(1));
        verify(client, never()).read(anyString(), any());
        verify(repository, never()).saveAll(any());
    }

    private NgaArtworkImportService createService(
            NgaOpenDataClient client,
            ArtworkRepository repository
    ) {
        return new NgaArtworkImportService(
                client,
                repository,
                new NgaArtworkEligibilityPolicy(
                        new ArtworkGenreClassifier()
                ),
                new ArtworkGenreClassifier(),
                new BalancedPoolSelector(),
                "objects.csv",
                "constituents.csv",
                "objects_constituents.csv",
                "published_images.csv",
                "terms.csv"
        );
    }

    private void stubDatasets(
            NgaOpenDataClient client,
            Map<String, String> datasets
    ) {
        when(client.read(anyString(), any()))
                .thenAnswer(invocation -> {
                    String url = invocation.getArgument(0);
                    NgaOpenDataClient.ReaderHandler<?> handler =
                            invocation.getArgument(1);

                    return handler.read(
                            new StringReader(datasets.getOrDefault(
                                    url,
                                    "objectID,termType,term,visualBrowserTheme\n"
                            ))
                    );
                });
    }
}
