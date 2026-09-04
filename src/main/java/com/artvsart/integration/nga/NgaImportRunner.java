package com.artvsart.integration.nga;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
@ConditionalOnProperty(
        name = "artvsart.import.nga.enabled",
        havingValue = "true"
)
public class NgaImportRunner implements ApplicationRunner {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(NgaImportRunner.class);

    private final NgaArtworkImportService service;
    private final NgaArtworkStyleImportService styleImportService;
    private final int targetSize;

    public NgaImportRunner(
            NgaArtworkImportService service,
            NgaArtworkStyleImportService styleImportService,
            @Value("${artvsart.import.nga.target-size:700}")
            int targetSize
    ) {
        this.service = service;
        this.styleImportService = styleImportService;
        this.targetSize = targetSize;
    }

    @Override
    public void run(ApplicationArguments args) {
        LOGGER.info(
                "Starting NGA artwork import with target size {}",
                targetSize
        );

        service.importPaintingPool(targetSize);
        styleImportService.importStyles();
    }
}
