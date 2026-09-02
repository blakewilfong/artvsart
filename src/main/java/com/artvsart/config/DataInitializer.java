package com.artvsart.config;

import com.artvsart.service.DailyGameService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final DailyGameService dailyGameService;

    public DataInitializer(
            DailyGameService dailyGameService
    ) {
        this.dailyGameService = dailyGameService;
    }

    @Override
    public void run(String... args) {
        dailyGameService.getOrCreateTodaysGame();
    }
}