package com.mg.game.bullet;
import com.mg.game.tank.Tank;

public class BulletParams {
    public float x, y;
    public Tank.Direction direction;
    public String color;
    public boolean fromEnemy;

    public BulletParams(float x, float y, Tank.Direction direction, String color, boolean fromEnemy) {
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.color = color;
        this.fromEnemy = fromEnemy;
    }
}

