package com.mg.game.tank.factory;

import com.mg.game.CollisionManager;
import com.mg.game.GameScreen;
import com.mg.game.tank.Tank;

public class EnemyTankFactory implements Factory {
    private String colour;
    private int level;
    private GameScreen screen;
    private CollisionManager collisionManager; // New field

    public EnemyTankFactory(String colour, int level, GameScreen screen) {
        this(colour, level, screen, null);
    }

    public EnemyTankFactory(String colour, int level, GameScreen screen, CollisionManager collisionManager) {
        this.colour = colour;
        this.level = level;
        this.screen = screen;
        this.collisionManager = collisionManager;
    }

    @Override
    public Tank create() {
        return new Tank(colour, level, true, screen, collisionManager);
    }
}