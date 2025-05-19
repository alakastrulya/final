package com.mg.game.tank.factory;

public interface Factory<T, P> {
    T create(P params);
}
