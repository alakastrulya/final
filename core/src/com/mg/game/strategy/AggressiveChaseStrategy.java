package com.mg.game.strategy;

import com.mg.game.GameScreen;
import com.mg.game.tank.Tank;

public class AggressiveChaseStrategy implements EnemyStrategy {

    @Override
    public void update(Tank enemy, float delta, GameScreen context) {
        if (!enemy.isEnemy()) return;

        // Update invulnerability timer
        if (enemy.isInvulnerable()) {
            enemy.reduceInvulnerability(delta);
        }

        // Get nearest alive player
        Tank target = context.getNearestAlivePlayer(enemy);
        if (target == null) return;

        // If the target is in line of sight — high chance to shoot
        if (enemy.isInLineOfSight(target)) {
            if (Math.random() < 0.85) { // 90% chance
                enemy.shoot();
            }
        } else {
            // If just nearby (e.g., < 120 pixels), low chance to shoot
            float dx = target.positionX - enemy.positionX;
            float dy = target.positionY - enemy.positionY;
            float distSq = dx * dx + dy * dy;

            if (distSq < 120 * 120 && Math.random() < 0.3) {
                enemy.shoot();
            }
        }
    }
}