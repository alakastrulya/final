package com.mg.game.bullet;

import com.mg.game.tank.Tank;
import com.mg.game.tank.factory.Factory;

public class BulletFactory implements Factory<Bullet, BulletParams>{

    @Override
    public Bullet create(BulletParams params) {
        return new Bullet(params.x, params.y, params.direction, params.color, params.fromEnemy);
    }
}
