package com.mg.game.observer;

public interface GameEventPublisher {
    void addObserver(GameObserver observer);
    void removeObserver(GameObserver observer);
    void notifyBaseDestroyed(GameContext context);
}

