package com.mg.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
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
import com.mg.game.Bullet;
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
    private float player1ShootCooldown = 0f;
    private float player2ShootCooldown = 0f;
    private static final float SHOOT_COOLDOWN = 0.5f; // Задержка между выстрелами в секундах

    // Переменные для контроля скорости движения
    private float moveTimer = 0f;
    private static final float MOVE_DELAY = 0.01f; // Уменьшенная задержка для более быстрого движения

    // Переменная для контроля движения врагов
    private float enemyMoveTimer = 0f;
    private static final float ENEMY_MOVE_DELAY = 0.01f; // Такая же задержка как у игрока

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

    public GameScreen(gdxGame game, int playerCount) {
        this.playerCount = playerCount;
        this.game = game;
        camera = new OrthographicCamera();
        camera.setToOrtho(true, 640, 480);
        batch = new SpriteBatch();
        textBatch = new SpriteBatch(); // Создаем отдельный SpriteBatch для текста
        stateTime = 0f;
        bullets = new ArrayList<>();
        enemies = new ArrayList<>();
        font = new BitmapFont(true); // Обычный шрифт для интерфейса (перевернутый)

        // Создаем шрифт для больших надписей (НЕ перевернутый)
        largeFont = new BitmapFont(false); // false - не переворачивать
        largeFont.getData().setScale(5f); // Увеличиваем размер в 2.5 раза

        // Загружаем ресурсы для игры
        Assets.loadLevel(1);

        // Загружаем анимации для всех цветов танков перед созданием танков
        Assets.loadTankAnimations("yellow", 1);
        Assets.loadTankAnimations("green", 1);
        Assets.loadTankAnimations("red", 1);

        // Загрузка звуков - обрабатываем ошибки
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
        // Используем конструктор из первого кода, так как он поддерживает isEnemy
        player1 = new Tank("yellow", 1, false);
        player1.positionX = 160;
        player1.positionY = 450;
        Gdx.app.log("GameScreen", "Player 1 color: " + player1.getColour());

        // Инициализация второго танка, если выбран режим на 2 игрока
        if (playerCount == 2) {
            player2 = new Tank("green", 1, false);
            player2.positionX = 290;
            player2.positionY = 450;
            Gdx.app.log("GameScreen", "Player 2 color: " + player2.getColour());
        }

        // Инициализация вражеских танков (например, 3 врага)
        // сначала загружаем карту
        mapLoader = new MapLoader();
        for (int i = 0; i < 3; i++) {
            Tank enemy = new Tank("red", 1, true);
            int[] spawn = findFreeSpawnPoint(50 + i * 60, 50, 16); // шаг 16 пикселей
            enemy.positionX = spawn[0];
            enemy.positionY = spawn[1];
            enemies.add(enemy);
        }


        // Инициализация карты
        mapLoader = new MapLoader();

        try {
            Music levelSound = Gdx.audio.newMusic(Gdx.files.internal("sounds/startLevel.mp3"));
            levelSound.play();
        } catch (Exception e) {
            Gdx.app.error("GameScreen", "Error playing level sound: " + e.getMessage());
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(192f / 255, 192f / 255, 192f / 255, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        camera.update();
        stateTime += Gdx.graphics.getDeltaTime();

        // Проверяем нажатие клавиши паузы (P или ESC)
        if (Gdx.input.isKeyJustPressed(Input.Keys.P) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            isPaused = !isPaused; // Переключаем состояние паузы
        }

        // Проверяем, не закончилась ли игра
        checkGameOver();

        // Обновляем игру только если она не на паузе и не окончена
        if (!gameOver && !isPaused) {
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

            // Обновляем снаряды и проверяем попадания
            updateBullets(delta);

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

        // 2. Карта
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

        // 3. Танки
        if (player1 != null && player1.isAlive()) {
            TextureRegion frame1 = player1.getCurrentFrame();
            if (frame1 != null) {
                // Если танк неуязвим, рисуем его мигающим
                if (!player1.isInvulnerable() || (int)(stateTime * 10) % 2 == 0) {
                    batch.draw(frame1, player1.positionX, player1.positionY, 26 / TILE_SCALE, 26 / TILE_SCALE);
                }
            }
        }

        if (player2 != null && player2.isAlive()) {
            TextureRegion frame2 = player2.getCurrentFrame();
            if (frame2 != null) {
                // Если танк неуязвим, рисуем его мигающим
                if (!player2.isInvulnerable() || (int)(stateTime * 10) % 2 == 0) {
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
                batch.draw(bullet.getTexture(), bullet.getPositionX(), bullet.getPositionY(), 4, 4);
            }
        }

        // Отображаем счет и здоровье
        font.setColor(Color.BLACK);
        font.draw(batch, "Score: " + score, 500, 50);
        int aliveEnemies = 0;
        for (Tank enemy : enemies) {
            if (enemy.isAlive()) aliveEnemies++;
        }

        font.draw(batch, "Enemies: ", 500, 140);
        if (Assets.enemyIcon != null) {
            int enemyIndex = 0;
            for (Tank enemy : enemies) {
                if (enemy.isAlive()) {
                    batch.draw(Assets.enemyIcon, 500 + (enemyIndex * 20), 160, 16, 16);
                    enemyIndex++;
                }
            }
        }
        if (Assets.healthIcon != null && player1 != null) {
            font.draw(batch, "P1: Health ", 500, 190);
            for (int i = 0; i < player1.getHealth(); i++) {
                batch.draw(Assets.healthIcon, 500 + i * 20, 210, 16, 16);
            }
        }
        if (Assets.healthIcon != null && player2 != null) {
            font.draw(batch, "P2: Health ", 500, 230);
            for (int i = 0; i < player2.getHealth(); i++) {
                batch.draw(Assets.healthIcon, 500 + i * 20, 250, 16, 16); // P2
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
                            Gdx.graphics.getWidth()/2 - Assets.gameOverTexture.getWidth()/2,
                            Gdx.graphics.getHeight()/2 - Assets.gameOverTexture.getHeight()/2);
                } else {
                    largeFont.setColor(Color.RED);
                    GlyphLayout gameOverLayout = new GlyphLayout(largeFont, "GAME OVER");
                    largeFont.draw(textBatch, "GAME OVER",
                            Gdx.graphics.getWidth()/2 - gameOverLayout.width/2,
                            Gdx.graphics.getHeight()/2 + gameOverLayout.height/2);

                    GlyphLayout restartLayout = new GlyphLayout(largeFont, "Нажмите ENTER для перезапуска");
                    largeFont.setColor(Color.WHITE);
                    largeFont.draw(textBatch, "Нажмите ENTER для перезапуска",
                            Gdx.graphics.getWidth()/2 - restartLayout.width/2,
                            Gdx.graphics.getHeight()/2 + gameOverLayout.height/2 + 40);
                }
            } else if (isPaused) {
                if (Assets.pauseTexture != null) {
                    textBatch.draw(Assets.pauseTexture,
                            Gdx.graphics.getWidth()/2 - Assets.pauseTexture.getWidth()/2,
                            Gdx.graphics.getHeight()/2 - Assets.pauseTexture.getHeight()/2);
                } else {
                    largeFont.setColor(Color.YELLOW);
                    GlyphLayout pauseLayout = new GlyphLayout(largeFont, "PAUSE");
                    largeFont.draw(textBatch, "PAUSE",
                            Gdx.graphics.getWidth()/2 - pauseLayout.width/2,
                            Gdx.graphics.getHeight()/2 + pauseLayout.height/2);

                    GlyphLayout continueLayout = new GlyphLayout(largeFont, "Нажмите P или ESC для продолжения");
                    largeFont.setColor(Color.WHITE);
                    largeFont.draw(textBatch, "Нажмите P или ESC для продолжения",
                            Gdx.graphics.getWidth()/2 - continueLayout.width/2,
                            Gdx.graphics.getHeight()/2 + pauseLayout.height/2 + 40);
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

        if (!playersAlive || !enemiesAlive) {
            gameOver = true;
        }
    }

    private void updateEnemies(float delta) {
        for (Tank enemy : enemies) {
            if (enemy == null || !enemy.isAlive()) continue;

            Bullet bullet = enemy.updateEnemy(delta, player1, player2);
            if (bullet != null) {
                bullets.add(bullet);
            }

            int newX = enemy.positionX;
            int newY = enemy.positionY;
            int keycode = -1;

            switch (enemy.getDirection()) {
                case FORWARD:
                    newY = enemy.positionY + 1;
                    keycode = Input.Keys.DOWN;
                    break;
                case BACKWARD:
                    newY = enemy.positionY - 1;
                    keycode = Input.Keys.UP;
                    break;
                case LEFT:
                    newX = enemy.positionX - 1;
                    keycode = Input.Keys.LEFT;
                    break;
                case RIGHT:
                    newX = enemy.positionX + 1;
                    keycode = Input.Keys.RIGHT;
                    break;
            }

            boolean canMove = true;
            if (newY >= 0 && newY <= 454 && newX >= 0 && newX <= 454) {
                if (checkPlayerCollision(enemy, newX, newY)) {
                    canMove = false;
                    Gdx.app.log("Collision", "Enemy at " + enemy.positionX + ", " + enemy.positionY + " collides with player");
                } else if (checkEnemyCollision(enemy, newY, newX) || checkMapCollision(newX, newY, enemy)) {
                    canMove = false;
                    Gdx.app.log("Collision", "Enemy at " + enemy.positionX + ", " + enemy.positionY + " collides with something");
                }
            } else {
                canMove = false;
                Gdx.app.log("Collision", "Enemy at " + enemy.positionX + ", " + enemy.positionY + " out of bounds");
            }

            if (canMove) {
                Gdx.app.log("Movement", "Enemy moving to " + newX + ", " + newY);
                switch (enemy.getDirection()) {
                    case FORWARD:
                        enemy.moveDown();
                        break;
                    case BACKWARD:
                        enemy.moveUp();
                        break;
                    case LEFT:
                        enemy.moveLeft();
                        break;
                    case RIGHT:
                        enemy.moveRight();
                        break;
                }
                try {
                    enemy.handleInput(keycode, stateTime);
                } catch (Exception e) {
                    Gdx.app.error("GameScreen", "Error handling input for enemy: " + e.getMessage());
                }
            } else {
                enemy.chooseRandomDirection();
                try {
                    enemy.handleInput(-1, stateTime);
                } catch (Exception e) {
                    Gdx.app.error("GameScreen", "Error handling input for enemy: " + e.getMessage());
                }
            }
        }
    }

    private boolean checkPlayerCollision(Tank enemy, int newX, int newY) {
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
            if (tile.isSolid) {
                Rectangle tileRect = tile.getBounds(MapLoader.TILE_SIZE, TILE_SCALE, -17, -17);
                if (bulletBounds.overlaps(tileRect)) {
                    bullet.deactivate();
                    return; // пуля пропадает — дальше не проверяем
                }
            }
        }

        // 2. Коллизия с игроками
        if (bullet.isFromEnemy()) {
            if (player1 != null && player1.isAlive() && bulletBounds.overlaps(player1.getBounds())) {
                bullet.deactivate();
                if (player1.takeDamage()) {
                    if (explosionSound != null) explosionSound.play();
                } else {
                    if (hitSound != null) hitSound.play();
                }
            }

            if (player2 != null && player2.isAlive() && bulletBounds.overlaps(player2.getBounds())) {
                bullet.deactivate();
                if (player2.takeDamage()) {
                    if (explosionSound != null) explosionSound.play();
                } else {
                    if (hitSound != null) hitSound.play();
                }
            }
        } else {
            // 3. Коллизия с врагами
            for (Tank enemy : enemies) {
                if (enemy != null && enemy.isAlive() && bulletBounds.overlaps(enemy.getBounds())) {
                    bullet.deactivate();
                    if (enemy.takeDamage()) {
                        if (explosionSound != null) explosionSound.play();
                        score += 100;
                    } else {
                        if (hitSound != null) hitSound.play();
                    }
                    break;
                }
            }
        }
    }

    private void checkKeyPress() {
        if (player1 == null || !player1.isAlive()) return;

        int keycode1 = -1;
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            keycode1 = Input.Keys.DOWN;
            int newY = player1.positionY + 1;
            if (newY <= 454 && !checkPlayerTankCollision(player1, newY, player1.positionX) && !checkPlayerEnemyCollision(player1, newY, player1.positionX) && !checkMapCollision(player1.positionX, newY, player1)) {
                player1.handleInput(keycode1, stateTime);
            }
        } else if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            keycode1 = Input.Keys.UP;
            int newY = player1.positionY - 1;
            if (newY >= 0 && !checkPlayerTankCollision(player1, newY, player1.positionX) && !checkPlayerEnemyCollision(player1, newY, player1.positionX) && !checkMapCollision(player1.positionX, newY, player1)) {
                player1.handleInput(keycode1, stateTime);
            }
        } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            keycode1 = Input.Keys.LEFT;
            int newX = player1.positionX - 1;
            if (newX >= 0 && !checkPlayerTankCollision(player1, player1.positionY, newX) && !checkPlayerEnemyCollision(player1, player1.positionY, newX) && !checkMapCollision(newX, player1.positionY, player1)) {
                player1.handleInput(keycode1, stateTime);
            }
        } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            keycode1 = Input.Keys.RIGHT;
            int newX = player1.positionX + 1;
            if (newX <= 454 && !checkPlayerTankCollision(player1, player1.positionY, newX) && !checkPlayerEnemyCollision(player1, player1.positionY, newX) && !checkMapCollision(newX, player1.positionY, player1)) {
                player1.handleInput(keycode1, stateTime);
            }
        } else if (Gdx.input.isKeyPressed(Input.Keys.SPACE) && player1ShootCooldown <= 0) {
            keycode1 = Input.Keys.SPACE;
            player1.handleInput(keycode1, stateTime);
            Bullet bullet = player1.shoot();
            if (bullet != null) {
                bullets.add(bullet);
                player1ShootCooldown = SHOOT_COOLDOWN;
            }
        } else {
            player1.handleInput(-1, stateTime);
        }

        if (player2 != null && player2.isAlive()) {
            int keycode2 = -1;
            if (Gdx.input.isKeyPressed(Input.Keys.S)) {
                keycode2 = Input.Keys.DOWN;
                int newY = player2.positionY + 1;
                if (newY <= 454
                        && !checkPlayerTankCollision(player2, newY, player2.positionX)
                        && !checkPlayerEnemyCollision(player2, newY, player2.positionX)
                        && !checkMapCollision(player2.positionX, newY, player2)) {
                    player2.handleInput(keycode2, stateTime);
                }
            } else if (Gdx.input.isKeyPressed(Input.Keys.W)) {
                keycode2 = Input.Keys.UP;
                int newY = player2.positionY - 1;
                if (newY >= 0
                        && !checkPlayerTankCollision(player2, newY, player2.positionX)
                        && !checkPlayerEnemyCollision(player2, newY, player2.positionX)
                        && !checkMapCollision(player2.positionX, newY, player2)) {
                    player2.handleInput(keycode2, stateTime);
                }
            } else if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                keycode2 = Input.Keys.LEFT;
                int newX = player2.positionX - 1;
                if (newX >= 0
                        && !checkPlayerTankCollision(player2, player2.positionY, newX)
                        && !checkPlayerEnemyCollision(player2, player2.positionY, newX)
                        && !checkMapCollision(newX, player2.positionY, player2)) {
                    player2.handleInput(keycode2, stateTime);
                }
            } else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                keycode2 = Input.Keys.RIGHT;
                int newX = player2.positionX + 1;
                if (newX <= 454
                        && !checkPlayerTankCollision(player2, player2.positionY, newX)
                        && !checkPlayerEnemyCollision(player2, player2.positionY, newX)
                        && !checkMapCollision(newX, player2.positionY, player2)) {
                    player2.handleInput(keycode2, stateTime);
                }
            } else if (Gdx.input.isKeyPressed(Input.Keys.ENTER) && player2ShootCooldown <= 0) {
                keycode2 = Input.Keys.SPACE;
                player2.handleInput(keycode2, stateTime);
                Bullet bullet = player2.shoot();
                if (bullet != null) {
                    bullets.add(bullet);
                    player2ShootCooldown = SHOOT_COOLDOWN;
                }
            } else {
                player2.handleInput(-1, stateTime);
            }

        }
    }

    private boolean checkPlayerTankCollision(Tank tank, float newY, float newX) {
        if (tank == null) return false;

        float oldX = tank.positionX;
        float oldY = tank.positionY;

        tank.positionX = (int) newX;
        tank.positionY = (int) newY;

        boolean collides = false;

        if (tank == player1 && player2 != null && player2.isAlive() && tank.collidesWith(player2)) {
            collides = true;
        } else if (tank == player2 && player1 != null && player1.isAlive() && tank.collidesWith(player1)) {
            collides = true;
        }

        tank.positionX = (int) oldX;
        tank.positionY = (int) oldY;

        return collides;
    }

    private boolean checkPlayerEnemyCollision(Tank tank, float newY, float newX) {
        if (tank == null) return false;

        float oldX = tank.positionX;
        float oldY = tank.positionY;

        tank.positionX = (int) newX;
        tank.positionY = (int) newY;

        boolean collides = false;
        for (Tank enemy : enemies) {
            if (enemy != null && enemy.isAlive() && tank.collidesWith(enemy)) {
                collides = true;
                break;
            }
        }

        tank.positionX = (int) oldX;
        tank.positionY = (int) oldY;

        return collides;
    }

    private boolean checkMapCollision(float newX, float newY, Tank tank) {
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
                        Rectangle r = new Rectangle(enemy.positionX, enemy.positionY, 26 / TILE_SCALE, 26 / TILE_SCALE);
                        if (rect.overlaps(r)) {
                            blocked = true;
                            break;
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

    private boolean checkEnemyCollision(Tank tank, float newY, float newX) {
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
    public void pause() {
        isPaused = true;
    }

    @Override
    public void resume() {
        // Можно оставить игру на паузе при восстановлении окна
    }

    @Override
    public void show() {}

    @Override
    public void hide() {}

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
    }

    @Override
    public void resize(int width, int height) {}
}