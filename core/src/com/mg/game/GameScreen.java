package com.mg.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.mg.game.assets.Assets;
import com.mg.game.bullet.Bullet;
import com.mg.game.bullet.BulletManager;
import com.mg.game.command.*;
import com.mg.game.explosion.Explosion;
import com.mg.game.explosion.ExplosionFactory;
import com.mg.game.level.LevelCompleteScreen;
import com.mg.game.level.LevelIntroAnimation;
import com.mg.game.map.MapLoader;
import com.mg.game.map.MapTile;
import com.mg.game.observer.GameObserver;
import com.mg.game.strategy.AggressiveChaseStrategy;
import com.mg.game.strategy.BaseAttackStrategy;
import com.mg.game.strategy.EnemyStrategy;
import com.mg.game.strategy.WanderStrategy;
import com.mg.game.tank.Tank;
import com.mg.game.tank.factory.EnemyTankFactory;
import com.mg.game.tank.factory.PlayerTankFactory;
import com.mg.game.utils.TextureUtils;

public class GameScreen implements Screen, GameObserver {
    private int playerCount;
    private gdxGame game;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private SpriteBatch textBatch; // Separate SpriteBatch for text
    private float stateTime;
    private Tank player1;
    private Tank player2;
    private ArrayList<Bullet> bullets;
    private ArrayList<Tank> enemies;
    private ArrayList<Explosion> explosions; // Added for explosion animation
    private LevelIntroAnimation levelIntro;
    private EnemyManager enemyManager;
    private boolean isLevelIntroPlaying = true;
    private int totalKilledEnemies;
    private InputHandler inputHandler;

    // Variables for controlling movement speed
    private float moveTimer = 0f;
    private static final float MOVE_DELAY = 0.003f;


    // Variables for tracking game state
    private int score = 0;
    private boolean gameOver = false;
    private BitmapFont font;
    private BitmapFont largeFont;
    private Sound explosionSound;
    private Sound hitSound;

    // Variable for tracking pause state
    private boolean isPaused = false;

    // Variables for the map
    private MapLoader mapLoader;
    private static final float TILE_SCALE = 0.87f;
    private static final float BASE_TILE_SHIFT = MapLoader.TILE_SIZE / TILE_SCALE;

    // Variables for tracking level completion
    private boolean levelComplete = false;
    private float levelCompleteTimer = 0f;
    private static final float LEVEL_COMPLETE_DELAY = 2.0f; // Delay before showing score screen

    // Current level
    private int currentLevel;

    // Add these fields to the GameScreen class
    private int baseX = 320; // X coordinate of the base (adjust as needed)
    private int baseY = 440; // Y coordinate of the base (adjust as needed)

    // Add debug information
    private boolean debugMode = false;

    private int player1Score = 0;
    private int player2Score = 0;

    private int[] player1PointsBreakdown = new int[4]; // 4 types of tanks
    private int[] player2PointsBreakdown = new int[4];
    private BulletManager bulletManager;

    // Fixed enemy spawn points
    private final int[][] SPAWN_POINTS = {
            {80, 40},   // Top-left corner
            {240, 40},  // Top-center
            {400, 40}   // Top-right corner
    };

    // Конструкторы и публичные геттеры для доступа из GameRenderer
    public GameScreen(gdxGame game, int playerCount) {
        this(game, playerCount, 1); // Start with level 1 by default
    }

    public GameScreen(gdxGame game, int playerCount, int level) {
        this.playerCount = playerCount;
        this.game = game;
        game.addObserver(this);
        this.currentLevel = level;
        camera = new OrthographicCamera();
        camera.setToOrtho(true, 640, 480);
        batch = new SpriteBatch();
        textBatch = new SpriteBatch();
        stateTime = 0f;
        bullets = new ArrayList<>();
        enemies = new ArrayList<>();
        explosions = new ArrayList<>();
        totalKilledEnemies = 0;
        font = new BitmapFont(true);
        largeFont = new BitmapFont(false);
        largeFont.getData().setScale(5f);

        Assets.loadLevel(currentLevel);
        Assets.loadCurtainTextures();
        Assets.loadTankAnimations("yellow", 1);
        Assets.loadTankAnimations("green", 1);
        Assets.loadTankAnimations("red", 1);
        Assets.loadScoreScreenTextures();

        try {
            explosionSound = Gdx.audio.newSound(Gdx.files.internal("sounds/explosion.mp3"));
        } catch (Exception e) {
            Gdx.app.error("GameScreen", "Error loading explosion sound: " + e.getMessage());
            explosionSound = null;
        }
        bulletManager = new BulletManager(bullets, explosions, this, explosionSound);

        try {
            hitSound = Gdx.audio.newSound(Gdx.files.internal("sounds/hit.mp3"));
        } catch (Exception e) {
            Gdx.app.error("GameScreen", "Error loading hit sound: " + e.getMessage());
            hitSound = null;
        }

        PlayerTankFactory player1Factory = new PlayerTankFactory("yellow", 1, this);
        player1 = player1Factory.create();
        player1.positionX = 152;
        player1.positionY = 450;

        if (playerCount == 2) {
            PlayerTankFactory player2Factory = new PlayerTankFactory("green", 1, this);
            player2 = player2Factory.create();
            player2.positionX = 299;
            player2.positionY = 450;
        }

        mapLoader = new MapLoader();
        enemyManager = new EnemyManager(this, enemies);

        inputHandler = new InputHandler(player1, player2, bullets, playerCount, this);
        levelIntro = new LevelIntroAnimation(currentLevel);
        Assets.loadExplosionAnimation();
    }

    // Публичные геттеры для GameRenderer
    public int getPlayerCount() {
        return playerCount;
    }

    public float getStateTime() {
        return stateTime;
    }

    public Tank getPlayer1() {
        return player1;
    }

    public Tank getPlayer2() {
        return player2;
    }

    public ArrayList<Bullet> getBullets() {
        return bullets;
    }

    public ArrayList<Tank> getEnemies() {
        return enemies;
    }

    public ArrayList<Explosion> getExplosions() {
        return explosions;
    }

    public boolean isLevelIntroPlaying() {
        return isLevelIntroPlaying;
    }

    public LevelIntroAnimation getLevelIntro() {
        return levelIntro;
    }

    public int getTotalKilledEnemies() {
        return totalKilledEnemies;
    }

    public int getScore() {
        return score;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public static float getTileScale() {
        return TILE_SCALE;
    }

    public static float getBaseTileShift() {
        return BASE_TILE_SHIFT;
    }

    public com.badlogic.gdx.utils.Array<MapTile> getMapTiles() {
        return mapLoader.tiles;
    }

    public void addPlayer1Score(int points) {
        player1Score += points;
        player1PointsBreakdown[0] += 1;
        Gdx.app.log("ScoreDebug", "P1 Score: " + player1Score);
    }

    public void addPlayer2Score(int points) {
        player2Score += points;
        player2PointsBreakdown[0] += 1;
        Gdx.app.log("ScoreDebug", "P2 Score: " + player2Score);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(192f / 255, 192f / 255, 192f / 255, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        camera.update();
        stateTime += Gdx.graphics.getDeltaTime();

        if (game.isGameOver()) {
            gameOver = true;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.P) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            isPaused = !isPaused;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
            debugMode = !debugMode;
        }

        checkGameOver();
        checkLevelComplete();

        if (levelComplete) {
            levelCompleteTimer += delta;
            if (levelCompleteTimer >= LEVEL_COMPLETE_DELAY) {
                int[] tankCounts = new int[4];
                for (int i = 0; i < 4; i++) {
                    tankCounts[i] = (player1PointsBreakdown[i] + player2PointsBreakdown[i]) / 100;
                }
                game.setScreen(new LevelCompleteScreen(game, currentLevel, 20000, player1Score, player2Score,
                        player1PointsBreakdown, player2PointsBreakdown, tankCounts, playerCount == 2));
                dispose();
                return;
            }
        }

        if (isLevelIntroPlaying) {
            levelIntro.update(delta);
            if (levelIntro.isFinished()) {
                isLevelIntroPlaying = false;
            }
            batch.begin();
            batch.draw(Assets.levelBack, 0, 0, 480, 480);
            int offsetX = -17, offsetY = -17;
            for (MapTile tile : mapLoader.tiles) {
                float scaledSize = MapLoader.TILE_SIZE / TILE_SCALE;
                float drawX = tile.x * scaledSize + offsetX;
                float drawY = tile.y * scaledSize + offsetY;
                batch.draw(tile.region, drawX, drawY, scaledSize, scaledSize);
            }
            levelIntro.render(batch);
            batch.end();
            return;
        }

        if (!gameOver && !isPaused && !levelComplete) {
            moveTimer += delta;
            if (player1 != null) player1.update(delta);
            if (player2 != null) player2.update(delta);
            for (Tank enemy : enemies) {
                if (enemy != null && enemy.isAlive()) {
                    enemy.update(delta);
                }
            }
            enemyManager.update(delta);
            bulletManager.update(delta);
            updateExplosions(delta);
            if (moveTimer >= MOVE_DELAY) {
                inputHandler.handleInput(delta);
                moveTimer = 0;
            }
        } else if (gameOver) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                game.setScreen(new GameScreen(game, playerCount));
                dispose();
                return;
            }
        }

        // Rendering code remains largely the same, except for debug info
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(Assets.levelBack, 0, 0, 480, 480);
        batch.setColor(0, 0, 0, 0.4f);
        batch.draw(Assets.pixel, 480, 0, 160, 480);
        batch.setColor(Color.WHITE);

        int offsetX = -17, offsetY = -17;
        float scaled = MapLoader.TILE_SIZE / TILE_SCALE;
        float baseOffsetX = -BASE_TILE_SHIFT;
        float baseOffsetY = -BASE_TILE_SHIFT;
        boolean baseDrawn = false;
        for (MapTile tile : mapLoader.tiles) {
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

        if (player1 != null && player1.isAlive()) {
            TextureRegion frame1 = player1.getCurrentFrame();
            if (frame1 != null) {
                if (!player1.isInvulnerable() || (int) (stateTime * 10) % 2 == 0) {
                    batch.draw(frame1, player1.positionX, player1.positionY, 26 / TILE_SCALE, 26 / TILE_SCALE);
                }
            }
        }

        if (player2 != null && player2.isAlive()) {
            TextureRegion frame2 = player2.getCurrentFrame();
            if (frame2 != null) {
                if (!player2.isInvulnerable() || (int) (stateTime * 10) % 2 == 0) {
                    batch.draw(frame2, player2.positionX, player2.positionY, 26 / TILE_SCALE, 26 / TILE_SCALE);
                }
            }
        }

        for (Tank enemy : enemies) {
            if (enemy != null && enemy.isAlive()) {
                TextureRegion enemyFrame = enemy.getCurrentFrame();
                if (enemyFrame != null) {
                    batch.draw(enemyFrame, enemy.positionX, enemy.positionY, 26 / TILE_SCALE, 26 / TILE_SCALE);
                }
            }
        }

        for (Bullet bullet : bullets) {
            if (bullet.isActive() && bullet.getTexture() != null) {
                if (bullet.getPositionX() >= 0 && bullet.getPositionX() < 480 &&
                        bullet.getPositionY() >= 0 && bullet.getPositionY() < 480) {
                    batch.draw(bullet.getTexture(), bullet.getPositionX(), bullet.getPositionY(), 4, 4);
                }
            }
        }

        for (Explosion explosion : explosions) {
            if (!explosion.isFinished()) {
                TextureRegion frame = explosion.getCurrentFrame();
                if (frame != null) {
                    batch.draw(frame, explosion.getPositionX(), explosion.getPositionY(), 32, 32);
                }
            }
        }

        font.setColor(Color.BLACK);
        font.draw(batch, "Score: " + score, 500, 50);
        font.draw(batch, "Enemies: " + (10 - totalKilledEnemies) + "/10", 500, 70);
        if (Assets.enemyIcon != null) {
            int totalEnemies = 10;
            int iconsPerColumn = 5;
            int iconSpacing = 20;
            int baseXIcon = 500;
            int baseYIcon = 90;
            for (int i = 0; i < totalEnemies; i++) {
                if (i < totalKilledEnemies) continue;
                int col = i / iconsPerColumn;
                int row = i % iconsPerColumn;
                batch.draw(Assets.enemyIcon, baseXIcon + col * iconSpacing, baseYIcon + row * iconSpacing, 16, 16);
            }
        }

        if (Assets.healthIcon != null && player1 != null) {
            font.draw(batch, "P1: Health", 500, 220);
            for (int i = 0; i < player1.getHealth(); i++) {
                batch.draw(Assets.healthIcon, 500 + i * 20, 240, 16, 16);
            }
        }

        if (Assets.healthIcon != null && player2 != null) {
            font.draw(batch, "P2: Health", 500, 270);
            for (int i = 0; i < player2.getHealth(); i++) {
                batch.draw(Assets.healthIcon, 500 + i * 20, 290, 16, 16);
            }
        }

        if (debugMode) {
            for (int i = 0; i < enemies.size() && i < enemyManager.getEnemyMovementInfos().size(); i++) {
                if (enemies.get(i).isAlive()) {
                    EnemyManager.EnemyMovementInfo info = enemyManager.getEnemyMovementInfos().get(i);
                    String dirStr;
                    switch (info.direction) {
                        case FORWARD:
                            dirStr = "UP";
                            break;
                        case BACKWARD:
                            dirStr = "DOWN";
                            break;
                        case LEFT:
                            dirStr = "LEFT";
                            break;
                        case RIGHT:
                            dirStr = "RIGHT";
                            break;
                        default:
                            dirStr = "?"; // Optional: handle unexpected cases
                            break;
                    }
                    // Debug drawing can be added if needed
                }
            }
        }

        batch.end();

        if (gameOver || isPaused) {
            textBatch.begin();
            Matrix4 normalMatrix = new Matrix4();
            normalMatrix.setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            textBatch.setProjectionMatrix(normalMatrix);

            if (gameOver) {
                if (Assets.gameOverTexture != null) {
                    textBatch.draw(Assets.gameOverTexture,
                            Gdx.graphics.getWidth() / 2 - Assets.gameOverTexture.getWidth() / 2,
                            Gdx.graphics.getHeight() / 2 - Assets.gameOverTexture.getHeight() / 2);
                } else {
                    largeFont.setColor(Color.RED);
                    GlyphLayout gameOverLayout = new GlyphLayout(largeFont, "GAME OVER");
                    largeFont.draw(textBatch, "GAME OVER",
                            Gdx.graphics.getWidth() / 2 - gameOverLayout.width / 2,
                            Gdx.graphics.getHeight() / 2 + gameOverLayout.height / 2);

                    GlyphLayout restartLayout = new GlyphLayout(largeFont, "Press ENTER to restart");
                    largeFont.setColor(Color.WHITE);
                    largeFont.draw(textBatch, "Press ENTER to restart",
                            Gdx.graphics.getWidth() / 2 - restartLayout.width / 2,
                            Gdx.graphics.getHeight() / 2 + gameOverLayout.height / 2 + 40);
                }
            } else if (isPaused) {
                if (Assets.pauseTexture != null) {
                    textBatch.draw(Assets.pauseTexture,
                            Gdx.graphics.getWidth() / 2 - Assets.pauseTexture.getWidth() / 2,
                            Gdx.graphics.getHeight() / 2 - Assets.pauseTexture.getHeight() / 2);
                } else {
                    largeFont.setColor(Color.YELLOW);
                    GlyphLayout pauseLayout = new GlyphLayout(largeFont, "PAUSE");
                    largeFont.draw(textBatch, "PAUSE",
                            Gdx.graphics.getWidth() / 2 - pauseLayout.width / 2,
                            Gdx.graphics.getHeight() / 2 + pauseLayout.height / 2);

                    GlyphLayout continueLayout = new GlyphLayout(largeFont, "Press P or ESC to continue");
                    largeFont.setColor(Color.WHITE);
                    largeFont.draw(textBatch, "Press P or ESC to continue",
                            Gdx.graphics.getWidth() / 2 - continueLayout.width / 2,
                            Gdx.graphics.getHeight() / 2 + pauseLayout.height / 2 + 40);
                }
            }
            textBatch.end();
        }

        if (debugMode) {
            renderDebugInfo();
        }
    }

    private void renderDebugInfo() {
        if (!debugMode) return;

        textBatch.begin();

        Matrix4 normalMatrix = new Matrix4();
        normalMatrix.setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        textBatch.setProjectionMatrix(normalMatrix);

        font.setColor(Color.WHITE);

        // Draw debug info about UI assets
        TextureUtils.drawDebugInfo(textBatch, font, 10, 100);

        // Draw player info
        font.draw(textBatch, "Player 1: " + (player1 != null ? "alive=" + player1.isAlive() : "null"), 10, 300);
        font.draw(textBatch, "Player 2: " + (player2 != null ? "alive=" + player2.isAlive() : "null"), 10, 320);
        font.draw(textBatch, "Player count: " + playerCount, 10, 340);
        font.draw(textBatch, "Two player mode: " + (playerCount == 2), 10, 360);

        textBatch.end();
    }

    private void checkGameOver() {
        boolean playersAlive = (player1 != null && player1.isAlive()) || (player2 != null && player2.isAlive());

        boolean enemiesAlive = false;
        for (Tank enemy : enemies) {
            if (enemy != null && enemy.isAlive()) {
                enemiesAlive = true;
                break;
            }
        }

        // Game ends if all players are dead
        if (!playersAlive) {
            gameOver = true;
        }
    }

    public static boolean isKeyPressed(int keycode) {
        return Gdx.input.isKeyPressed(keycode);
    }

    // Обновлённый метод canMoveTo с учётом масштабирования
    public static boolean canMoveTo(Tank tank, int newX, int newY, GameScreen screen) {
        if (tank == null || !tank.isAlive()) {
            Gdx.app.log("canMoveTo", "Cannot move: tank is null or dead");
            return false;
        }
        if (newX < 0 || newX > 454 - 9 || newY < 0 || newY > 454) {
            Gdx.app.log("canMoveTo", "Cannot move: out of bounds at x=" + newX + ", y=" + newY);
            return false;
        }
        if (screen.checkCollisionWithTank(tank, newX, newY)) {
            Gdx.app.log("canMoveTo", "Cannot move: collision with tank at x=" + newX + ", y=" + newY);
            return false;
        }
        if (screen.checkCollisionWithEnemy(tank, newX, newY)) {
            Gdx.app.log("canMoveTo", "Cannot move: collision with enemy at x=" + newX + ", y=" + newY);
            return false;
        }
        if (screen.checkCollisionWithMap(newX, newY, tank)) {
            Gdx.app.log("canMoveTo", "Cannot move: collision with map at x=" + newX + ", y=" + newY);
            return false;
        }
        Gdx.app.log("canMoveTo", "Can move to x=" + newX + ", y=" + newY);
        return true;
    }

    private void checkLevelComplete() {
        boolean enemiesDefeated = true;
        for (Tank enemy : enemies) {
            if (enemy != null && enemy.isAlive()) {
                enemiesDefeated = false;
                break;
            }
        }
        if (enemiesDefeated && !levelComplete && !gameOver) {
            levelComplete = true;
            levelCompleteTimer = 0f;
        }
    }



    public boolean checkCollisionWithPlayer(Tank enemy, int newX, int newY) {
        if (enemy == null) return false;

        int oldX = enemy.positionX;
        int oldY = enemy.positionY;

        enemy.positionX = newX;
        enemy.positionY = newY;

        boolean collides =
                (player1 != null && player1.isAlive() && enemy.collidesWith(player1)) ||
                        (player2 != null && player2.isAlive() && enemy.collidesWith(player2));

        enemy.positionX = oldX;
        enemy.positionY = oldY;

        return collides;
    }

    // Returns the nearest alive player for strategy use
    public Tank getNearestAlivePlayer(Tank enemy) {
        Tank nearest = null;
        float minDistSq = Float.MAX_VALUE;

        if (player1 != null && player1.isAlive()) {
            float dx = player1.positionX - enemy.positionX;
            float dy = player1.positionY - enemy.positionY;
            float distSq = dx * dx + dy * dy;
            if (distSq < minDistSq) {
                minDistSq = distSq;
                nearest = player1;
            }
        }

        if (player2 != null && player2.isAlive()) {
            float dx = player2.positionX - enemy.positionX;
            float dy = player2.positionY - enemy.positionY;
            float distSq = dx * dx + dy * dy;
            if (distSq < minDistSq) {
                minDistSq = distSq;
                nearest = player2;
            }
        }

        return nearest;
    }

    private void updateExplosions(float delta) {
        Iterator<Explosion> iterator = explosions.iterator();
        while (iterator.hasNext()) {
            Explosion explosion = iterator.next();
            explosion.update(delta);
            if (explosion.isFinished()) {
                iterator.remove();
                Gdx.app.log("Explosion", "Explosion removed from list");
            }
        }
    }

    public void onEnemyKilled() {
        totalKilledEnemies++; // Increment counter
        Gdx.app.log("GameScreen", "Enemy killed! Total killed: " + totalKilledEnemies);
    }

    public boolean checkCollisionWithTank(Tank tank, int newX, int newY) {
        if (tank == null) return false;

        int oldX = tank.positionX;
        int oldY = tank.positionY;

        tank.positionX = newX;
        tank.positionY = newY;

        boolean collides = false;

        // Check collision with the first player, only if alive
        if (tank == player2 && player1 != null && player1.isAlive() && tank.collidesWith(player1)) {
            collides = true;
        }
        // Check collision with the second player, only if alive
        else if (tank == player1 && player2 != null && player2.isAlive() && tank.collidesWith(player2)) {
            collides = true;
        }

        tank.positionX = oldX;
        tank.positionY = oldY;

        return collides;
    }

    public boolean checkCollisionWithEnemy(Tank tank, int newX, int newY) {
        if (tank == null) return false;

        int oldX = tank.positionX;
        int oldY = tank.positionY;

        tank.positionX = newX;
        tank.positionY = newY;

        boolean collides = false;
        for (Tank enemy : enemies) {
            if (enemy != null && enemy.isAlive() && tank != enemy && tank.collidesWith(enemy)) {
                collides = true;
                break;
            }
        }

        tank.positionX = oldX;
        tank.positionY = oldY;

        return collides;
    }

    public boolean checkCollisionWithMap(int newX, int newY, Tank tank) {
        Rectangle tankRect = new Rectangle(newX, newY, 26 / TILE_SCALE, 26 / TILE_SCALE);
        for (MapTile tile : mapLoader.tiles) {
            if (tile.isSolid) {
                Rectangle tileRect = tile.getBounds(MapLoader.TILE_SIZE, TILE_SCALE, -17, -17);
                if (tankRect.overlaps(tileRect)) {
                    return true;
                }
            }
        }
        return false;
    }

    private int[] findFreeSpawnPoint(int startX, int startY, int step) {
        for (int y = startY; y < 480; y += step) {
            for (int x = startX; x < 440; x += step) {
                Rectangle rect = new Rectangle(x, y, 26 / TILE_SCALE, 26 / TILE_SCALE);
                boolean blocked = false;

                // 1. Check map
                for (MapTile tile : mapLoader.tiles) {
                    if (tile.isSolid) {
                        Rectangle tileRect = tile.getBounds(MapLoader.TILE_SIZE, TILE_SCALE, -17, -17);
                        if (rect.overlaps(tileRect)) {
                            blocked = true;
                            break;
                        }
                    }
                }

                // 2. Check player 1
                if (!blocked && player1 != null) {
                    Rectangle r1 = new Rectangle(player1.positionX, player1.positionY, 26 / TILE_SCALE, 26 / TILE_SCALE);
                    if (rect.overlaps(r1)) {
                        blocked = true;
                    }
                }

                // 3. Check player 2
                if (!blocked && player2 != null) {
                    Rectangle r2 = new Rectangle(player2.positionX, player2.positionY, 26 / TILE_SCALE, 26 / TILE_SCALE);
                    if (rect.overlaps(r2)) {
                        blocked = true;
                    }
                }

                // 4. Check other enemies
                if (!blocked) {
                    for (Tank enemy : enemies) {
                        if (enemy != null) {
                            Rectangle r = new Rectangle(enemy.positionX, enemy.positionY, 26 / TILE_SCALE, 26 / TILE_SCALE);
                            if (rect.overlaps(r)) {
                                blocked = true;
                                break;
                            }
                        }
                    }
                }

                if (!blocked) {
                    return new int[]{x, y};
                }
            }
        }
        return new int[]{50, 50};
    }

    private boolean checkCollisionWithEnemy(Tank tank, float newX, float newY) {
        if (tank == null) return false;

        float oldX = tank.positionX;
        float oldY = tank.positionY;

        tank.positionX = (int) newX;
        tank.positionY = (int) newY;

        boolean collides = false;
        for (Tank otherEnemy : enemies) {
            if (otherEnemy != null && tank != otherEnemy && otherEnemy.isAlive() && tank.collidesWith(otherEnemy)) {
                collides = true;
                break;
            }
        }

        tank.positionX = (int) oldX;
        tank.positionY = (int) oldY;

        return collides;
    }
    public boolean isSpawnPointClear(int x, int y) {
        if (checkCollisionWithMap(x, y, null)) {
            return false;
        }

        Rectangle spawnRect = new Rectangle(x, y, 26 / TILE_SCALE, 26 / TILE_SCALE);
        if (player1 != null && player1.isAlive()) {
            Rectangle playerRect = new Rectangle(player1.positionX, player1.positionY, 26 / TILE_SCALE, 26 / TILE_SCALE);
            if (spawnRect.overlaps(playerRect)) {
                return false;
            }
        }

        if (player2 != null && player2.isAlive()) {
            Rectangle playerRect = new Rectangle(player2.positionX, player2.positionY, 26 / TILE_SCALE, 26 / TILE_SCALE);
            if (spawnRect.overlaps(playerRect)) {
                return false;
            }
        }

        for (Tank enemy : enemies) {
            if (enemy != null && enemy.isAlive()) {
                Rectangle enemyRect = new Rectangle(enemy.positionX, enemy.positionY, 26 / TILE_SCALE, 26 / TILE_SCALE);
                if (spawnRect.overlaps(enemyRect)) {
                    return false;
                }
            }
        }

        return true;
    }

    public int[] findNearestFreeSpot(int startX, int startY) {
        for (int radius = 5; radius <= 50; radius += 5) {
            for (int offsetX = -radius; offsetX <= radius; offsetX += 5) {
                for (int offsetY = -radius; offsetY <= radius; offsetY += 5) {
                    int x = startX + offsetX;
                    int y = startY + offsetY;
                    if (x >= 0 && x <= 454 && y >= 0 && y <= 454 && isSpawnPointClear(x, y)) {
                        return new int[]{x, y};
                    }
                }
            }
        }
        Gdx.app.error("GameScreen", "Could not find free spawn point near " + startX + ", " + startY);
        return new int[]{startX, startY};
    }

    private void notifyEnemyKilled(Tank enemy) {
        onEnemyKilled();
    }

    @Override
    public void onBaseDestroyed() {
        gameOver = true;
        Gdx.app.log("GameScreen", "Observer: Base destroyed!");
    }

    public int getBaseX() {
        return baseX;
    }

    // Get the Y coordinate of the base
    public int getBaseY() {
        return baseY;
    }

    // Get the base tile (top-left corner of the base)
    public MapTile getBaseTile() {
        for (MapTile tile : mapLoader.tiles) {
            if (tile.isBase && tile.x % 2 == 0 && tile.y % 2 == 0) {
                return tile;
            }
        }
        return null;
    }

    @Override
    public void show() {
        // Method called when the screen is shown
    }

    @Override
    public void resize(int width, int height) {
        // Method called when the window is resized
    }

    @Override
    public void pause() {
        isPaused = true;
    }

    @Override
    public void resume() {
        // Can keep the game paused when the window is restored
    }

    @Override
    public void hide() {
        // Method called when the screen is hidden
    }

    @Override
    public void dispose() {
        batch.dispose();
        textBatch.dispose();
        for (Bullet bullet : bullets) {
            if (bullet != null) {
                bullet.dispose();
            }
        }
        font.dispose();
        largeFont.dispose();
        if (explosionSound != null) explosionSound.dispose();
        if (hitSound != null) hitSound.dispose();
        if (levelIntro != null) levelIntro.dispose();
        game.removeObserver(this);
    }
    public int getOffsetX() {
        return -17;
    }

    public int getOffsetY() {
        return -17;
    }
    public int getTileSize() {
        return MapLoader.TILE_SIZE;
    }
}