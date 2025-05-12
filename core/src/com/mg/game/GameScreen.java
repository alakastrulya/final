package com.mg.game;

import java.util.ArrayList;
import java.util.Arrays;

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

import java.util.ArrayList;
import java.util.Iterator;

public class GameScreen implements Screen, GameObserver {
    private int playerCount;
    private gdxGame game;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private SpriteBatch textBatch; // Separate SpriteBatch for text
    public float stateTime;
    private Tank player1;
    private Tank player2;
    public ArrayList<Bullet> bullets;
    private ArrayList<Tank> enemies;
    private ArrayList<Explosion> explosions; // Added for explosion animation
    private LevelIntroAnimation levelIntro;
    private boolean isLevelIntroPlaying = true;
    private int totalKilledEnemies;
    private InputHandler inputHandler;

    // Variables for controlling movement speed
    private float moveTimer = 0f;
    private static final float MOVE_DELAY = 0.003f;

    // Variable for controlling enemy movement
    private float enemyMoveTimer = 0f;
    private static final float ENEMY_MOVE_DELAY = 0.04f;

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
    private static final int MAX_ENEMIES_ON_MAP = 3;
    private static final int TOTAL_ENEMIES_PER_LEVEL = 10;
    private int remainingEnemies = TOTAL_ENEMIES_PER_LEVEL - MAX_ENEMIES_ON_MAP;
    private float enemyRespawnTimer = 0f;
    private static final float ENEMY_RESPAWN_DELAY = 3.0f; // Seconds between enemy respawns
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

    public int getPlayerCount() {
        return playerCount;
    }

    public float getStateTime() {
        return stateTime;
    }

    // Structure for storing enemy movement information
    private class EnemyMovementInfo {
        public Tank.Direction direction;
        public float directionChangeTimer;
        public int movementDistance;
        public boolean isStuck;
        public int stuckCounter;

        public EnemyMovementInfo() {
            direction = Tank.Direction.BACKWARD;
            directionChangeTimer = (float) (Math.random() * 2.0f + 1.0f);
            movementDistance = 0;
            isStuck = false;
            stuckCounter = 0;
        }
    }

    // Movement information for each enemy
    private ArrayList<EnemyMovementInfo> enemyMovementInfos;

    // Constants for controlling enemy movement
    private static final float MIN_DIRECTION_CHANGE_TIME = 1.0f;
    private static final float MAX_DIRECTION_CHANGE_TIME = 3.0f;
    private static final int STUCK_THRESHOLD = 10; // Number of movement attempts before considering the tank stuck
    private static final int MIN_MOVEMENT_BEFORE_CHANGE = 20; // Minimum distance before changing direction

    public GameScreen(gdxGame game, int playerCount) {
        this(game, playerCount, 1); // Start with level 1 by default
    }

    public GameScreen(gdxGame game, int playerCount, int level) {
        this.playerCount = playerCount;
        this.game = game;
        gdxGame.addObserver(new GameObserver() {
            @Override
            public void onBaseDestroyed() {
                gameOver = true;
                Gdx.app.log("Observer", "Base destroyed. Setting gameOver = true");
            }
        });
        this.currentLevel = level;
        camera = new OrthographicCamera();
        camera.setToOrtho(true, 640, 480);
        batch = new SpriteBatch();
        textBatch = new SpriteBatch();
        stateTime = 0f;
        bullets = new ArrayList<>();
        enemies = new ArrayList<>();
        explosions = new ArrayList<>(); // Initialize explosions list
        enemyMovementInfos = new ArrayList<>();
        totalKilledEnemies = 0;
        font = new BitmapFont(true);
        game.addObserver(this);

        game.resetGameOverFlag();

        largeFont = new BitmapFont(false);
        largeFont.getData().setScale(5f);

        // Load game resources
        Assets.loadLevel(currentLevel);
        Assets.loadCurtainTextures();

        // Load animations for all tank colors before creating tanks
        Assets.loadTankAnimations("yellow", 1);
        Assets.loadTankAnimations("green", 1);
        Assets.loadTankAnimations("red", 1);

        // Load textures for the score screen
        Assets.loadScoreScreenTextures();

        // Load sounds
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


        // Initialize first tank
        PlayerTankFactory player1Factory = new PlayerTankFactory("yellow", 1, this);
        player1 = player1Factory.create();
        player1.positionX = 152;
        player1.positionY = 450;
        Gdx.app.log("GameScreen", "Player 1 color: " + player1.getColour());


        // Initialize second tank if two-player mode is selected
        if (playerCount == 2) {
            PlayerTankFactory player2Factory = new PlayerTankFactory("green", 1, this);
            player2 = player2Factory.create();
            player2.positionX = 299;
            player2.positionY = 450;
            Gdx.app.log(
                    "GameScreen",
                    "Player 2 initialized at x=" + player2.positionX + ", y=" + player2.positionY + ", alive=" + player2.isAlive()
            );
        }

        // Initialize map
        mapLoader = new MapLoader();

        this.remainingEnemies = TOTAL_ENEMIES_PER_LEVEL - MAX_ENEMIES_ON_MAP;

        // Initialize only MAX_ENEMIES_ON_MAP enemies initially at fixed spawn points
        EnemyTankFactory enemyFactory = new EnemyTankFactory("red", 1, this);
        for (int i = 0; i < MAX_ENEMIES_ON_MAP; i++) {
            Tank enemy = enemyFactory.create();
            enemy.setStrategy(getRandomStrategy());

            // Use fixed spawn points
            int spawnPointIndex = i % SPAWN_POINTS.length;
            int spawnX = SPAWN_POINTS[spawnPointIndex][0];
            int spawnY = SPAWN_POINTS[spawnPointIndex][1];

            // Check if the spawn point is clear
            if (isSpawnPointClear(spawnX, spawnY)) {
                enemy.positionX = spawnX;
                enemy.positionY = spawnY;
            } else {
                // If the point is occupied, find the nearest free spot
                int[] freeSpawn = findNearestFreeSpot(spawnX, spawnY);
                enemy.positionX = freeSpawn[0];
                enemy.positionY = freeSpawn[1];
            }

            // Set initial direction - down (towards the player)
            enemy.setDirection(Tank.Direction.BACKWARD);

            enemies.add(enemy);

            // Create movement information for this enemy
            EnemyMovementInfo movementInfo = new EnemyMovementInfo();
            // Choose a random initial direction
            Tank.Direction[] directions = Tank.Direction.values();
            movementInfo.direction = directions[(int) (Math.random() * directions.length)];
            enemy.setDirection(movementInfo.direction);
            enemyMovementInfos.add(movementInfo);

            Gdx.app.log("GameScreen", "Spawned initial enemy at " + enemy.positionX + ", " + enemy.positionY);
        }

        inputHandler = new InputHandler(player1, player2, bullets, playerCount, this);

        // Initialize level intro animation
        levelIntro = new LevelIntroAnimation(currentLevel);
        isLevelIntroPlaying = true;

        // Load explosion animation
        Assets.loadExplosionAnimation();

        Gdx.app.log(
                "GameScreen",
                "Initialized with " + enemies.size() + " enemies, " + remainingEnemies + " remaining to spawn"
        );
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

        // Check for pause key press (P or ESC)
        if (Gdx.input.isKeyJustPressed(Input.Keys.P) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            isPaused = !isPaused; // Toggle pause state
        }

        // Toggle debug mode
        if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
            debugMode = !debugMode;
        }

        // Check if the game is over
        checkGameOver();

        // Check if the level is complete
        checkLevelComplete();

        // If the level is complete, update the timer
        if (levelComplete) {
            levelCompleteTimer += delta;

            if (levelCompleteTimer >= LEVEL_COMPLETE_DELAY) {
                // Collect real data
                int[] tankCounts = new int[4];
                for (int i = 0; i < 4; i++) {
                    tankCounts[i] = (player1PointsBreakdown[i] + player2PointsBreakdown[i]) / 100;
                }
                Gdx.app.log("ScoreDebug", "player1Score = " + player1Score);
                game.setScreen(
                        new LevelCompleteScreen(
                                game,
                                currentLevel,
                                20000,
                                player1Score,
                                player2Score,
                                player1PointsBreakdown,
                                player2PointsBreakdown,
                                tankCounts,
                                playerCount == 2
                        )
                );
                dispose();
                return;
            }
        }


        // If the level intro animation is playing
        if (isLevelIntroPlaying) {
            levelIntro.update(delta);

            // If the animation is finished, start the game
            if (levelIntro.isFinished()) {
                isLevelIntroPlaying = false;
            }

            batch.begin();
            // Draw the level background
            batch.draw(Assets.levelBack, 0, 0, 480, 480);

            // Draw the map
            int offsetX = -17;
            int offsetY = -17;

            for (MapTile tile : mapLoader.tiles) {
                float scaledSize = MapLoader.TILE_SIZE / TILE_SCALE;
                float drawX = tile.x * scaledSize + offsetX;
                float drawY = tile.y * scaledSize + offsetY;
                batch.draw(tile.region, drawX, drawY, scaledSize, scaledSize);
            }

            // Draw the level intro animation
            levelIntro.render(batch);

            batch.end();
            return;
        }

        // Update the game only if it's not paused and not over
        if (!gameOver && !isPaused && !levelComplete) {
            // Update movement timers
            moveTimer += delta;
            enemyMoveTimer += delta;

            // Update tank states
            if (player1 != null) player1.update(delta);
            if (player2 != null) player2.update(delta);
            for (Tank enemy : enemies) {
                if (enemy != null && enemy.isAlive()) {
                    enemy.update(delta);
                }
            }

            // Update enemies only if enough time has passed
            if (enemyMoveTimer >= ENEMY_MOVE_DELAY) {
                updateEnemies(delta);
                enemyMoveTimer = 0;
            }

            // Check if new enemies need to be spawned
            checkEnemyRespawn(delta);

            // Update bullets and check for hits
            bulletManager.update(delta);

            // Update explosions
            updateExplosions(delta);

            // Call InputHandler to process input
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

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // 1. Background
        batch.draw(Assets.levelBack, 0, 0, 480, 480);
        batch.setColor(0, 0, 0, 0.4f); // Black with 40% transparency
        batch.draw(Assets.pixel, 480, 0, 160, 480); // x=480 (start of sidebar), width=160
        batch.setColor(Color.WHITE); // Restore color

        // 2. Map
        int offsetX = -17, offsetY = -17;
        float scaled = MapLoader.TILE_SIZE / TILE_SCALE;

        float baseOffsetX = -BASE_TILE_SHIFT;
        float baseOffsetY = -BASE_TILE_SHIFT;

        boolean baseDrawn = false;
        for (MapTile tile : mapLoader.tiles) {
            if (tile.isBase) {
                // Draw ONLY the top-left base tile — it represents the entire base texture
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

        // 3. Tanks
        if (player1 != null && player1.isAlive()) {
            TextureRegion frame1 = player1.getCurrentFrame();
            if (frame1 != null) {
                // If the tank is invulnerable, draw it blinking
                if (!player1.isInvulnerable() || (int) (stateTime * 10) % 2 == 0) {
                    batch.draw(frame1, player1.positionX, player1.positionY, 26 / TILE_SCALE, 26 / TILE_SCALE);
                }
            }
        }

        if (player2 != null && player2.isAlive()) {
            TextureRegion frame2 = player2.getCurrentFrame();
            if (frame2 != null) {
                // If the tank is invulnerable, draw it blinking
                if (!player2.isInvulnerable() || (int) (stateTime * 10) % 2 == 0) {
                    batch.draw(frame2, player2.positionX, player2.positionY, 26 / TILE_SCALE, 26 / TILE_SCALE);
                }
            }
        }

        // Draw alive enemies
        for (Tank enemy : enemies) {
            if (enemy != null && enemy.isAlive()) {
                TextureRegion enemyFrame = enemy.getCurrentFrame();
                if (enemyFrame != null) {
                    batch.draw(enemyFrame, enemy.positionX, enemy.positionY, 26 / TILE_SCALE, 26 / TILE_SCALE);
                }
            }
        }

        // 4. Bullets
        for (Bullet bullet : bullets) {
            if (bullet.isActive() && bullet.getTexture() != null) {
                // Draw the bullet only if it's within the game map
                if (
                        bullet.getPositionX() >= 0 &&
                                bullet.getPositionX() < 480 &&
                                bullet.getPositionY() >= 0 &&
                                bullet.getPositionY() < 480
                ) {
                    batch.draw(bullet.getTexture(), bullet.getPositionX(), bullet.getPositionY(), 4, 4);
                }
            }
        }

        // 5. Explosions
        for (Explosion explosion : explosions) {
            if (!explosion.isFinished()) {
                TextureRegion frame = explosion.getCurrentFrame();
                if (frame != null) {
                    // Draw the explosion without additional offset, as it's already handled in the Explosion class
                    batch.draw(frame, explosion.getPositionX(), explosion.getPositionY(), 32, 32);
                }
            }
        }

        // Display score and health
        font.setColor(Color.BLACK);

        // Score
        font.draw(batch, "Score: " + score, 500, 50);

        // Enemy panel — always 10 icons, bottom to top
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

        // P1 Health
        if (Assets.healthIcon != null && player1 != null) {
            font.draw(batch, "P1: Health", 500, 220);
            for (int i = 0; i < player1.getHealth(); i++) {
                batch.draw(Assets.healthIcon, 500 + i * 20, 240, 16, 16);
            }
        }

        // P2 Health
        if (Assets.healthIcon != null && player2 != null) {
            font.draw(batch, "P2: Health", 500, 270);
            for (int i = 0; i < player2.getHealth(); i++) {
                batch.draw(Assets.healthIcon, 500 + i * 20, 290, 16, 16);
            }
        }

        // Display debug information if debug mode is enabled
        if (debugMode) {
            // Display information about enemy movement direction
            for (int i = 0; i < enemies.size() && i < enemyMovementInfos.size(); i++) {
                if (enemies.get(i).isAlive()) {
                    EnemyMovementInfo info = enemyMovementInfos.get(i);
                    String dirStr = "?";
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
                    }
                    // Note: Removed incomplete debug drawing code as it was not fully implemented
                }
            }
        }

        batch.end();

        // Separate rendering for PAUSE and GAME OVER text
        if (gameOver || isPaused) {
            textBatch.begin();

            Matrix4 normalMatrix = new Matrix4();
            normalMatrix.setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            textBatch.setProjectionMatrix(normalMatrix);

            if (gameOver) {
                if (Assets.gameOverTexture != null) {
                    textBatch.draw(
                            Assets.gameOverTexture,
                            Gdx.graphics.getWidth() / 2 - Assets.gameOverTexture.getWidth() / 2,
                            Gdx.graphics.getHeight() / 2 - Assets.gameOverTexture.getHeight() / 2
                    );
                } else {
                    largeFont.setColor(Color.RED);
                    GlyphLayout gameOverLayout = new GlyphLayout(largeFont, "GAME OVER");
                    largeFont.draw(
                            textBatch,
                            "GAME OVER",
                            Gdx.graphics.getWidth() / 2 - gameOverLayout.width / 2,
                            Gdx.graphics.getHeight() / 2 + gameOverLayout.height / 2
                    );

                    GlyphLayout restartLayout = new GlyphLayout(largeFont, "Press ENTER to restart");
                    largeFont.setColor(Color.WHITE);
                    largeFont.draw(
                            textBatch,
                            "Press ENTER to restart",
                            Gdx.graphics.getWidth() / 2 - restartLayout.width / 2,
                            Gdx.graphics.getHeight() / 2 + gameOverLayout.height / 2 + 40
                    );
                }
            } else if (isPaused) {
                if (Assets.pauseTexture != null) {
                    textBatch.draw(
                            Assets.pauseTexture,
                            Gdx.graphics.getWidth() / 2 - Assets.pauseTexture.getWidth() / 2,
                            Gdx.graphics.getHeight() / 2 - Assets.pauseTexture.getHeight() / 2
                    );
                } else {
                    largeFont.setColor(Color.YELLOW);
                    GlyphLayout pauseLayout = new GlyphLayout(largeFont, "PAUSE");
                    largeFont.draw(
                            textBatch,
                            "PAUSE",
                            Gdx.graphics.getWidth() / 2 - pauseLayout.width / 2,
                            Gdx.graphics.getHeight() / 2 + pauseLayout.height / 2
                    );

                    GlyphLayout continueLayout = new GlyphLayout(largeFont, "Press P or ESC to continue");
                    largeFont.setColor(Color.WHITE);
                    largeFont.draw(
                            textBatch,
                            "Press P or ESC to continue",
                            Gdx.graphics.getWidth() / 2 - continueLayout.width / 2,
                            Gdx.graphics.getHeight() / 2 + pauseLayout.height / 2 + 40
                    );
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
        // Level is complete when all enemies are defeated (both on map and remaining to spawn)
        boolean enemiesDefeated = true;
        for (Tank enemy : enemies) {
            if (enemy != null && enemy.isAlive()) {
                enemiesDefeated = false;
                break;
            }
        }

        // If all enemies on map are defeated and no more enemies to spawn
        if (enemiesDefeated && remainingEnemies <= 0 && !levelComplete && !gameOver) {
            levelComplete = true;
            levelCompleteTimer = 0f;
        }
    }

    // Method to count alive enemies
    private int countAliveEnemies() {
        int count = 0;
        for (Tank enemy : enemies) {
            if (enemy != null && enemy.isAlive()) {
                count++;
            }
        }
        return count;
    }

    // Check if the spawn point is clear
    private boolean isSpawnPointClear(int x, int y) {
        // Check collision with the map
        if (checkCollisionWithMap(x, y, null)) {
            return false;
        }

        // Check collision with players
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

        // Check collision with other enemies
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

    // Find the nearest free spot
    private int[] findNearestFreeSpot(int startX, int startY) {
        // Check within a 50-pixel radius
        for (int radius = 5; radius <= 50; radius += 5) {
            for (int offsetX = -radius; offsetX <= radius; offsetX += 5) {
                for (int offsetY = -radius; offsetY <= radius; offsetY += 5) {
                    int x = startX + offsetX;
                    int y = startY + offsetY;

                    // Check if the point is within the map
                    if (x >= 0 && x <= 454 && y >= 0 && y <= 454) {
                        if (isSpawnPointClear(x, y)) {
                            return new int[]{x, y};
                        }
                    }
                }
            }
        }

        // If no free point is found, return the original (this may cause issues)
        Gdx.app.error("GameScreen", "Could not find free spawn point near " + startX + ", " + startY);
        return new int[]{startX, startY};
    }

    // New method to check and spawn new enemies
    private void checkEnemyRespawn(float delta) {
        // Check if new enemies need to be spawned
        int aliveEnemies = countAliveEnemies();

        if (aliveEnemies < MAX_ENEMIES_ON_MAP && remainingEnemies > 0) {
            enemyRespawnTimer += delta;

            if (enemyRespawnTimer >= ENEMY_RESPAWN_DELAY) {
                spawnNewEnemy();
                enemyRespawnTimer = 0f;
                remainingEnemies--;

                Gdx.app.log("GameScreen", "Spawned new enemy. Remaining: " + remainingEnemies);
            }
        }
    }

    // Completely reworked enemy update method
    private void updateEnemies(float delta) {
        // Iterate through all enemies
        for (int i = 0; i < enemies.size(); i++) {
            Tank enemy = enemies.get(i);
            if (enemy == null || !enemy.isAlive()) continue;

            if (enemy.getStrategy() != null) {
                enemy.getStrategy().update(enemy, delta, this);
            }

            // Get movement information for this enemy
            if (i >= enemyMovementInfos.size()) {
                // If no information exists, create new
                enemyMovementInfos.add(new EnemyMovementInfo());
            }
            EnemyMovementInfo info = enemyMovementInfos.get(i);

            // Update direction change timer
            info.directionChangeTimer -= delta;

            // Check if direction needs to be changed
            boolean shouldChangeDirection = false;

            // Change direction if:
            // 1. Timer has expired
            if (info.directionChangeTimer <= 0) {
                shouldChangeDirection = true;
            }
            // 2. Tank is stuck
            else if (info.isStuck && info.stuckCounter >= STUCK_THRESHOLD) {
                shouldChangeDirection = true;
                info.stuckCounter = 0;
            }
            // 3. Tank has moved enough distance
            else if (info.movementDistance >= MIN_MOVEMENT_BEFORE_CHANGE && Math.random() < 0.05) {
                shouldChangeDirection = true;
            }

            // If direction needs to be changed
            if (shouldChangeDirection) {
                // Choose new direction
                chooseNewDirectionForEnemy(enemy, info, i);

                // Reset counters
                info.directionChangeTimer = (float) (
                        Math.random() * (MAX_DIRECTION_CHANGE_TIME - MIN_DIRECTION_CHANGE_TIME) + MIN_DIRECTION_CHANGE_TIME
                );
                info.movementDistance = 0;
                info.isStuck = false;
                info.stuckCounter = 0;
            }

            // Try to move in the current direction
            boolean moved = moveEnemyInDirection(enemy, info);

            // If movement failed, increment stuck counter
            if (!moved) {
                info.isStuck = true;
                info.stuckCounter++;

                // If stuck too many times, change direction immediately
                if (info.stuckCounter >= STUCK_THRESHOLD) {
                    chooseNewDirectionForEnemy(enemy, info, i);
                    info.directionChangeTimer = (float) (
                            Math.random() * (MAX_DIRECTION_CHANGE_TIME - MIN_DIRECTION_CHANGE_TIME) + MIN_DIRECTION_CHANGE_TIME
                    );
                    info.movementDistance = 0;
                    info.isStuck = false;
                    info.stuckCounter = 0;
                }
            } else {
                // If movement succeeded, increment distance counter
                info.movementDistance++;
                info.isStuck = false;
            }

            // Add random shooting
            if (Math.random() < 0.005) { // 0.5% chance to shoot
                Bullet bullet = enemy.shoot();
                if (bullet != null) {
                    bullets.add(bullet);
                }
            }
        }
    }

    // Method to choose a new direction for an enemy
    private void chooseNewDirectionForEnemy(Tank enemy, EnemyMovementInfo info, int enemyIndex) {
        // Determine target: player or base
        boolean targetBase = Math.random() < 0.3; // 30% chance to target the base, 70% the player

        Tank targetPlayer = null;
        if (player1 != null && player1.isAlive()) {
            targetPlayer = player1;
        } else if (player2 != null && player2.isAlive()) {
            targetPlayer = player2;
        }

        // If no players are alive, target the base
        if (targetPlayer == null) {
            targetBase = true;
        }

        int targetX, targetY;
        if (targetBase) {
            targetX = baseX;
            targetY = baseY;
        } else {
            targetX = targetPlayer.positionX;
            targetY = targetPlayer.positionY;
        }

        // Calculate distance to the target horizontally and vertically
        int dx = targetX - enemy.positionX;
        int dy = targetY - enemy.positionY;

        // Decide whether to move horizontally or vertically
        // Add randomness for more natural movement
        boolean moveHorizontally = Math.abs(dx) > Math.abs(dy) || Math.random() < 0.3;

        // Choose direction with a higher probability towards the target
        if (moveHorizontally) {
            // Move horizontally
            if (dx > 0) {
                if (Math.random() < 0.8) {
                    // 80% chance to choose the correct direction
                    info.direction = Tank.Direction.RIGHT;
                } else {
                    // 20% chance to choose a random other direction
                    Tank.Direction[] otherDirections = {
                            Tank.Direction.LEFT, Tank.Direction.FORWARD, Tank.Direction.BACKWARD
                    };
                    info.direction = otherDirections[(int) (Math.random() * otherDirections.length)];
                }
            } else {
                if (Math.random() < 0.8) {
                    // 80% chance to choose the correct direction
                    info.direction = Tank.Direction.LEFT;
                } else {
                    // 20% chance to choose a random other direction
                    Tank.Direction[] otherDirections = {
                            Tank.Direction.RIGHT, Tank.Direction.FORWARD, Tank.Direction.BACKWARD
                    };
                    info.direction = otherDirections[(int) (Math.random() * otherDirections.length)];
                }
            }
        } else {
            // Move vertically (account for flipped coordinate system)
            if (dy > 0) {
                if (Math.random() < 0.8) {
                    // 80% chance to choose the correct direction
                    info.direction = Tank.Direction.BACKWARD; // Down
                } else {
                    // 20% chance to choose a random other direction
                    Tank.Direction[] otherDirections = {
                            Tank.Direction.FORWARD, Tank.Direction.LEFT, Tank.Direction.RIGHT
                    };
                    info.direction = otherDirections[(int) (Math.random() * otherDirections.length)];
                }
            } else {
                if (Math.random() < 0.8) {
                    // 80% chance to choose the correct direction
                    info.direction = Tank.Direction.FORWARD; // Up
                } else {
                    // 20% chance to choose a random other direction
                    Tank.Direction[] otherDirections = {
                            Tank.Direction.BACKWARD, Tank.Direction.LEFT, Tank.Direction.RIGHT
                    };
                    info.direction = otherDirections[(int) (Math.random() * otherDirections.length)];
                }
            }
        }

        // Set the chosen direction for the tank
        enemy.setDirection(info.direction);
    }

    private boolean moveEnemyInDirection(Tank enemy, EnemyMovementInfo info) {
        // Determine new coordinates based on direction
        int newX = enemy.positionX;
        int newY = enemy.positionY;
        int keycode = -1;

        // Increase movement speed (3 pixels per step)
        int moveSpeed = 3;

        switch (info.direction) {
            case FORWARD: // Up (in flipped coordinate system, this is -Y)
                newY = enemy.positionY - moveSpeed;
                keycode = Input.Keys.UP;
                break;
            case BACKWARD: // Down (in flipped coordinate system, this is +Y)
                newY = enemy.positionY + moveSpeed;
                keycode = Input.Keys.DOWN;
                break;
            case LEFT:
                newX = enemy.positionX - moveSpeed;
                keycode = Input.Keys.LEFT;
                break;
            case RIGHT:
                newX = enemy.positionX + moveSpeed;
                keycode = Input.Keys.RIGHT;
                break;
        }

        // Check if movement is possible in this direction
        boolean canMove = true;

        // Check map boundaries
        if (newX < 0 || newX > 454 - 9 || newY < 0 || newY > 454) {
            canMove = false;
            Gdx.app.log("Collision", "Enemy at " + enemy.positionX + ", " + enemy.positionY + " out of bounds");
        } else {
            // Check collisions
            if (checkCollisionWithPlayer(enemy, newX, newY)) {
                canMove = false;
                Gdx.app.log("Collision", "Enemy at " + enemy.positionX + ", " + enemy.positionY + " collides with player");
            } else if (checkCollisionWithEnemy(enemy, newX, newY) || checkCollisionWithMap(newX, newY, enemy)) {
                canMove = false;
                Gdx.app.log("Collision", "Enemy at " + enemy.positionX + ", " + enemy.positionY + " collides with something");
            }
        }

        // If movement is possible, update position
        if (canMove) {
            // IMPORTANT: Actually move the tank
            enemy.positionX = newX;
            enemy.positionY = newY;

            // Process input for animation
            try {
                enemy.handleInput(keycode, stateTime);
            } catch (Exception e) {
                Gdx.app.error("GameScreen", "Error handling input for enemy: " + e.getMessage());
            }

            return true;
        } else {
            // If movement is not possible, update animation only
            try {
                enemy.handleInput(-1, stateTime);
            } catch (Exception e) {
                Gdx.app.error("GameScreen", "Error handling input for enemy: " + e.getMessage());
            }

            return false;
        }
    }

    // Updated method for spawning a new enemy using fixed spawn points
    private void spawnNewEnemy() {
        int spawnPointIndex = (int) (Math.random() * SPAWN_POINTS.length);
        int spawnX = SPAWN_POINTS[spawnPointIndex][0];
        int spawnY = SPAWN_POINTS[spawnPointIndex][1];

        EnemyTankFactory enemyFactory = new EnemyTankFactory("red", 1, this);
        Tank enemy = enemyFactory.create();
        enemy.setStrategy(getRandomStrategy());

        if (isSpawnPointClear(spawnX, spawnY)) {
            enemy.positionX = spawnX;
            enemy.positionY = spawnY;
        } else {
            int[] freeSpawn = findNearestFreeSpot(spawnX, spawnY);
            enemy.positionX = freeSpawn[0];
            enemy.positionY = freeSpawn[1];
        }

        enemy.setDirection(Tank.Direction.BACKWARD);

        enemies.add(enemy);

        EnemyMovementInfo movementInfo = new EnemyMovementInfo();
        Tank.Direction[] directions = Tank.Direction.values();
        movementInfo.direction = directions[(int) (Math.random() * directions.length)];
        enemy.setDirection(movementInfo.direction);
        enemyMovementInfos.add(movementInfo);

        cleanupDeadEnemies();

        Gdx.app.log(
                "GameScreen",
                "Spawned new enemy at " +
                        enemy.positionX +
                        ", " +
                        enemy.positionY +
                        ". Alive enemies: " +
                        countAliveEnemies() +
                        ", Remaining: " +
                        remainingEnemies
        );
    }

    // Method to remove dead enemies from the list
    private void cleanupDeadEnemies() {
        for (int i = enemies.size() - 1; i >= 0; i--) {
            Tank enemy = enemies.get(i);
            if (enemy == null || !enemy.isAlive()) {
                enemies.remove(i);
                if (i < enemyMovementInfos.size()) {
                    enemyMovementInfos.remove(i);
                }
            }
        }
    }

    private boolean checkCollisionWithPlayer(Tank enemy, int newX, int newY) {
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

    private EnemyStrategy getRandomStrategy() {
        int r = (int) (Math.random() * 3); // 0, 1, or 2
        switch (r) {
            case 0:
                return new BaseAttackStrategy();
            case 1:
                return new AggressiveChaseStrategy();
            case 2:
                return new WanderStrategy(); // If exists
            default:
                return new BaseAttackStrategy(); // Fallback
        }
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

    public com.badlogic.gdx.utils.Array<MapTile> getMapTiles() {
        return mapLoader.tiles;
    }


    public int getTileSize() {
        return MapLoader.TILE_SIZE;
    }

    public float getTileScale() {
        return TILE_SCALE;
    }

    public int getOffsetX() {
        return -17;
    }

    public int getOffsetY() {
        return -17;
    }

    public ArrayList<Tank> getEnemies() {
        return enemies;
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


}