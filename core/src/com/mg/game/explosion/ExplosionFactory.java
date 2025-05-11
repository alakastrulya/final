package com.mg.game.explosion;

import com.mg.game.tank.factory.Factory;

public class ExplosionFactory implements Factory {
    private final float x, y;

    public ExplosionFactory(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Explosion create() {
        return new Explosion(x, y);
    }
}
