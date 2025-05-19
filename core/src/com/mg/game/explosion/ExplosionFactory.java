package com.mg.game.explosion;

import com.mg.game.tank.factory.Factory;

public class ExplosionFactory implements Factory<Explosion, ExplosionParams>  {

    @Override
    public Explosion create(ExplosionParams params) {
        return new Explosion(params.x, params.y);
    }
}
