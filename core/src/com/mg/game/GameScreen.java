package com.mg.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mg.game.assets.Assets;
import com.mg.game.bullet.Bullet;
import com.mg.game.bullet.BulletManager;
import com.mg.game.explosion.Explosion;
import com.mg.game.explosion.ExplosionFactory;
import com.mg.game.level.LevelCompleteScreen;
import com.mg.game.level.LevelIntroAnimation;
import com.mg.game.manager.*;
import com.mg.game.map.MapLoader;
import com.mg.game.map.MapTile;
import com.mg.game.observer.GameContext;
import com.mg.game.observer.GameObserver;
import com.mg.game.tank.Tank;
import com.mg.game.tank.factory.PlayerTankFactory;
import com.mg.game.tank.factory.TankParams;

import java.util.ArrayList;
import java.util.Iterator;

public class GameScreen implements Screen, GameObserver {
    private int playerCount;
    private gdxGame game;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private SpriteBatch textBatch;
    private float stateTime;
    private Tank player1;
    private Tank player2;
    private ArrayList<Bullet> bullets;
    private ArrayList<Tank> enemies;
    private ArrayList<Explosion> explosions;
    private LevelIntroAnimation levelIntro;
    private EnemyManager enemyManager;
    private boolean isLevelIntroPlaying = true;
    private int totalKilledEnemies;
    private InputManager inputManager;
    private int score = 0;
    private BitmapFont font;
    private BitmapFont largeFont;
    private Sound explosionSound;
    private Sound hitSound;
    private MapLoader mapLoader;
    private static final float TILE_SCALE = 0.87f;
    private static final float BASE_TILE_SHIFT = MapLoader.TILE_SIZE / TILE_SCALE;
    private float levelCompleteTimer = 0f;
    private CollisionManager collisionManager;
    private static final float LEVEL_COMPLETE_DELAY = 2.0f;
    private int currentLevel;
    private int baseX = 320;
    private int baseY = 440;
    private boolean debugMode = false;
    private int player1Score = 0;
    private int player2Score = 0;
    private int[] player1PointsBreakdown = new int[4];
    private int[] player2PointsBreakdown = new int[4];
    private GameRenderer gameRenderer;
    private BulletManager bulletManager;
    private GameStateManager gameStateManager;

    private final int[][] SPAWN_POINTS = {{80, 40}, {240, 40}, {400, 40}};

    public GameScreen(gdxGame game, int playerCount) {
        this(game, playerCount, 1);
    }

    public GameScreen(gdxGame game, int playerCount, int level) {
        this.playerCount = playerCount;
        this.game = game;
        game.getEventPublisher().addObserver(this);
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
        gameStateManager = new GameStateManager();


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
        bulletManager = new BulletManager(bullets, explosions, this, explosionSound, new ExplosionFactory());

        try {
            hitSound = Gdx.audio.newSound(Gdx.files.internal("sounds/hit.mp3"));
        } catch (Exception e) {
            Gdx.app.error("GameScreen", "Error loading hit sound: " + e.getMessage());
            hitSound = null;
        }

        mapLoader = new MapLoader();
        for (MapTile tile : mapLoader.tiles) {
            if (tile.isBase) {
                tile.setEventPublisher(game.getEventPublisher(), currentLevel, playerCount);
            }
        }
        collisionManager = new CollisionManager(mapLoader, null, null, enemies);
        PlayerTankFactory playerFactory = new PlayerTankFactory();
        TankParams p1Params = new TankParams("yellow", 1, false, this, collisionManager);
        player1 = playerFactory.create(p1Params);
        player1.positionX = 152;
        player1.positionY = 450;
        if (!collisionManager.isSpawnPointClear(player1.positionX, player1.positionY)) {
            Gdx.app.error("GameScreen", "Player1 spawn point is blocked at x=152, y=450");
            int[] freeSpawn = collisionManager.findNearestFreeSpot(152, 450);
            player1.positionX = freeSpawn[0];
            player1.positionY = freeSpawn[1];
            Gdx.app.log("GameScreen", "Moved Player1 to free spot: x=" + freeSpawn[0] + ", y=" + freeSpawn[1]);
        }

        if (playerCount == 2) {
            TankParams p2Params = new TankParams("green", 1, false, this, collisionManager);
            player2 = playerFactory.create(p2Params);
            player2.positionX = 299;
            player2.positionY = 450;
            if (!collisionManager.isSpawnPointClear(player2.positionX, player2.positionY)) {
                Gdx.app.error("GameScreen", "Player2 spawn point is blocked at x=299, y=450");
                int[] freeSpawn = collisionManager.findNearestFreeSpot(299, 450);
                player2.positionX = freeSpawn[0];
                player2.positionY = freeSpawn[1];
                Gdx.app.log("GameScreen", "Moved Player2 to free spot: x=" + freeSpawn[0] + ", y=" + freeSpawn[1]);
            }
        }

        collisionManager = new CollisionManager(mapLoader, player1, player2, enemies);
        player1.setCollisionManager(collisionManager);
        if (player2 != null) {
            player2.setCollisionManager(collisionManager);
        }

        enemyManager = new EnemyManager(this, enemies);
        inputManager = new InputManager(this, player1, player2, bullets, playerCount);
        levelIntro = new LevelIntroAnimation(currentLevel);
        Assets.loadExplosionAnimation();

        gameRenderer = new GameRenderer(this);
    }

    public GameStateManager getGameStateManager() {
        return gameStateManager;
    }

    public CollisionManager getCollisionManager() {
        return collisionManager;
    }

    public int getPlayerCount() { return playerCount; }
    public float getStateTime() { return stateTime; }
    public Tank getPlayer1() { return player1; }
    public Tank getPlayer2() { return player2; }
    public ArrayList<Bullet> getBullets() { return bullets; }
    public ArrayList<Tank> getEnemies() { return enemies; }
    public ArrayList<Explosion> getExplosions() { return explosions; }
    public boolean isLevelIntroPlaying() { return isLevelIntroPlaying; }
    public void setLevelIntroPlaying(boolean playing) { isLevelIntroPlaying = playing; }
    public LevelIntroAnimation getLevelIntro() { return levelIntro; }
    public int getTotalKilledEnemies() { return totalKilledEnemies; }
    public int getScore() { return score; }
    public boolean isDebugMode() { return debugMode; }
    public void toggleDebugMode() { debugMode = !debugMode; }
    public static float getTileScale() { return TILE_SCALE; }
    public static float getBaseTileShift() { return BASE_TILE_SHIFT; }
    public com.badlogic.gdx.utils.Array<MapTile> getMapTiles() { return mapLoader.tiles; }
    public int getBaseX() { return baseX; }
    public int getBaseY() { return baseY; }
    public MapTile getBaseTile() {
        for (MapTile tile : mapLoader.tiles) {
            if (tile.isBase && tile.x % 2 == 0 && tile.y % 2 == 0) {
                return tile;
            }
        }
        return null;
    }
    public int getOffsetX() { return -17; }
    public int getOffsetY() { return -17; }
    public int getTileSize() { return MapLoader.TILE_SIZE; }
    public boolean isGameOver() { return gameStateManager.isGameOver(); }
    public boolean isPaused() {
        return gameStateManager.isPaused(); }
    public void togglePause() {
        gameStateManager.togglePause(); }
    public boolean isLevelComplete() { return gameStateManager.isLevelComplete(); }

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

    public void restartGame() {
        game.setScreen(new GameScreen(game, playerCount));
        dispose();
    }

    @Override
    public void render(float delta) {
        stateTime += Gdx.graphics.getDeltaTime();

        if (gameStateManager.isGameOver()) {
            Gdx.app.log("GameScreen", "Game Over detected, switching to GameOverScreen");
            game.setScreen(new GameOverScreen(game, playerCount, player1Score, player2Score));
            dispose();
            return;
        }

        checkGameOver();
        checkLevelComplete();

        if (gameStateManager.isLevelComplete()) {
            gameStateManager.update(delta);
            if (gameStateManager.shouldSwitchToLevelCompleteScreen()) {
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

        if (!gameStateManager.isGameOver() && !gameStateManager.isPaused() && !gameStateManager.isLevelComplete()) {
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
        }

        inputManager.handleInput(delta);

        if (!gameStateManager.isGameOver() && !gameStateManager.isLevelComplete()) {
            gameRenderer.render(delta);
        }
    }

    private void checkGameOver() {
        boolean playersAlive = (player1 != null && player1.isAlive()) || (player2 != null && player2.isAlive());
        if (!playersAlive) {
            gameStateManager.triggerGameOver();;
            Gdx.app.log("GameScreen", "Game Over: No players alive");
        }
    }

    private void checkLevelComplete() {
        if (totalKilledEnemies >= 10 && !gameStateManager.isLevelComplete() && !gameStateManager.isGameOver()) {
            gameStateManager.triggerLevelComplete();
        }
    }

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
        totalKilledEnemies++;
        Gdx.app.log("GameScreen", "Enemy killed! Total killed: " + totalKilledEnemies);
    }

    @Override
    public void onBaseDestroyed(GameContext context) {
        gameStateManager.triggerGameOver();
        Gdx.app.log("GameScreen", "Observer: Base destroyed by " + context.destroyedBy +
                " on level " + context.level + ", players: " + context.playerCount);
    }

    @Override
    public void show() {}
    @Override
    public void resize(int width, int height) {}
    @Override
    public void pause() {
        gameStateManager.togglePause();
    }
    @Override
    public void resume() {}
    @Override
    public void hide() {}

    @Override
    public void dispose() {
        Gdx.app.log("GameScreen", "Disposing GameScreen resources");
        if (batch != null) {
            batch.dispose();
            batch = null;
        }
        if (textBatch != null) {
            textBatch.dispose();
            textBatch = null;
        }
        if (bullets != null) {
            for (Bullet bullet : bullets) {
                if (bullet != null) {
                    bullet.dispose();
                }
            }
            bullets.clear();
        }
        if (font != null) {
            font.dispose();
            font = null;
        }
        if (largeFont != null) {
            largeFont.dispose();
            largeFont = null;
        }
        if (explosionSound != null) {
            explosionSound.stop();
            explosionSound.dispose();
            explosionSound = null;
        }
        if (hitSound != null) {
            hitSound.stop();
            hitSound.dispose();
            hitSound = null;
        }
        if (levelIntro != null) {
            levelIntro.dispose();
            levelIntro = null;
        }
        if (inputManager != null) {
            inputManager.dispose();
            inputManager = null;
        }
        if (gameRenderer != null) {
            gameRenderer.dispose();
            gameRenderer = null;
        }
        if (game != null) {
            game.getEventPublisher().removeObserver(this);
        }
    }
}