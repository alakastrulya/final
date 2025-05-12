package com.mg.game.strategy;

import com.mg.game.GameScreen;
import com.mg.game.tank.Tank;

public class WanderStrategy implements EnemyStrategy {

    private float shootCooldown = 0;

    @Override
    public void update(Tank enemy, float delta, GameScreen context) {
        if (!enemy.isEnemy()) return;
        if (enemy.isInvulnerable()) {
            enemy.reduceInvulnerability(delta);
        }

        shootCooldown -= delta;

        // Simple random movement
        if (Math.random() < 0.02) {
            int dir = (int) (Math.random() * 4);
            switch (dir) {
                case 0:
                    enemy.setDirection(Tank.Direction.FORWARD);
                    break;
                case 1:
                    enemy.setDirection(Tank.Direction.BACKWARD);
                    break;
                case 2:
                    enemy.setDirection(Tank.Direction.LEFT);
                    break;
                case 3:
                    enemy.setDirection(Tank.Direction.RIGHT);
                    break;
            }
        }

        // Occasionally shoots forward
        if (shootCooldown <= 0 && Math.random() < 0.1) {
            enemy.shoot();
            shootCooldown = 1.5f;
        }
    }
}