package com.mg.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.mg.game.assets.Assets;
import com.mg.game.bullet.Bullet;
import com.mg.game.explosion.Explosion;
import com.mg.game.map.MapLoader;
import com.mg.game.map.MapTile;
import com.mg.game.tank.Tank;

public class GameRenderer {
    private GameScreen gameScreen;
    private OrthographicCamera camera;
    private SpriteBatch batch;

    public GameRenderer(GameScreen gameScreen) {
        this.gameScreen = gameScreen;
        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(true, 640, 480); // Камера настроена под размеры экрана
        this.batch = new SpriteBatch();
    }

    public void render(float delta) {
        // Очистка экрана
        Gdx.gl.glClearColor(192f / 255, 192f / 255, 192f / 255, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();

        // Если проигрывается интро-анимация уровня
        if (gameScreen.isLevelIntroPlaying()) {
            // Рендерим фон уровня
            batch.draw(Assets.levelBack, 0, 0, 480, 480);

            // Рендерим карту
            renderMap();

            // Рендерим интро-анимацию
            gameScreen.getLevelIntro().render(batch);
        } else {
            // Обычный игровой цикл
            // 1. Рендерим фон
            batch.draw(Assets.levelBack, 0, 0, 480, 480);

            // 2. Рендерим боковую панель (sidebar)
            batch.setColor(0, 0, 0, 0.4f); // Черный с прозрачностью 40%
            batch.draw(Assets.pixel, 480, 0, 160, 480); // Sidebar: x=480, ширина 160
            batch.setColor(1, 1, 1, 1); // Сбрасываем цвет

            // 3. Рендерим карту
            renderMap();

            // 4. Рендерим танки
            renderTanks();

            // 5. Рендерим пули
            renderBullets();

            // 6. Рендерим взрывы
            renderExplosions();

            // 7. Рендерим UI (счёт, здоровье, иконки врагов)
            renderUI();
        }

        batch.end();
    }

    private void renderMap() {
        float tileScale = GameScreen.getTileScale();
        float baseTileShift = GameScreen.getBaseTileShift();
        int offsetX = -17; // Смещение карты
        int offsetY = -17;

        float scaled = MapLoader.TILE_SIZE / tileScale;
        float baseOffsetX = -baseTileShift;
        float baseOffsetY = -baseTileShift;

        boolean baseDrawn = false;
        for (MapTile tile : gameScreen.getMapTiles()) {
            if (tile.isBase) {
                // Рендерим только верхний левый тайл базы (размер базы 2x2)
                if (!baseDrawn && tile.x % 2 == 0 && tile.y % 2 == 0) {
                    float x = tile.x * scaled + offsetX + baseOffsetX;
                    float y = tile.y * scaled + offsetY + baseOffsetY;
                    batch.draw(tile.region, x, y, scaled * 2, scaled * 2);
                    baseDrawn = true;
                }
            } else if (tile.isSolid) {
                float x = tile.x * scaled + offsetX;
                float y = tile.y * scaled + offsetY;
                batch.draw(tile.region, x, y, scaled, scaled);
            }
        }
    }

    private void renderTanks() {
        Tank player1 = gameScreen.getPlayer1();
        Tank player2 = gameScreen.getPlayer2();
        float stateTime = gameScreen.getStateTime();
        float scaledSize = 26 / GameScreen.getTileScale(); // Масштабированный размер танка

        // Рендерим первого игрока
        if (player1 != null && player1.isAlive()) {
            TextureRegion frame1 = player1.getCurrentFrame();
            if (frame1 != null) {
                // Если танк неуязвим, мигаем (рендерим через кадр)
                if (!player1.isInvulnerable() || (int) (stateTime * 10) % 2 == 0) {
                    batch.draw(frame1, player1.positionX, player1.positionY, scaledSize, scaledSize);
                }
            } else {
                Gdx.app.log("GameRenderer", "Player1 frame is null");
            }
        }

        // Рендерим второго игрока
        if (player2 != null && player2.isAlive()) {
            TextureRegion frame2 = player2.getCurrentFrame();
            if (frame2 != null) {
                if (!player2.isInvulnerable() || (int) (stateTime * 10) % 2 == 0) {
                    batch.draw(frame2, player2.positionX, player2.positionY, scaledSize, scaledSize);
                }
            } else {
                Gdx.app.log("GameRenderer", "Player2 frame is null");
            }
        }

        // Рендерим врагов
        for (Tank enemy : gameScreen.getEnemies()) {
            if (enemy != null && enemy.isAlive()) {
                TextureRegion enemyFrame = enemy.getCurrentFrame();
                if (enemyFrame != null) {
                    batch.draw(enemyFrame, enemy.positionX, enemy.positionY, scaledSize, scaledSize);
                } else {
                    Gdx.app.log("GameRenderer", "Enemy frame is null at x=" + enemy.positionX + ", y=" + enemy.positionY);
                }
            }
        }
    }

    private void renderBullets() {
        for (Bullet bullet : gameScreen.getBullets()) {
            if (bullet != null && bullet.isActive()) {
                TextureRegion bulletFrame = bullet.getTexture();
                if (bulletFrame != null) {
                    // Масштабированный размер пули
                    float scaledBulletSize = 4 / GameScreen.getTileScale();
                    // Проверяем, что пуля в пределах карты
                    if (bullet.getPositionX() >= 0 && bullet.getPositionX() < 480 &&
                            bullet.getPositionY() >= 0 && bullet.getPositionY() < 480) {
                        batch.draw(bulletFrame, bullet.getPositionX(), bullet.getPositionY(), scaledBulletSize, scaledBulletSize);
                    }
                } else {
                    Gdx.app.log("GameRenderer", "Bullet texture is null for bullet at x=" + bullet.getPositionX() + ", y=" + bullet.getPositionY());
                }
            }
        }
    }

    private void renderExplosions() {
        for (Explosion explosion : gameScreen.getExplosions()) {
            if (explosion != null && !explosion.isFinished()) {
                TextureRegion frame = explosion.getCurrentFrame();
                if (frame != null) {
                    // Масштабированный размер взрыва
                    float scaledExplosionSize = 32 / GameScreen.getTileScale();
                    batch.draw(frame, explosion.getPositionX(), explosion.getPositionY(), scaledExplosionSize, scaledExplosionSize);
                } else {
                    Gdx.app.log("GameRenderer", "Explosion frame is null at x=" + explosion.getPositionX() + ", y=" + explosion.getPositionY());
                }
            }
        }
    }

    private void renderUI() {
        // Используем шрифт из Assets, если он доступен, или создаём временный
        BitmapFont font = new BitmapFont(true);


        font.setColor(0, 0, 0, 1); // Чёрный цвет

        // Рендерим счёт
        font.draw(batch, "Score: " + gameScreen.getScore(), 500, 50);

        // Рендерим панель врагов (10 иконок, снизу вверх)
        font.draw(batch, "Enemies: " + (10 - gameScreen.getTotalKilledEnemies()) + "/10", 500, 70);
        if (Assets.enemyIcon != null) {
            int totalEnemies = 10;
            int iconsPerColumn = 5;
            int iconSpacing = 20;
            int baseXIcon = 500;
            int baseYIcon = 90;

            for (int i = 0; i < totalEnemies; i++) {
                if (i < gameScreen.getTotalKilledEnemies()) continue;
                int col = i / iconsPerColumn;
                int row = i % iconsPerColumn;
                batch.draw(Assets.enemyIcon, baseXIcon + col * iconSpacing, baseYIcon + row * iconSpacing, 16, 16);
            }
        }

        // Рендерим здоровье первого игрока
        Tank player1 = gameScreen.getPlayer1();
        if (Assets.healthIcon != null && player1 != null) {
            font.draw(batch, "P1: Health", 500, 220);
            for (int i = 0; i < player1.getHealth(); i++) {
                batch.draw(Assets.healthIcon, 500 + i * 20, 240, 16, 16);
            }
        }

        // Рендерим здоровье второго игрока
        Tank player2 = gameScreen.getPlayer2();
        if (Assets.healthIcon != null && player2 != null) {
            font.draw(batch, "P2: Health", 500, 270);
            for (int i = 0; i < player2.getHealth(); i++) {
                batch.draw(Assets.healthIcon, 500 + i * 20, 290, 16, 16);
            }
        }
    }
    public void dispose() {
        batch.dispose();
    }
}