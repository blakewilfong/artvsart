package com.artvsart.integration.met;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
@ConditionalOnProperty(
        name = "artvsart.import.met.enabled",
        havingValue = "true"
)
public class MetImportRunner implements ApplicationRunner {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    MetImportRunner.class
            );

    private final MetArtworkImportService importService;
    private final int targetSize;
    private final boolean refreshMetadata;

    public MetImportRunner(
            MetArtworkImportService importService,
            @Value("${artvsart.import.met.target-size:700}")
            int targetSize,
            @Value("${artvsart.import.met.refresh-metadata:false}")
            boolean refreshMetadata
    ) {
        this.importService = importService;
        this.targetSize = targetSize;
        this.refreshMetadata = refreshMetadata;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (refreshMetadata) {
            LOGGER.info(
                    "Starting Met artwork metadata refresh"
            );

            int refreshedCount =
                    importService
                            .refreshImportedMetadata();

            LOGGER.info(
                    "Met metadata refresh completed for {} artworks",
                    refreshedCount
            );

            return;
        }

        LOGGER.info(
                "Starting balanced Met artwork import with target size {}",
                targetSize
        );

        int importedCount =
                importService.importPaintingPool(
                        targetSize
                );

        LOGGER.info(
                "Met artwork import completed with {} new artworks",
                importedCount
        );
    }
}
