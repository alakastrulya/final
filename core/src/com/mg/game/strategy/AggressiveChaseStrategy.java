package com.mg.game.strategy;

import com.mg.game.GameScreen;
import com.mg.game.tank.Tank;

public class AggressiveChaseStrategy implements EnemyStrategy {

    @Override
    public void update(Tank enemy, float delta, GameScreen context) {
        if (!enemy.isEnemy()) return;

        // Обновляем таймер неуязвимости
        if (enemy.isInvulnerable()) {
            enemy.reduceInvulnerability(delta);
        }

        // Получаем ближайшего игрока
        Tank target = context.getNearestAlivePlayer(enemy);
        if (target == null) return;

        // Если цель в прямой видимости — высокая вероятность выстрела
        if (enemy.isInLineOfSight(target)) {
            if (Math.random() < 0.85) { // 90% шанс
                enemy.shoot();
            }
        } else {
            // Если просто близко (например, < 120 пикселей), небольшой шанс выстрела
            float dx = target.positionX - enemy.positionX;
            float dy = target.positionY - enemy.positionY;
            float distSq = dx * dx + dy * dy;

            if (distSq < 120 * 120 && Math.random() < 0.3) {
                enemy.shoot();
            }
        }
    }
}
