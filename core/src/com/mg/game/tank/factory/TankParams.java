package com.mg.game.tank.factory;

import com.mg.game.GameScreen;
import com.mg.game.manager.CollisionManager;

public class TankParams {
    public String colour;
    public int level;
    public boolean isEnemy;
    public GameScreen screen;
    public com.mg.game.manager.CollisionManager collisionManager;

    public TankParams(String colour, int level, boolean isEnemy, GameScreen screen, CollisionManager collisionManager) {
        this.colour = colour;
        this.level = level;
        this.isEnemy = isEnemy;
        this.screen = screen;
        this.collisionManager = collisionManager;
    }
}


