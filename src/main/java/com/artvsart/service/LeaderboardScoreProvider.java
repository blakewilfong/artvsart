package com.artvsart.service;

import com.artvsart.model.GameMode;
import com.artvsart.model.GameRun;

public interface LeaderboardScoreProvider {

    GameMode getGameMode();

    int getScore(GameRun run);
}
