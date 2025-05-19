package com.mg.game.observer;

import java.util.ArrayList;
import java.util.List;

public class GameEventDispatcher implements GameEventPublisher {
    private final List<GameObserver> observers = new ArrayList<>();

    @Override
    public void addObserver(GameObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void removeObserver(GameObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyBaseDestroyed(GameContext context) {
        for (GameObserver o : observers) {
            o.onBaseDestroyed(context);
        }
    }
}
