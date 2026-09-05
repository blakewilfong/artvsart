package com.artvsart.integration.cma;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(3)
@ConditionalOnProperty(
        name = "artvsart.import.cma.enabled",
        havingValue = "true"
)
public class CmaImportRunner implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(
            CmaImportRunner.class
    );

    private final CmaArtworkImportService importService;
    private final int targetSize;
    private final Integer createdAfterYear;
    private final int maximumWorksPerArtist;

    public CmaImportRunner(
            CmaArtworkImportService importService,
            @Value("${artvsart.import.cma.target-size:600}")
            int targetSize,
            @Value("${artvsart.import.cma.created-after:1750}")
            Integer createdAfterYear,
            @Value("${artvsart.import.cma.max-per-artist:5}")
            int maximumWorksPerArtist
    ) {
        this.importService = importService;
        this.targetSize = targetSize;
        this.createdAfterYear = createdAfterYear;
        this.maximumWorksPerArtist = maximumWorksPerArtist;
    }

    @Override
    public void run(ApplicationArguments args) {
        LOGGER.info(
                "Starting CMA modern painting import with target size {}",
                targetSize
        );

        importService.importModernPaintingPool(
                targetSize,
                createdAfterYear,
                maximumWorksPerArtist
        );
    }
}
