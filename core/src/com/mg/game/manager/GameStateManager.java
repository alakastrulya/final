package com.mg.game.manager;

public class GameStateManager {
    private boolean paused = false;
    private boolean gameOver = false;
    private boolean levelComplete = false;
    private float levelCompleteTimer = 0f;

    private static final float LEVEL_COMPLETE_DELAY = 2.0f;

    public void update(float delta) {
        if (levelComplete) {
            levelCompleteTimer += delta;
        }
    }

    public void triggerGameOver() {
        gameOver = true;
    }

    public void triggerLevelComplete() {
        levelComplete = true;
        levelCompleteTimer = 0f;
    }

    public boolean isPaused() {
        return paused;
    }

    public void togglePause() {
        paused = !paused;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public boolean isLevelComplete() {
        return levelComplete;
    }

    public boolean shouldSwitchToLevelCompleteScreen() {
        return levelComplete && levelCompleteTimer >= LEVEL_COMPLETE_DELAY;
    }
}
