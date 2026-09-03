package com.artvsart.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class GameTimeConfiguration {

    @Bean
    public Clock leaderboardClock(
            @Value("${artvsart.game.time-zone:America/Chicago}")
            String timeZone
    ) {
        return Clock.system(ZoneId.of(timeZone));
    }
}
