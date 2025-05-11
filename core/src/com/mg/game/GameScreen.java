package com.mg.game;

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
import com.mg.game.map.MapLoader;
import com.mg.game.map.MapTile;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;

public class GameScreen implements Screen {

    private int playerCount;
    private gdxGame game;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private SpriteBatch textBatch; // Отдельный SpriteBatch для текста
    private float stateTime;
    private Tank player1;
    private Tank player2;
    private ArrayList<Bullet> bullets;
    private ArrayList<Tank> enemies;
    private ArrayList<Explosion> explosions; // Added for explosion animation
    private float player1ShootCooldown = 0f;
    private float player2ShootCooldown = 0f;
    private static final float SHOOT_COOLDOWN = 0.3f; // Задержка между выстрелами в секундах
    private LevelIntroAnimation levelIntro;
    private boolean isLevelIntroPlaying = true;
    private int totalKilledEnemies;

    // Переменные для контроля скорости движения
    private float moveTimer = 0f;
    private static final float MOVE_DELAY = 0.005f;

    // Переменная для контроля движения врагов
    private float enemyMoveTimer = 0f;
    private static final float ENEMY_MOVE_DELAY = 0.04f;

    // Переменные для отслеживания состояния игры
    private int score = 0;
    private boolean gameOver = false;
    private BitmapFont font;
    private BitmapFont largeFont;
    private Sound explosionSound;
    private Sound hitSound;

    // Переменная для отслеживания состояния паузы
    private boolean isPaused = false;

    // Переменные для карты
    private MapLoader mapLoader;
    private static final float TILE_SCALE = 0.87f;
    private static final float BASE_TILE_SHIFT = MapLoader.TILE_SIZE / TILE_SCALE;

    // Переменные для отслеживания завершения уровня
    private boolean levelComplete = false;
    private float levelCompleteTimer = 0f;
    private static final float LEVEL_COMPLETE_DELAY = 2.0f; // Delay before showing score screen

    // Текущий уровень
    private int currentLevel;

    // Add these fields to the GameScreen class
    private static final int MAX_ENEMIES_ON_MAP = 3;
    private static final int TOTAL_ENEMIES_PER_LEVEL = 10;
    private int remainingEnemies = TOTAL_ENEMIES_PER_LEVEL - MAX_ENEMIES_ON_MAP;
    private float enemyRespawnTimer = 0f;
    private static final float ENEMY_RESPAWN_DELAY = 3.0f; // Seconds between enemy respawns
    private int baseX = 320; // X coordinate of the base (adjust as needed)
    private int baseY = 440; // Y coordinate of the base (adjust as needed)

    // Добавляем отладочную информацию
    private boolean debugMode = true;

    // Фиксированные точки появления врагов
    private final int[][] SPAWN_POINTS = {
            {80, 40},    // Левый верхний угол
            {240, 40},   // Центр верха
            {400, 40}    // Правый верхний угол
    };

    // Структура для хранения информации о движении врагов
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

    // Информация о движении для каждого врага
    private ArrayList<EnemyMovementInfo> enemyMovementInfos;

    // Константы для управления движением врагов
    private static final float MIN_DIRECTION_CHANGE_TIME = 1.0f;
    private static final float MAX_DIRECTION_CHANGE_TIME = 3.0f;
    private static final int STUCK_THRESHOLD = 10; // Количество попыток движения, после которых считаем, что танк застрял
    private static final int MIN_MOVEMENT_BEFORE_CHANGE = 20; // Минимальное расстояние перед сменой направления

    public GameScreen(gdxGame game, int playerCount) {
        this(game, playerCount, 1); // По умолчанию начинаем с уровня 1
    }

    public GameScreen(gdxGame game, int playerCount, int level) {
        this.playerCount = playerCount;
        this.game = game;
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

        gdxGame.resetGameOverFlag();

        largeFont = new BitmapFont(false);
        largeFont.getData().setScale(5f);

        // Загружаем ресурсы для игры
        Assets.loadLevel(currentLevel);
        Assets.loadCurtainTextures();

        // Загружаем анимации для всех цветов танков перед созданием танков
        Assets.loadTankAnimations("yellow", 1);
        Assets.loadTankAnimations("green", 1);
        Assets.loadTankAnimations("red", 1);

        // Загружаем текстуры для экрана счета
        Assets.loadScoreScreenTextures();

        // Загрузка звуков
        try {
            explosionSound = Gdx.audio.newSound(Gdx.files.internal("sounds/explosion.mp3"));
        } catch (Exception e) {
            Gdx.app.error("GameScreen", "Error loading explosion sound: " + e.getMessage());
            explosionSound = null;
        }

        try {
            hitSound = Gdx.audio.newSound(Gdx.files.internal("sounds/hit.mp3"));
        } catch (Exception e) {
            Gdx.app.error("GameScreen", "Error loading hit sound: " + e.getMessage());
            hitSound = null;
        }

        // Инициализация первого танка
        player1 = new Tank("yellow", 1, false);
        player1.positionX = 152;
        player1.positionY = 450;
        Gdx.app.log("GameScreen", "Player 1 color: " + player1.getColour());

        // Инициализация второго танка, если выбран режим на 2 игрока
        if (playerCount == 2) {
            player2 = new Tank("green", 1, false);
            player2.positionX = 299;
            player2.positionY = 450;
            Gdx.app.log("GameScreen", "Player 2 color: " + player2.getColour());
        }

        // Инициализация карты
        mapLoader = new MapLoader();

        this.remainingEnemies = TOTAL_ENEMIES_PER_LEVEL - MAX_ENEMIES_ON_MAP;

        // Initialize only MAX_ENEMIES_ON_MAP enemies initially at fixed spawn points
        for (int i = 0; i < MAX_ENEMIES_ON_MAP; i++) {
            Tank enemy = new Tank("red", 1, true);

            // Используем фиксированные точки появления
            int spawnPointIndex = i % SPAWN_POINTS.length;
            int spawnX = SPAWN_POINTS[spawnPointIndex][0];
            int spawnY = SPAWN_POINTS[spawnPointIndex][1];

            // Проверяем, свободна ли точка появления
            if (isSpawnPointClear(spawnX, spawnY)) {
                enemy.positionX = spawnX;
                enemy.positionY = spawnY;
            } else {
                // Если точка занята, ищем ближайшую свободную точку
                int[] freeSpawn = findNearestFreeSpot(spawnX, spawnY);
                enemy.positionX = freeSpawn[0];
                enemy.positionY = freeSpawn[1];
            }

            // Устанавливаем начальное направление - вниз (к игроку)
            enemy.setDirection(Tank.Direction.BACKWARD);

            enemies.add(enemy);

            // Создаем информацию о движении для этого врага
            EnemyMovementInfo movementInfo = new EnemyMovementInfo();
            // Выбираем случайное начальное направление
            Tank.Direction[] directions = Tank.Direction.values();
            movementInfo.direction = directions[(int) (Math.random() * directions.length)];
            enemy.setDirection(movementInfo.direction);
            enemyMovementInfos.add(movementInfo);

            Gdx.app.log("GameScreen", "Spawned initial enemy at " + enemy.positionX + ", " + enemy.positionY);
        }

        // Инициализация анимации начала уровня
        levelIntro = new LevelIntroAnimation(currentLevel);
        isLevelIntroPlaying = true;

        // Загружаем анимацию взрыва
        Assets.loadExplosionAnimation();

        Gdx.app.log("GameScreen", "Initialized with " + enemies.size() + " enemies, " + remainingEnemies + " remaining to spawn");
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(192f / 255, 192f / 255, 192f / 255, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        camera.update();
        stateTime += Gdx.graphics.getDeltaTime();

        if (gdxGame.isGameOver()) {
            gameOver = true;
        }


        // Проверяем нажатие клавиши паузы (P или ESC)
        if (Gdx.input.isKeyJustPressed(Input.Keys.P) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            isPaused = !isPaused; // Переключаем состояние паузы
        }

        // Включение/выключение режима отладки
        if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
            debugMode = !debugMode;
        }

        // Проверяем, не закончилась ли игра
        checkGameOver();

        // Проверяем, не завершен ли уровень
        checkLevelComplete();

        // Если уровень завершен, обновляем таймер
        if (levelComplete) {
            levelCompleteTimer += delta;

            // Когда таймер достигает задержки, показываем экран счета
            if (levelCompleteTimer >= LEVEL_COMPLETE_DELAY) {
                // Рассчитываем распределение очков (примерные значения - настройте по необходимости)
                int[] player1PointsBreakdown = {
                        score / 4,      // Очки за первый тип танка
                        score / 4,      // Очки за второй тип танка
                        score / 4,      // Очки за третий тип танка
                        score / 4       // Очки за четвертый тип танка
                };

                int[] player2PointsBreakdown = {0, 0, 0, 0};
                if (playerCount == 2 && player2 != null) {
                    // Рассчитываем очки игрока 2, если в режиме 2 игроков
                    player2PointsBreakdown[0] = score / 8;
                    player2PointsBreakdown[1] = score / 8;
                    player2PointsBreakdown[2] = score / 8;
                    player2PointsBreakdown[3] = score / 8;
                }

                // Количество танков (сколько каждого типа было уничтожено)
                int[] tankCounts = {4, 3, 2, 1};

                // Создаем и показываем экран завершения уровня
                game.setScreen(new LevelCompleteScreen(
                        game,
                        currentLevel,
                        20000, // Рекорд
                        score, // Очки игрока 1
                        playerCount == 2 ? score / 2 : 0, // Очки игрока 2
                        player1PointsBreakdown,
                        player2PointsBreakdown,
                        tankCounts,
                        playerCount == 2
                ));
                dispose();
                return;
            }
        }

        // Если играет анимация начала уровня
        if (isLevelIntroPlaying) {
            levelIntro.update(delta);

            // Если анимация завершена, начинаем игру
            if (levelIntro.isFinished()) {
                isLevelIntroPlaying = false;
            }

            batch.begin();
            // Рисуем фон уровня
            batch.draw(Assets.levelBack, 0, 0, 480, 480);

            // Рисуем карту
            int offsetX = -17;
            int offsetY = -17;
            float tileScale = 0.8f;

            for (MapTile tile : mapLoader.tiles) {
                float scaledSize = MapLoader.TILE_SIZE / TILE_SCALE;
                float drawX = tile.x * scaledSize + offsetX;
                float drawY = tile.y * scaledSize + offsetY;
                batch.draw(
                        tile.region,
                        drawX,
                        drawY,
                        scaledSize,
                        scaledSize
                );
            }

            // Рисуем анимацию начала уровня
            levelIntro.render(batch);

            batch.end();
            return;
        }

        // Обновляем игру только если она не на паузе и не окончена
        if (!gameOver && !isPaused && !levelComplete) {
            // Обновляем кулдауны
            player1ShootCooldown -= delta;
            if (player2 != null) {
                player2ShootCooldown -= delta;
            }

            // Обновляем таймеры движения
            moveTimer += delta;
            enemyMoveTimer += delta;

            // Обновляем состояние танков
            if (player1 != null) player1.update(delta);
            if (player2 != null) player2.update(delta);
            for (Tank enemy : enemies) {
                if (enemy != null && enemy.isAlive()) {
                    enemy.update(delta);
                }
            }

            // Обновляем врагов только если прошло достаточно времени
            if (enemyMoveTimer >= ENEMY_MOVE_DELAY) {
                updateEnemies(delta);
                enemyMoveTimer = 0;
            }

            // Проверяем, нужно ли создать новых врагов
            checkEnemyRespawn(delta);

            // Обновляем снаряды и проверяем попадания
            updateBullets(delta);

            // Обновляем взрывы
            updateExplosions(delta);

            // Проверяем нажатия клавиш только если прошло достаточно времени
            if (moveTimer >= MOVE_DELAY) {
                checkKeyPress();
                moveTimer = 0;
            }
        } else if (gameOver) {
            // Если игра окончена, проверяем нажатие клавиши для перезапуска
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                game.setScreen(new GameScreen(game, playerCount));
                dispose();
                return;
            }
        }

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // 1. Фон
        batch.draw(Assets.levelBack, 0, 0, 480, 480);
        batch.setColor(0, 0, 0, 0.4f); // Черный с 40% прозрачности
        batch.draw(Assets.pixel, 480, 0, 160, 480); // x=480 (начало боковой панели), ширина=160
        batch.setColor(Color.WHITE); // Восстанавливаем цвет

        // 2. Карта
        int offsetX = -17, offsetY = -17;
        float scaled = MapLoader.TILE_SIZE / TILE_SCALE;

        float baseOffsetX = -BASE_TILE_SHIFT;
        float baseOffsetY = -BASE_TILE_SHIFT;

        boolean baseDrawn = false;
        for (MapTile tile : mapLoader.tiles) {
            if (tile.isBase) {
                // Рисуем ТОЛЬКО левый верхний тайл базы — он отвечает за текстуру всей базы
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


        // 3. Танки
        if (player1 != null && player1.isAlive()) {
            TextureRegion frame1 = player1.getCurrentFrame();
            if (frame1 != null) {
                // Если танк неуязвим, рисуем его мигающим
                if (!player1.isInvulnerable() || (int) (stateTime * 10) % 2 == 0) {
                    batch.draw(frame1, player1.positionX, player1.positionY, 26 / TILE_SCALE, 26 / TILE_SCALE);
                }
            }
        }

        if (player2 != null && player2.isAlive()) {
            TextureRegion frame2 = player2.getCurrentFrame();
            if (frame2 != null) {
                // Если танк неуязвим, рисуем его мигающим
                if (!player2.isInvulnerable() || (int) (stateTime * 10) % 2 == 0) {
                    batch.draw(frame2, player2.positionX, player2.positionY, 26 / TILE_SCALE, 26 / TILE_SCALE);
                }
            }
        }

        // Рисуем врагов, которые живы
        for (Tank enemy : enemies) {
            if (enemy != null && enemy.isAlive()) {
                TextureRegion enemyFrame = enemy.getCurrentFrame();
                if (enemyFrame != null) {
                    batch.draw(enemyFrame, enemy.positionX, enemy.positionY, 26 / TILE_SCALE, 26 / TILE_SCALE);
                }
            }
        }

        // 4. Пули
        for (Bullet bullet : bullets) {
            if (bullet.isActive() && bullet.getTexture() != null) {
                // Отрисовываем пулю только если она находится в пределах игровой карты
                if (bullet.getPositionX() >= 0 && bullet.getPositionX() < 480 &&
                        bullet.getPositionY() >= 0 && bullet.getPositionY() < 480) {
                    batch.draw(bullet.getTexture(), bullet.getPositionX(), bullet.getPositionY(), 4, 4);
                }
            }
        }

        // 5. Взрывы
        for (Explosion explosion : explosions) {
            if (!explosion.isFinished()) {
                TextureRegion frame = explosion.getCurrentFrame();
                if (frame != null) {
                    // Рисуем взрыв без дополнительного смещения, так как оно уже учтено в классе Explosion
                    batch.draw(frame, explosion.getPositionX(), explosion.getPositionY(), 32, 32);
                }
            }
        }

        // Отображаем счет и здоровье
        font.setColor(Color.BLACK);

        // Счёт
        font.draw(batch, "Score: " + score, 500, 50);

        // Панель врагов — всегда 10 иконок, снизу вверх
        font.draw(batch, "Enemies: " + (10 - totalKilledEnemies) + "/10", 500, 70);
        if (Assets.enemyIcon != null) {
            int totalEnemies = 10;
            int iconsPerColumn = 5;
            int iconSpacing = 20;
            int baseX = 500;
            int baseY = 90;

            for (int i = 0; i < totalEnemies; i++) {
                if (i < totalKilledEnemies) continue;
                int col = i / iconsPerColumn;
                int row = i % iconsPerColumn;
                batch.draw(Assets.enemyIcon, baseX + col * iconSpacing, baseY + row * iconSpacing, 16, 16);
            }
        }

        // Здоровье P1
        if (Assets.healthIcon != null && player1 != null) {
            font.draw(batch, "P1: Health", 500, 220);
            for (int i = 0; i < player1.getHealth(); i++) {
                batch.draw(Assets.healthIcon, 500 + i * 20, 240, 16, 16);
            }
        }

        // Здоровье P2
        if (Assets.healthIcon != null && player2 != null) {
            font.draw(batch, "P2: Health", 500, 270);
            for (int i = 0; i < player2.getHealth(); i++) {
                batch.draw(Assets.healthIcon, 500 + i * 20, 290, 16, 16);
            }
        }

        // Отображаем отладочную информацию, если включен режим отладки
        if (debugMode) {
            // Отображаем информацию о направлении движения врагов
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
                }
            }
        }

        batch.end();

        // Отдельная отрисовка для текста PAUSE и GAME OVER
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

                    GlyphLayout restartLayout = new GlyphLayout(largeFont, "Нажмите ENTER для перезапуска");
                    largeFont.setColor(Color.WHITE);
                    largeFont.draw(textBatch, "Нажмите ENTER для перезапуска",
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

                    GlyphLayout continueLayout = new GlyphLayout(largeFont, "Нажмите P или ESC для продолжения");
                    largeFont.setColor(Color.WHITE);
                    largeFont.draw(textBatch, "Нажмите P или ESC для продолжения",
                            Gdx.graphics.getWidth() / 2 - continueLayout.width / 2,
                            Gdx.graphics.getHeight() / 2 + pauseLayout.height / 2 + 40);
                }
            }

            textBatch.end();
        }
    }

    private void checkGameOver() {
        boolean playersAlive = (player1 != null && player1.isAlive()) ||
                (player2 != null && player2.isAlive());

        boolean enemiesAlive = false;
        for (Tank enemy : enemies) {
            if (enemy != null && enemy.isAlive()) {
                enemiesAlive = true;
                break;
            }
        }

        // Игра заканчивается, если все игроки мертвы
        if (!playersAlive) {
            gameOver = true;
        }
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

    // Метод для подсчета живых врагов
    private int countAliveEnemies() {
        int count = 0;
        for (Tank enemy : enemies) {
            if (enemy != null && enemy.isAlive()) {
                count++;
            }
        }
        return count;
    }

    // Проверка, свободна ли точка появления
    private boolean isSpawnPointClear(int x, int y) {
        // Проверяем коллизию с картой
        if (checkCollisionWithMap(x, y, null)) {
            return false;
        }

        // Проверяем коллизию с игроками
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

        // Проверяем коллизию с другими врагами
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

    // Поиск ближайшей свободной точки
    private int[] findNearestFreeSpot(int startX, int startY) {
        // Проверяем в радиусе 50 пикселей
        for (int radius = 5; radius <= 50; radius += 5) {
            for (int offsetX = -radius; offsetX <= radius; offsetX += 5) {
                for (int offsetY = -radius; offsetY <= radius; offsetY += 5) {
                    int x = startX + offsetX;
                    int y = startY + offsetY;

                    // Проверяем, находится ли точка в пределах карты
                    if (x >= 0 && x <= 454 && y >= 0 && y <= 454) {
                        if (isSpawnPointClear(x, y)) {
                            return new int[]{x, y};
                        }
                    }
                }
            }
        }

        // Если не нашли свободную точку, возвращаем исходную (хотя это может привести к проблемам)
        Gdx.app.error("GameScreen", "Could not find free spawn point near " + startX + ", " + startY);
        return new int[]{startX, startY};
    }

    // Новый метод для проверки и создания новых врагов
    private void checkEnemyRespawn(float delta) {
        // Проверяем, нужно ли создать новых врагов
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

    // Полностью переработанный метод обновления врагов
    private void updateEnemies(float delta) {
        // Перебираем всех врагов
        for (int i = 0; i < enemies.size(); i++) {
            Tank enemy = enemies.get(i);
            if (enemy == null || !enemy.isAlive()) continue;

            // Получаем информацию о движении для этого врага
            if (i >= enemyMovementInfos.size()) {
                // Если информации нет, создаем новую
                enemyMovementInfos.add(new EnemyMovementInfo());
            }
            EnemyMovementInfo info = enemyMovementInfos.get(i);

            // Обновляем таймер изменения направления
            info.directionChangeTimer -= delta;

            // Проверяем, нужно ли изменить направление
            boolean shouldChangeDirection = false;

            // Меняем направление, если:
            // 1. Истек таймер
            if (info.directionChangeTimer <= 0) {
                shouldChangeDirection = true;
            }
            // 2. Танк застрял
            else if (info.isStuck && info.stuckCounter >= STUCK_THRESHOLD) {
                shouldChangeDirection = true;
                info.stuckCounter = 0;
            }
            // 3. Танк прошел достаточное расстояние
            else if (info.movementDistance >= MIN_MOVEMENT_BEFORE_CHANGE && Math.random() < 0.05) {
                shouldChangeDirection = true;
            }

            // Если нужно изменить направление
            if (shouldChangeDirection) {
                // Выбираем новое направление
                chooseNewDirectionForEnemy(enemy, info, i);

                // Сбрасываем счетчики
                info.directionChangeTimer = (float) (Math.random() *
                        (MAX_DIRECTION_CHANGE_TIME - MIN_DIRECTION_CHANGE_TIME) +
                        MIN_DIRECTION_CHANGE_TIME);
                info.movementDistance = 0;
                info.isStuck = false;
                info.stuckCounter = 0;
            }

            // Пробуем двигаться в текущем направлении
            boolean moved = moveEnemyInDirection(enemy, info);

            // Если не удалось двигаться, увеличиваем счетчик застревания
            if (!moved) {
                info.isStuck = true;
                info.stuckCounter++;

                // Если застрял слишком много раз, меняем направление немедленно
                if (info.stuckCounter >= STUCK_THRESHOLD) {
                    chooseNewDirectionForEnemy(enemy, info, i);
                    info.directionChangeTimer = (float) (Math.random() *
                            (MAX_DIRECTION_CHANGE_TIME - MIN_DIRECTION_CHANGE_TIME) +
                            MIN_DIRECTION_CHANGE_TIME);
                    info.movementDistance = 0;
                    info.isStuck = false;
                    info.stuckCounter = 0;
                }
            } else {
                // Если удалось двигаться, увеличиваем счетчик пройденного расстояния
                info.movementDistance++;
                info.isStuck = false;
            }

            // Используем улучшенный AI для стрельбы
            enemy.improveEnemyAI(delta, player1, player2);

            // Добавляем случайную стрельбу
            if (Math.random() < 0.005) { // 0.5% шанс выстрелить
                Bullet bullet = enemy.shoot();
                if (bullet != null) {
                    bullets.add(bullet);
                }
            }
        }
    }

    // Метод для выбора нового направления для врага
    private void chooseNewDirectionForEnemy(Tank enemy, EnemyMovementInfo info, int enemyIndex) {
        // Определяем цель: игрок или база
        boolean targetBase = Math.random() < 0.3; // 30% шанс целиться в базу, 70% в игрока

        Tank targetPlayer = null;
        if (player1 != null && player1.isAlive()) {
            targetPlayer = player1;
        } else if (player2 != null && player2.isAlive()) {
            targetPlayer = player2;
        }

        // Если нет живых игроков, целимся в базу
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

        // Вычисляем расстояние до цели по горизонтали и вертикали
        int dx = targetX - enemy.positionX;
        int dy = targetY - enemy.positionY;

        // Решаем, двигаться ли по горизонтали или вертикали
        // Добавляем случайность для более естественного движения
        boolean moveHorizontally = Math.abs(dx) > Math.abs(dy) || Math.random() < 0.3;

        // Выбираем направление с большей вероятностью в сторону цели
        if (moveHorizontally) {
            // Двигаемся по горизонтали
            if (dx > 0) {
                if (Math.random() < 0.8) { // 80% шанс выбрать правильное направление
                    info.direction = Tank.Direction.RIGHT;
                } else {
                    // 20% шанс выбрать случайное другое направление
                    Tank.Direction[] otherDirections = {
                            Tank.Direction.LEFT, Tank.Direction.FORWARD, Tank.Direction.BACKWARD
                    };
                    info.direction = otherDirections[(int) (Math.random() * otherDirections.length)];
                }
            } else {
                if (Math.random() < 0.8) { // 80% шанс выбрать правильное направление
                    info.direction = Tank.Direction.LEFT;
                } else {
                    // 20% шанс выбрать случайное другое направление
                    Tank.Direction[] otherDirections = {
                            Tank.Direction.RIGHT, Tank.Direction.FORWARD, Tank.Direction.BACKWARD
                    };
                    info.direction = otherDirections[(int) (Math.random() * otherDirections.length)];
                }
            }
        } else {
            // Двигаемся по вертикали (учитываем перевернутую систему координат)
            if (dy > 0) {
                if (Math.random() < 0.8) { // 80% шанс выбрать правильное направление
                    info.direction = Tank.Direction.BACKWARD; // Вниз
                } else {
                    // 20% шанс выбрать случайное другое направление
                    Tank.Direction[] otherDirections = {
                            Tank.Direction.FORWARD, Tank.Direction.LEFT, Tank.Direction.RIGHT
                    };
                    info.direction = otherDirections[(int) (Math.random() * otherDirections.length)];
                }
            } else {
                if (Math.random() < 0.8) { // 80% шанс выбрать правильное направление
                    info.direction = Tank.Direction.FORWARD; // Вверх
                } else {
                    // 20% шанс выбрать случайное другое направление
                    Tank.Direction[] otherDirections = {
                            Tank.Direction.BACKWARD, Tank.Direction.LEFT, Tank.Direction.RIGHT
                    };
                    info.direction = otherDirections[(int) (Math.random() * otherDirections.length)];
                }
            }
        }

        // Устанавливаем выбранное направление для танка
        enemy.setDirection(info.direction);
    }

    private boolean moveEnemyInDirection(Tank enemy, EnemyMovementInfo info) {
        // Определяем новые координаты в зависимости от направления
        int newX = enemy.positionX;
        int newY = enemy.positionY;
        int keycode = -1;

        // Увеличиваем скорость движения (3 пикселя за шаг)
        int moveSpeed = 3;

        switch (info.direction) {
            case FORWARD: // Вверх (в перевернутой системе координат это -Y)
                newY = enemy.positionY - moveSpeed;
                keycode = Input.Keys.UP;
                break;
            case BACKWARD: // Вниз (в перевернутой системе координат это +Y)
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

        // Проверяем, можем ли мы двигаться в этом направлении
        boolean canMove = true;

        // Проверяем границы карты
        if (newX < 0 || newX > 454 - 9 || newY < 0 || newY > 454) {
            canMove = false;
            Gdx.app.log("Collision", "Enemy at " + enemy.positionX + ", " + enemy.positionY + " out of bounds");
        } else {
            // Проверяем коллизии
            if (checkCollisionWithPlayer(enemy, newX, newY)) {
                canMove = false;
                Gdx.app.log("Collision", "Enemy at " + enemy.positionX + ", " + enemy.positionY + " collides with player");
            } else if (checkCollisionWithEnemy(enemy, newX, newY) || checkCollisionWithMap(newX, newY, enemy)) {
                canMove = false;
                Gdx.app.log("Collision", "Enemy at " + enemy.positionX + ", " + enemy.positionY + " collides with something");
            }
        }

        // Если можем двигаться, обновляем позицию
        if (canMove) {
            // ВАЖНО: Фактически перемещаем танк
            enemy.positionX = newX;
            enemy.positionY = newY;

            // Обрабатываем ввод для анимации
            try {
                enemy.handleInput(keycode, stateTime);
            } catch (Exception e) {
                Gdx.app.error("GameScreen", "Error handling input for enemy: " + e.getMessage());
            }

            return true;
        } else {
            // Если не можем двигаться, просто обновляем анимацию
            try {
                enemy.handleInput(-1, stateTime);
            } catch (Exception e) {
                Gdx.app.error("GameScreen", "Error handling input for enemy: " + e.getMessage());
            }

            return false;
        }
    }

    // Обновленный метод создания нового врага с использованием фиксированных точек появления
    private void spawnNewEnemy() {
        // Выбираем одну из трех точек появления случайным образом
        int spawnPointIndex = (int) (Math.random() * SPAWN_POINTS.length);
        int spawnX = SPAWN_POINTS[spawnPointIndex][0];
        int spawnY = SPAWN_POINTS[spawnPointIndex][1];

        // Создаем нового врага
        Tank enemy = new Tank("red", 1, true);

        // Проверяем, свободна ли точка появления
        if (isSpawnPointClear(spawnX, spawnY)) {
            enemy.positionX = spawnX;
            enemy.positionY = spawnY;
        } else {
            // Если точка занята, ищем ближайшую свободную точку
            int[] freeSpawn = findNearestFreeSpot(spawnX, spawnY);
            enemy.positionX = freeSpawn[0];
            enemy.positionY = freeSpawn[1];
        }

        // Устанавливаем начальное направление - вниз (к игроку)
        enemy.setDirection(Tank.Direction.BACKWARD);

        // Добавляем врага в список
        enemies.add(enemy);

        // Создаем информацию о движении для этого врага
        EnemyMovementInfo movementInfo = new EnemyMovementInfo();
        // Выбираем случайное начальное направление
        Tank.Direction[] directions = Tank.Direction.values();
        movementInfo.direction = directions[(int) (Math.random() * directions.length)];
        enemy.setDirection(movementInfo.direction);
        enemyMovementInfos.add(movementInfo);

        // Удаляем мертвых врагов из списка
        cleanupDeadEnemies();

        Gdx.app.log("GameScreen", "Spawned new enemy at " + enemy.positionX + ", " + enemy.positionY +
                ". Alive enemies: " + countAliveEnemies() + ", Remaining: " + remainingEnemies);
    }

    // Метод для удаления мертвых врагов из списка
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

        boolean collides = (player1 != null && player1.isAlive() && enemy.collidesWith(player1)) ||
                (player2 != null && player2.isAlive() && enemy.collidesWith(player2));

        enemy.positionX = oldX;
        enemy.positionY = oldY;

        return collides;
    }

    private void updateBullets(float delta) {
        Iterator<Bullet> iterator = bullets.iterator();
        while (iterator.hasNext()) {
            Bullet bullet = iterator.next();
            if (bullet == null) {
                iterator.remove();
                continue;
            }

            bullet.update(delta);

            // 💥 Проверка столкновения пуль между собой
            for (Bullet other : bullets) {
                if (other != bullet && other.isActive() && bullet.isActive()) {
                    if (bullet.getBounds().overlaps(other.getBounds())) {
                        explosions.add(new Explosion(bullet.getPositionX(), bullet.getPositionY()));
                        if (explosionSound != null) explosionSound.play();
                        bullet.deactivate();
                        other.deactivate();
                        break;
                    }
                }
            }

            // Проверка выхода за границы
            boolean outOfBounds = false;
            float explosionX = bullet.getPositionX();
            float explosionY = bullet.getPositionY();

            if (bullet.getPositionX() >= 480) {
                outOfBounds = true;
                explosionX = 465;
            } else if (bullet.getPositionX() < 0) {
                outOfBounds = true;
                explosionX = 15;
            } else if (bullet.getPositionY() >= 480) {
                outOfBounds = true;
                explosionY = 465;
            } else if (bullet.getPositionY() < 0) {
                outOfBounds = true;
                explosionY = 15;
            }

            if (outOfBounds) {
                explosions.add(new Explosion(explosionX, explosionY));
                if (explosionSound != null) explosionSound.play();
                Gdx.app.log("Bullet", "Пуля вышла за границы карты, создан взрыв на " + explosionX + ", " + explosionY);
                bullet.deactivate();
                bullet.dispose();
                iterator.remove();
                continue;
            }

            // Проверяем столкновения с объектами на карте и танками
            checkBulletCollisions(bullet);

            if (!bullet.isActive()) {
                bullet.dispose();
                iterator.remove();
            }
        }
    }


    private void checkBulletCollisions(Bullet bullet) {
        if (bullet == null || !bullet.isActive()) return;

        Rectangle bulletBounds = bullet.getBounds();

        // 1. Коллизия с твердыми блоками карты
        for (MapTile tile : mapLoader.tiles) {
            if (!tile.isSolid) continue;

            Rectangle tileRect = tile.getBounds(MapLoader.TILE_SIZE, TILE_SCALE, -17, -17);
            if (bulletBounds.overlaps(tileRect)) {
                // Если блок разрушаемый — наносим урон
                if (tile.isDestructible) {
                    tile.takeHit();

                    // Проверка на уничтожение базы
                    if (tile.isBase) {
                        gameOver = true;
                        Gdx.app.log("GameScreen", "База уничтожена! Игра окончена.");
                    }

                    // Повреждаем соседний блок
                    // Повреждаем соседний блок
                    int tx = tile.x;
                    int ty = tile.y;
                    for (MapTile neighbor : mapLoader.tiles) {
                        if (!neighbor.isDestructible || !neighbor.isSolid) continue;

                        boolean isNeighbor =
                                (neighbor.x == tx && Math.abs(neighbor.y - ty) == 1) ||
                                        (neighbor.y == ty && Math.abs(neighbor.x - tx) == 1);

                        if (isNeighbor && !neighbor.isBase) {
                            neighbor.takeHit(); // ← база больше не ломается случайно
                            break;
                        }
                    }
                }

                // Взрыв
                explosions.add(new Explosion(bullet.getPositionX(), bullet.getPositionY()));
                if (explosionSound != null) explosionSound.play();

                bullet.deactivate();
                return;
            }
        }

        // 2. Коллизия с игроками
        if (bullet.isFromEnemy()) {
            if (player1 != null && player1.isAlive() && bulletBounds.overlaps(player1.getBounds())) {
                explosions.add(new Explosion(bullet.getPositionX() - 14, bullet.getPositionY() - 14));
                bullet.deactivate();
                if (player1.takeDamage()) {
                    if (explosionSound != null) explosionSound.play();
                } else {
                    if (hitSound != null) hitSound.play();
                }

                if (player1.isAlive()) {
                    player1.positionX = 152;
                    player1.positionY = 450;
                }
            }

            if (player2 != null && player2.isAlive() && bulletBounds.overlaps(player2.getBounds())) {
                explosions.add(new Explosion(bullet.getPositionX() - 14, bullet.getPositionY() - 14));
                bullet.deactivate();
                if (player2.takeDamage()) {
                    if (explosionSound != null) explosionSound.play();
                } else {
                    if (hitSound != null) hitSound.play();
                }

                if (player2.isAlive()) {
                    player2.positionX = 299;
                    player2.positionY = 450;
                }
            }
        } else {
            // 3. Коллизия с врагами
            for (Tank enemy : enemies) {
                if (enemy != null && enemy.isAlive() && bulletBounds.overlaps(enemy.getBounds())) {
                    explosions.add(new Explosion(bullet.getPositionX() - 14, bullet.getPositionY() - 14));
                    bullet.deactivate();
                    if (enemy.takeDamage()) {
                        if (explosionSound != null) explosionSound.play();
                        score += 100;
                        onEnemyKilled(); // Вызываем метод при уничтожении врага
                    } else {
                        if (hitSound != null) hitSound.play();
                    }
                    break;
                }
            }
        }
    }


    private void updateExplosions(float delta) {
        Iterator<Explosion> iterator = explosions.iterator();
        while (iterator.hasNext()) {
            Explosion explosion = iterator.next();
            explosion.update(delta);
            if (explosion.isFinished()) {
                iterator.remove();
                Gdx.app.log("Explosion", "Взрыв удален из списка");
            }
        }
    }

    private void checkKeyPress() {
// Обработка ввода для первого игрока (желтый танк)
        if (player1 != null && player1.isAlive()) {
            boolean moved = false;
            int movementKeycode = -1;

            // В режиме двух игроков первый игрок использует WASD
            if (playerCount == 2) {
                // Обработка движения для первого игрока (WASD)
                if (Gdx.input.isKeyPressed(Input.Keys.S)) {
                    movementKeycode = Input.Keys.DOWN;
                    int newY = player1.positionY + 3;
                    if (newY <= 454 && !checkCollisionWithTank(player1, player1.positionX, newY) &&
                            !checkCollisionWithEnemy(player1, player1.positionX, newY) &&
                            !checkCollisionWithMap(player1.positionX, newY, player1)) {
                        player1.handleInput(movementKeycode, stateTime);
                        moved = true;
                    }
                } else if (Gdx.input.isKeyPressed(Input.Keys.W)) {
                    movementKeycode = Input.Keys.UP;
                    int newY = player1.positionY - 3;
                    if (newY >= 0 && !checkCollisionWithTank(player1, player1.positionX, newY) &&
                            !checkCollisionWithEnemy(player1, player1.positionX, newY) &&
                            !checkCollisionWithMap(player1.positionX, newY, player1)) {
                        player1.handleInput(movementKeycode, stateTime);
                        moved = true;
                    }
                } else if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                    movementKeycode = Input.Keys.LEFT;
                    int newX = player1.positionX - 3;
                    if (newX >= 0 && !checkCollisionWithTank(player1, newX, player1.positionY) &&
                            !checkCollisionWithEnemy(player1, newX, player1.positionY) &&
                            !checkCollisionWithMap(newX, player1.positionY, player1)) {
                        player1.handleInput(movementKeycode, stateTime);
                        moved = true;
                    }
                } else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                    movementKeycode = Input.Keys.RIGHT;
                    int newX = player1.positionX + 3;
                    if (newX <= 454 - 9 && !checkCollisionWithTank(player1, newX, player1.positionY) &&
                            !checkCollisionWithEnemy(player1, newX, player1.positionY) &&
                            !checkCollisionWithMap(newX, player1.positionY, player1)) {
                        player1.handleInput(movementKeycode, stateTime);
                        moved = true;
                    }
                }
            }
            // В режиме одного игрока первый игрок использует стрелочки
            else {
                // Обработка движения для первого игрока (стрелочки)
                if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
                    movementKeycode = Input.Keys.DOWN;
                    int newY = player1.positionY + 3;
                    if (newY <= 454 && !checkCollisionWithTank(player1, player1.positionX, newY) &&
                            !checkCollisionWithEnemy(player1, player1.positionX, newY) &&
                            !checkCollisionWithMap(player1.positionX, newY, player1)) {
                        player1.handleInput(movementKeycode, stateTime);
                        moved = true;
                    }
                } else if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
                    movementKeycode = Input.Keys.UP;
                    int newY = player1.positionY - 3;
                    if (newY >= 0 && !checkCollisionWithTank(player1, player1.positionX, newY) &&
                            !checkCollisionWithEnemy(player1, player1.positionX, newY) &&
                            !checkCollisionWithMap(player1.positionX, newY, player1)) {
                        player1.handleInput(movementKeycode, stateTime);
                        moved = true;
                    }
                } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
                    movementKeycode = Input.Keys.LEFT;
                    int newX = player1.positionX - 3;
                    if (newX >= 0 && !checkCollisionWithTank(player1, newX, player1.positionY) &&
                            !checkCollisionWithEnemy(player1, newX, player1.positionY) &&
                            !checkCollisionWithMap(newX, player1.positionY, player1)) {
                        player1.handleInput(movementKeycode, stateTime);
                        moved = true;
                    }
                } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
                    movementKeycode = Input.Keys.RIGHT;
                    int newX = player1.positionX + 3;
                    if (newX <= 454 - 9 && !checkCollisionWithTank(player1, newX, player1.positionY) &&
                            !checkCollisionWithEnemy(player1, newX, player1.positionY) &&
                            !checkCollisionWithMap(newX, player1.positionY, player1)) {
                        player1.handleInput(movementKeycode, stateTime);
                        moved = true;
                    }
                }
            }

            // Если не было движения и не нажата клавиша стрельбы, устанавливаем состояние покоя
            if (!moved && !Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
                player1.handleInput(-1, stateTime);
            }

            // Обработка стрельбы для первого игрока (всегда на пробеле)
            if (Gdx.input.isKeyPressed(Input.Keys.SPACE) && player1ShootCooldown <= 0) {
                // Сохраняем текущее направление танка
                Tank.Direction currentDirection = player1.getDirection();

                // Обрабатываем ввод для стрельбы
                player1.handleInput(Input.Keys.SPACE, stateTime);

                // Восстанавливаем направление танка (чтобы стрельба не меняла направление)
                player1.setDirection(currentDirection);

                Bullet bullet = player1.shoot();
                if (bullet != null) {
                    bullets.add(bullet);
                    player1ShootCooldown = SHOOT_COOLDOWN;
                }
            }
        }

        // Обработка ввода для второго игрока (зеленый танк)
        if (player2 != null && player2.isAlive()) {
            boolean moved = false;
            int moveKey = -1;

            if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
                moveKey = Input.Keys.DOWN;
                int newY = player2.positionY + 3;
                if (newY <= 454 && !checkCollisionWithTank(player2, player2.positionX, newY) &&
                        !checkCollisionWithEnemy(player2, player2.positionX, newY) &&
                        !checkCollisionWithMap(player2.positionX, newY, player2)) {
                    player2.handleInput(moveKey, stateTime);
                    moved = true;
                }
            } else if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
                moveKey = Input.Keys.UP;
                int newY = player2.positionY - 3;
                if (newY >= 0 && !checkCollisionWithTank(player2, player2.positionX, newY) &&
                        !checkCollisionWithEnemy(player2, player2.positionX, newY) &&
                        !checkCollisionWithMap(player2.positionX, newY, player2)) {
                    player2.handleInput(moveKey, stateTime);
                    moved = true;
                }
            } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
                moveKey = Input.Keys.LEFT;
                int newX = player2.positionX - 3;
                if (newX >= 0 && !checkCollisionWithTank(player2, newX, player2.positionY) &&
                        !checkCollisionWithEnemy(player2, newX, player2.positionY) &&
                        !checkCollisionWithMap(newX, player2.positionY, player2)) {
                    player2.handleInput(moveKey, stateTime);
                    moved = true;
                }
            } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
                moveKey = Input.Keys.RIGHT;
                int newX = player2.positionX + 3;
                if (newX <= 454 - 9 && !checkCollisionWithTank(player2, newX, player2.positionY) &&
                        !checkCollisionWithEnemy(player2, newX, player2.positionY) &&
                        !checkCollisionWithMap(newX, player2.positionY, player2)) {
                    player2.handleInput(moveKey, stateTime);
                    moved = true;
                }
            }

            // Состояние покоя, если нет движения
            if (!moved && !Gdx.input.isKeyPressed(Input.Keys.ENTER)) {
                player2.handleInput(-1, stateTime);
            }

            // Стрельба по ENTER
            if (Gdx.input.isKeyPressed(Input.Keys.ENTER) && player2ShootCooldown <= 0) {
                Bullet bullet = player2.shoot(); // просто напрямую вызываем
                if (bullet != null) {
                    bullets.add(bullet);
                    player2ShootCooldown = SHOOT_COOLDOWN;
                    Gdx.app.log("SHOOT", "Player2 bullet added");
                }
            }
        }
    }

    public void onEnemyKilled() {
        totalKilledEnemies++; // Увеличиваем счетчик
        Gdx.app.log("GameScreen", "Enemy killed! Total killed: " + totalKilledEnemies);
    }

    boolean checkCollisionWithTank (Tank tank,int newX, int newY){
        if (tank == null) return false;

        int oldX = tank.positionX;
        int oldY = tank.positionY;

        tank.positionX = newX;
        tank.positionY = newY;

        boolean collides = false;

        // Проверяем коллизию с первым игроком, только если он жив
        if (tank == player2 && player1 != null && player1.isAlive() && tank.collidesWith(player1)) {
            collides = true;
        }
        // Проверяем коллизию со вторым игроком, только если он жив
        else if (tank == player1 && player2 != null && player2.isAlive() && tank.collidesWith(player2)) {
            collides = true;
        }

        tank.positionX = oldX;
        tank.positionY = oldY;

        return collides;
    }

    private boolean checkCollisionWithEnemy (Tank tank,int newX, int newY){
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

    private boolean checkCollisionWithMap ( int newX, int newY, Tank tank){
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

    private int[] findFreeSpawnPoint ( int startX, int startY, int step){
        for (int y = startY; y < 480; y += step) {
            for (int x = startX; x < 440; x += step) {
                Rectangle rect = new Rectangle(x, y, 26 / TILE_SCALE, 26 / TILE_SCALE);
                boolean blocked = false;

                // 1. Проверка карты
                for (MapTile tile : mapLoader.tiles) {
                    if (tile.isSolid) {
                        Rectangle tileRect = tile.getBounds(MapLoader.TILE_SIZE, TILE_SCALE, -17, -17);
                        if (rect.overlaps(tileRect)) {
                            blocked = true;
                            break;
                        }
                    }
                }

                // 2. Проверка игрока 1
                if (!blocked && player1 != null) {
                    Rectangle r1 = new Rectangle(player1.positionX, player1.positionY, 26 / TILE_SCALE, 26 / TILE_SCALE);
                    if (rect.overlaps(r1)) {
                        blocked = true;
                    }
                }

                // 3. Проверка игрока 2
                if (!blocked && player2 != null) {
                    Rectangle r2 = new Rectangle(player2.positionX, player2.positionY, 26 / TILE_SCALE, 26 / TILE_SCALE);
                    if (rect.overlaps(r2)) {
                        blocked = true;
                    }
                }

                // 4. Проверка других врагов
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
        return new int[]{50, 50}; // fallback
    }

    private boolean checkCollisionWithEnemy (Tank tank,float newX, float newY){
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

    @Override
    public void show () {
        // Метод вызывается при показе экрана
    }

    @Override
    public void resize ( int width, int height){
        // Метод вызывается при изменении размера окна
    }

    @Override
    public void pause () {
        isPaused = true;
    }

    @Override
    public void resume () {
        // Можно оставить игру на паузе при восстановлении окна
    }

    @Override
    public void hide () {
        // Метод вызывается при скрытии экрана
    }

    @Override
    public void dispose () {
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
    }
}