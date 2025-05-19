package com.mg.game.tank.factory;

import com.mg.game.manager.CollisionManager;
import com.mg.game.GameScreen;
import com.mg.game.tank.Tank;

public class PlayerTankFactory implements Factory<Tank, TankParams> {

    @Override
    public Tank create(TankParams params) {
        return new Tank(params.colour, params.level, false,
                params.screen, params.collisionManager);
    }
}

