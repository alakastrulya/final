package com.mg.game.strategy;

import com.mg.game.GameScreen;
import com.mg.game.tank.Tank;

public interface EnemyStrategy {
    void update(Tank enemy, float delta, GameScreen context);
}
