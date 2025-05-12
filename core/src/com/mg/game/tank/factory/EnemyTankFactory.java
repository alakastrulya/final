package com.mg.game.tank.factory;

import com.mg.game.GameScreen;
import com.mg.game.tank.Tank;

public class EnemyTankFactory implements Factory {
    private final String color;
    private final int level;
    private final GameScreen screen; // Новое поле

    public EnemyTankFactory(String color, int level, GameScreen screen) {
        this.color = color;
        this.level = level;
        this.screen = screen;
    }

    public Tank create() {
        return new Tank(color, level, true, screen);
    }
}