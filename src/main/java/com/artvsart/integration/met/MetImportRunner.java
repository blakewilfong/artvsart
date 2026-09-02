package com.artvsart.integration.met;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
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

    public MetImportRunner(
            MetArtworkImportService importService,
            @Value("${artvsart.import.met.target-size:200}")
            int targetSize
    ) {
        this.importService = importService;
        this.targetSize = targetSize;
    }

    @Override
    public void run(ApplicationArguments args) {
        LOGGER.info(
                "Starting Met artwork import with target size {}",
                targetSize
        );

        int importedCount =
                importService.importPaintingPool(targetSize);

        LOGGER.info(
                "Met artwork import completed with {} new artworks",
                importedCount
        );
    }
}