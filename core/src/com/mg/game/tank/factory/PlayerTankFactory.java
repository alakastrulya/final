package com.mg.game.tank.factory;

import com.mg.game.tank.Tank;

public class PlayerTankFactory implements Factory {
    private final String color;
    private final int level;

    public PlayerTankFactory(String color, int level) {
        this.color = color;
        this.level = level;
    }

    public Tank create() {
        return new Tank(color, level, false);
    }
}
