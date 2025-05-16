package com.mg.game.strategy;

import com.mg.game.GameScreen;
import com.mg.game.map.MapTile;
import com.mg.game.tank.Tank;

public class BaseAttackStrategy implements EnemyStrategy {

    @Override
    public void update(Tank enemy, float delta, GameScreen context) {
        if (!enemy.isEnemy()) return;

        if (enemy.isInvulnerable()) {
            enemy.reduceInvulnerability(delta);
        }

        // Get base coordinates
        int baseX = context.getBaseX();
        int baseY = context.getBaseY();

        // Move toward the base
        int dx = baseX - enemy.positionX;
        int dy = baseY - enemy.positionY;

        if (Math.abs(dx) > Math.abs(dy)) {
            enemy.setDirection(dx > 0 ? Tank.Direction.RIGHT : Tank.Direction.LEFT);
        } else {
            enemy.setDirection(dy > 0 ? Tank.Direction.BACKWARD : Tank.Direction.FORWARD);
        }

        // Check if the base exists
        MapTile baseTile = context.getBaseTile();
        if (baseTile != null) {
            // Create a temporary "dummy" tank at the base's position
            Tank dummyBase = new Tank();
            dummyBase.positionX = baseX;
            dummyBase.positionY = baseY;

            // If enemy sees the base or is close to it — shoot
            boolean closeEnough = enemy.getBounds().overlaps(dummyBase.getBounds());
            boolean seesBase = enemy.isInLineOfSight(dummyBase);

            if ((closeEnough || seesBase) && Math.random() < 0.3) {
                enemy.shoot();
            }
        }
    }
}