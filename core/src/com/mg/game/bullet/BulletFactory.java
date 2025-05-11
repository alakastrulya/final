package com.mg.game.bullet;

import com.mg.game.tank.Tank;
import com.mg.game.tank.factory.Factory;

public class BulletFactory implements Factory {
    private final float x, y;
    private final Tank.Direction direction;
    private final String color;
    private final boolean fromEnemy;

    public BulletFactory(float x, float y, Tank.Direction direction, String color, boolean fromEnemy) {
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.color = color;
        this.fromEnemy = fromEnemy;
    }

    public Bullet create() {
        return new Bullet(x, y, direction, color, fromEnemy);
    }
}
