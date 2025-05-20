package com.mg.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mg.game.assets.Assets;
import com.mg.game.bullet.Bullet;
import com.mg.game.explosion.Explosion;
import com.mg.game.level.LevelIntroAnimation;
import com.mg.game.map.MapLoader;
import com.mg.game.map.MapTile;
import com.mg.game.tank.Tank;

public class GameRenderer {
    private GameScreen gameScreen;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private BitmapFont font; // Добавляем поле для шрифта

    public GameRenderer(GameScreen gameScreen) {
        this.gameScreen = gameScreen;
        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(true, 640, 480);
        this.batch = new SpriteBatch();
        this.font = new BitmapFont(true); // Инициализация шрифта
        font.setColor(0, 0, 0, 1);
    }

    public void render(float delta) {
        if (gameScreen.isGameOver()) {
            Gdx.app.log("GameRenderer", "Skipping render due to game over");
            return;
        }

        Gdx.gl.glClearColor(192f / 255, 192f / 255, 192f / 255, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();

        if (gameScreen.isLevelIntroPlaying()) {
            if (Assets.levelBack != null) {
                batch.draw(Assets.levelBack, 0, 0, 480, 480);
            } else {
                Gdx.app.log("GameRenderer", "levelBack texture is null");
            }

            renderMap();

            LevelIntroAnimation intro = gameScreen.getLevelIntro();
            if (intro != null) {
                intro.update(delta);
                intro.render(batch);
                if (intro.isFinished()) {
                    gameScreen.setLevelIntroPlaying(false);
                    Gdx.app.log("GameRenderer", "Level intro finished");
                }
            } else {
                Gdx.app.log("GameRenderer", "LevelIntroAnimation is null");
                gameScreen.setLevelIntroPlaying(false);
            }
        } else {
            if (Assets.levelBack != null) {
                batch.draw(Assets.levelBack, 0, 0, 480, 480);
            } else {
                Gdx.app.log("GameRenderer", "levelBack texture is null");
            }

            batch.setColor(0, 0, 0, 0.4f);
            batch.draw(Assets.pixel, 480, 0, 160, 480);
            batch.setColor(1, 1, 1, 1);

            renderMap();
            renderTanks();
            renderBullets();
            renderExplosions();
            renderUI();
        }

        batch.end();
    }

    private void renderMap() {
        float tileScale = GameScreen.getTileScale();
        float baseTileShift = GameScreen.getBaseTileShift();
        int offsetX = -17;
        int offsetY = -17;

        float scaled = MapLoader.TILE_SIZE / tileScale;
        float baseOffsetX = -baseTileShift;
        float baseOffsetY = -baseTileShift;

        boolean baseDrawn = false;
        for (MapTile tile : gameScreen.getMapTiles()) {
            if (tile == null || tile.region == null) {
                Gdx.app.log("GameRenderer", "MapTile or region is null");
                continue;
            }
            if (tile.isBase) {
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
        float scaledSize = 26 / GameScreen.getTileScale();

        if (player1 != null && player1.isAlive()) {
            TextureRegion frame1 = player1.getCurrentFrame();
            if (frame1 != null) {
                if (!player1.isInvulnerable() || (int) (stateTime * 10) % 2 == 0) {
                    batch.draw(frame1, player1.positionX, player1.positionY, scaledSize, scaledSize);
                }
            } else {
                Gdx.app.log("GameRenderer", "Player1 frame is null");
            }
        }

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
                    float scaledBulletSize = 4 / GameScreen.getTileScale();
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
                    float scaledExplosionSize = 32 / GameScreen.getTileScale();
                    batch.draw(frame, explosion.getPositionX(), explosion.getPositionY(), scaledExplosionSize, scaledExplosionSize);
                } else {
                    Gdx.app.log("GameRenderer", "Explosion frame is null at x=" + explosion.getPositionX() + ", y=" + explosion.getPositionY());
                }
            }
        }
    }

    private void renderUI() {
        font.draw(batch, "P1 Score: " + gameScreen.getPlayer1Score(), 500, 50);
        if (gameScreen.getPlayerCount() == 2) {
            font.draw(batch, "P2 Score: " + gameScreen.getPlayer2Score(), 500, 70);
        }
        font.draw(batch, "Enemies: " + (10 - gameScreen.getTotalKilledEnemies()) + "/10", 500, 90);

        if (Assets.enemyIcon != null) {
            int totalEnemies = 10;
            int iconsPerColumn = 5;
            int iconSpacing = 20;
            int baseXIcon = 500;
            int baseYIcon = 110;

            for (int i = 0; i < totalEnemies; i++) {
                if (i < gameScreen.getTotalKilledEnemies()) continue;
                int col = i / iconsPerColumn;
                int row = i % iconsPerColumn;
                batch.draw(Assets.enemyIcon, baseXIcon + col * iconSpacing, baseYIcon + row * iconSpacing, 16, 16);
            }
        } else {
            Gdx.app.log("GameRenderer", "enemyIcon texture is null");
        }

        Tank player1 = gameScreen.getPlayer1();
        if (Assets.healthIcon != null && player1 != null) {
            font.draw(batch, "P1: Health", 500, 220);
            for (int i = 0; i < player1.getHealth(); i++) {
                batch.draw(Assets.healthIcon, 500 + i * 20, 240, 16, 16);
            }
        } else {
            Gdx.app.log("GameRenderer", "healthIcon texture is null or player1 is null");
        }

        Tank player2 = gameScreen.getPlayer2();
        if (Assets.healthIcon != null && player2 != null) {
            font.draw(batch, "P2: Health", 500, 270);
            for (int i = 0; i < player2.getHealth(); i++) {
                batch.draw(Assets.healthIcon, 500 + i * 20, 290, 16, 16);
            }
        } else {
            Gdx.app.log("GameRenderer", "healthIcon texture is null or player2 is null");
        }
    }

    public void dispose() {
        Gdx.app.log("GameRenderer", "Disposing GameRenderer resources");
        if (batch != null) {
            batch.dispose();
            batch = null;
        }
        if (font != null) {
            font.dispose();
            font = null;
        }
    }
}