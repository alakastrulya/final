package com.mg.game.tank.factory;

import com.mg.game.GameScreen;
import com.mg.game.bullet.BulletFactory;
import com.mg.game.tank.Tank;

public class EnemyTankFactory implements Factory<Tank> {
    private final String color;
    private final int level;
    private final GameScreen screen;

    public EnemyTankFactory(String color, int level, GameScreen screen) {
        this.color = color;
        this.level = level;
        this.screen = screen;
    }

    @Override
    public Tank create() {
        return new Tank(color, level, true, screen);
    }
}