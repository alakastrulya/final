package com.mg.game.bullet;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.mg.game.GameScreen;
import com.mg.game.explosion.Explosion;
import com.mg.game.explosion.ExplosionFactory;
import com.mg.game.map.MapTile;
import com.mg.game.tank.Tank;

import java.util.ArrayList;
import java.util.Iterator;

public class BulletManager {

    private final ArrayList<Bullet> bullets;
    private final ArrayList<Explosion> explosions;
    private final GameScreen screen;
    private final Sound explosionSound;

    public BulletManager(ArrayList<Bullet> bullets, ArrayList<Explosion> explosions, GameScreen screen, Sound explosionSound) {
        this.bullets = bullets;
        this.explosions = explosions;
        this.screen = screen;
        this.explosionSound = explosionSound;
    }

    public void update(float delta) {
        Iterator<Bullet> iterator = bullets.iterator();
        while (iterator.hasNext()) {
            Bullet bullet = iterator.next();
            if (bullet == null) {
                iterator.remove();
                continue;
            }

            bullet.update(delta);

            // Remove bullet if it goes out of bounds
            if (!bullet.isActive()) {
                bullet.dispose();
                iterator.remove();
                continue;
            }

            // Collisions
            if (checkCollision(bullet)) {
                bullet.deactivate();
                bullet.dispose();
                iterator.remove();
            }
        }
    }

    private boolean checkCollision(Bullet bullet) {
        // Collision with map tiles
        for (MapTile tile : screen.getMapTiles()) {
            if (tile.isSolid && bullet.getBounds().overlaps(tile.getBounds(screen.getTileSize(), screen.getTileScale(), screen.getOffsetX(), screen.getOffsetY()))) {
                if (tile.isDestructible) tile.takeHit();
                explosions.add(new ExplosionFactory(bullet.getPositionX(), bullet.getPositionY()).create());
                if (explosionSound != null) explosionSound.play();
                return true;
            }
        }

        // Collision with enemies (for player bullets)
        if (!bullet.isFromEnemy()) {
            for (Tank enemy : screen.getEnemies()) {
                if (enemy != null && enemy.isAlive() && bullet.getBounds().overlaps(enemy.getBounds())) {
                    boolean wasKilled = enemy.takeDamage();
                    if (wasKilled) {
                        screen.onEnemyKilled();
                        explosions.add(new ExplosionFactory(enemy.positionX, enemy.positionY).create());
                        if (explosionSound != null) explosionSound.play();
                        String color = bullet.getColor();
                        if ("yellow".equalsIgnoreCase(color)) {
                            screen.addPlayer1Score(100);
                        } else if ("green".equalsIgnoreCase(color)) {
                            screen.addPlayer2Score(100);
                        }
                    }
                    return true;
                }
            }

            //  NEW: Allow friendly fire between players
            Tank player1 = screen.getPlayer1();
            Tank player2 = screen.getPlayer2();
            if (player1 != null && player1.isAlive() && !"yellow".equalsIgnoreCase(bullet.getColor()) &&
                    bullet.getBounds().overlaps(player1.getBounds())) {
                boolean killed = player1.takeDamage();
                explosions.add(new ExplosionFactory(player1.positionX, player1.positionY).create());
                if (explosionSound != null) explosionSound.play();
                return true;
            }
            if (player2 != null && player2.isAlive() && !"green".equalsIgnoreCase(bullet.getColor()) &&
                    bullet.getBounds().overlaps(player2.getBounds())) {
                boolean killed = player2.takeDamage();
                explosions.add(new ExplosionFactory(player2.positionX, player2.positionY).create());
                if (explosionSound != null) explosionSound.play();
                return true;
            }
        }

        // Enemy bullets damaging players
        if (bullet.isFromEnemy()) {
            Tank player1 = screen.getPlayer1();
            Tank player2 = screen.getPlayer2();

            if (player1 != null && player1.isAlive() && bullet.getBounds().overlaps(player1.getBounds())) {
                boolean killed = player1.takeDamage();
                explosions.add(new ExplosionFactory(player1.positionX, player1.positionY).create());
                if (explosionSound != null) explosionSound.play();
                return true;
            }
            if (player2 != null && player2.isAlive() && bullet.getBounds().overlaps(player2.getBounds())) {
                boolean killed = player2.takeDamage();
                explosions.add(new ExplosionFactory(player2.positionX, player2.positionY).create());
                if (explosionSound != null) explosionSound.play();
                return true;
            }
        }

        return false;
    }

    public Sound getExplosionSound() {
        return explosionSound;
    }

}