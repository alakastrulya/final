package com.mg.game.observer;

public class GameContext {
    public final int level;
    public final int playerCount;
    public final String destroyedBy;

    public GameContext(int level, int playerCount, String destroyedBy) {
        this.level = level;
        this.playerCount = playerCount;
        this.destroyedBy = destroyedBy;
    }
}

