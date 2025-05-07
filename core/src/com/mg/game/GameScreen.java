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

    // Добавляем переменные для контроля скорости движения
    private float moveTimer = 0f;
    private static final float MOVE_DELAY = 0.01f; // Уменьшенная задержка для более быстрого движения

    // Добавляем переменную для контроля движения врагов
    private float enemyMoveTimer = 0f;
    private static final float ENEMY_MOVE_DELAY = 0.01f; // Такая же задержка как у игрока

    // Добавляем переменные для отслеживания состояния игры
    private int score = 0;
    private boolean gameOver = false;
    private BitmapFont font;
    private BitmapFont largeFont;
    private Sound explosionSound;
    private Sound hitSound;

    // Добавляем переменную для отслеживания состояния паузы
    private boolean isPaused = false;

    public GameScreen(gdxGame game, int playerCount) {
        this.playerCount = playerCount;
        this.game = game;
        camera = new OrthographicCamera();
        camera.setToOrtho(true, 640, 480);
        batch = new SpriteBatch();
        textBatch = new SpriteBatch(); // Создаем отдельный SpriteBatch для текста
        stateTime = 0F;
        bullets = new ArrayList<>();
        enemies = new ArrayList<>();
        font = new BitmapFont(true); // Обычный шрифт для интерфейса (перевернутый)

        // Создаем шрифт для больших надписей (НЕ перевернутый)
        largeFont = new BitmapFont(false); // false - не переворачивать
        largeFont.getData().setScale(5f); // Увеличиваем размер в 2.5 раза

        // Загружаем ресурсы для игры
        Assets.loadLevel(1);

        // ВАЖНО: Загружаем анимации для всех цветов танков перед созданием танков
        // Загружаем анимации для каждого цвета отдельно
        Assets.loadTankAnimations("yellow", 1);
        Assets.loadTankAnimations("green", 1);
        Assets.loadTankAnimations("red", 1);

        // Загрузка звуков - обрабатываем ошибки
        try {
            // Пробуем загрузить звуки, но не останавливаемся, если их нет
            try {
                explosionSound = Gdx.audio.newSound(Gdx.files.internal("sounds/explosion.mp3"));
            } catch (Exception e) {
                Gdx.app.error("GameScreen", "Error loading explosion sound: " + e.getMessage());
                // Создаем пустой звук, чтобы избежать NullPointerException
                explosionSound = null;
            }

            try {
                hitSound = Gdx.audio.newSound(Gdx.files.internal("sounds/hit.mp3"));
            } catch (Exception e) {
                Gdx.app.error("GameScreen", "Error loading hit sound: " + e.getMessage());
                // Создаем пустой звук, чтобы избежать NullPointerException
                hitSound = null;
            }
        } catch (Exception e) {
            Gdx.app.error("GameScreen", "Error loading sounds: " + e.getMessage());
        }

        // Инициализация первого танка
        player1 = new Tank("yellow", 1, false);
        player1.positionX = 50;
        player1.positionY = 50;
        Gdx.app.log("GameScreen", "Player 1 color: " + player1.getColour());

        // Инициализация второго танка, если выбран режим на 2 игрока
        if (playerCount == 2) {
            player2 = new Tank("green", 1, false);
            player2.positionX = 400;
            player2.positionY = 400;
            Gdx.app.log("GameScreen", "Player 2 color: " + player2.getColour());
        }

        // Инициализация вражеских танков (например, 3 врага)
        for (int i = 0; i < 3; i++) {
            Tank enemy = new Tank("red", 1, true);
            enemy.positionX = 100 + i * 150;
            enemy.positionY = 100 + i * 100;
            enemies.add(enemy);
        }

        try {
            Music levelSound = Gdx.audio.newMusic(Gdx.files.internal("sounds/startLevel.mp3"));
            levelSound.play();
        } catch (Exception e) {
            Gdx.app.error("GameScreen", "Error playing level sound: " + e.getMessage());
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor((float)192/255, (float)192/255, (float)192/255, 1);
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
        batch.draw(Assets.levelBack, 0, 0, 480, 480);

        // Рисуем первый танк, если он жив
        if (player1 != null && player1.isAlive()) {
            TextureRegion frame1 = player1.getCurrentFrame();
            if (frame1 != null) {
                // Если танк неуязвим, рисуем его мигающим
                if (!player1.isInvulnerable() || (int)(stateTime * 10) % 2 == 0) {
                    batch.draw(frame1, player1.positionX, player1.positionY, 26, 26);
                }
            }
        }

        // Рисуем второй танк, если он существует и жив
        if (player2 != null && player2.isAlive()) {
            TextureRegion frame2 = player2.getCurrentFrame();
            if (frame2 != null) {
                // Если танк неуязвим, рисуем его мигающим
                if (!player2.isInvulnerable() || (int)(stateTime * 10) % 2 == 0) {
                    batch.draw(frame2, player2.positionX, player2.positionY, 26, 26);
                }
            }
        }

        // Рисуем врагов, которые живы
        for (Tank enemy : enemies) {
            if (enemy != null && enemy.isAlive()) {
                TextureRegion enemyFrame = enemy.getCurrentFrame();
                if (enemyFrame != null) {
                    batch.draw(enemyFrame, enemy.positionX, enemy.positionY, 26, 26);
                }
            }
        }

        // Рисуем снаряды
        for (Bullet bullet : bullets) {
            if (bullet != null && bullet.isActive() && bullet.getTexture() != null) {
                batch.draw(bullet.getTexture(), bullet.getPositionX(), bullet.getPositionY(), 4, 4);
            }
        }

        // Отображаем счет и здоровье
        font.setColor(Color.BLACK);
        font.draw(batch, "Score: " + score, 500, 50);
        if (player1 != null) {
            font.draw(batch, "Health: " + player1.getHealth(), 500, 80);
        }

        batch.end();

        // Отдельная отрисовка для текста PAUSE и GAME OVER
        if (gameOver || isPaused) {
            // Настраиваем отдельный batch для текста (не перевернутый)
            textBatch.begin();

            // Создаем матрицу для переворота текста по вертикали
            Matrix4 normalMatrix = new Matrix4();
            normalMatrix.setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            textBatch.setProjectionMatrix(normalMatrix);

            if (gameOver) {
                if (Assets.gameOverTexture != null) {
                    // Рисуем текстуру GAME OVER
                    textBatch.draw(Assets.gameOverTexture,
                            Gdx.graphics.getWidth()/2 - Assets.gameOverTexture.getWidth()/2,
                            Gdx.graphics.getHeight()/2 - Assets.gameOverTexture.getHeight()/2);
                } else {
                    // Рисуем текст GAME OVER
                    largeFont.setColor(Color.RED);

                    // Используем GlyphLayout для центрирования текста
                    GlyphLayout gameOverLayout = new GlyphLayout(largeFont, "GAME OVER");
                    largeFont.draw(textBatch, "GAME OVER",
                            Gdx.graphics.getWidth()/2 - gameOverLayout.width/2,
                            Gdx.graphics.getHeight()/2 + gameOverLayout.height/2);

                    // Рисуем подсказку для перезапуска
                    GlyphLayout restartLayout = new GlyphLayout(largeFont, "Нажмите ENTER для перезапуска");
                    largeFont.setColor(Color.WHITE);
                    largeFont.draw(textBatch, "Нажмите ENTER для перезапуска",
                            Gdx.graphics.getWidth()/2 - restartLayout.width/2,
                            Gdx.graphics.getHeight()/2 + gameOverLayout.height/2 + 40);
                }
            } else if (isPaused) {
                if (Assets.pauseTexture != null) {
                    // Рисуем текстуру PAUSE
                    textBatch.draw(Assets.pauseTexture,
                            Gdx.graphics.getWidth()/2 - Assets.pauseTexture.getWidth()/2,
                            Gdx.graphics.getHeight()/2 - Assets.pauseTexture.getHeight()/2);
                } else {
                    // Рисуем текст PAUSE
                    largeFont.setColor(Color.YELLOW);

                    // Используем GlyphLayout для центрирования текста
                    GlyphLayout pauseLayout = new GlyphLayout(largeFont, "PAUSE");
                    largeFont.draw(textBatch, "PAUSE",
                            Gdx.graphics.getWidth()/2 - pauseLayout.width/2,
                            Gdx.graphics.getHeight()/2 + pauseLayout.height/2);

                    // Рисуем подсказку для продолжения
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
        // Проверяем, живы ли игроки
        boolean playersAlive = (player1 != null && player1.isAlive()) ||
                (player2 != null && player2.isAlive());

        // Проверяем, остались ли враги
        boolean enemiesAlive = false;
        for (Tank enemy : enemies) {
            if (enemy != null && enemy.isAlive()) {
                enemiesAlive = true;
                break;
            }
        }

        // Если все игроки мертвы или все враги мертвы, игра окончена
        if (!playersAlive || !enemiesAlive) {
            gameOver = true;
        }
    }

    private void updateEnemies(float delta) {
        for (Tank enemy : enemies) {
            if (enemy == null || !enemy.isAlive()) continue;

            // Обновляем состояние врага (выбор направления и стрельба)
            Bullet bullet = enemy.updateEnemy(delta, player1, player2);
            if (bullet != null) {
                bullets.add(bullet);
            }

            // Проверяем столкновение перед движением врага
            int newX = enemy.positionX;
            int newY = enemy.positionY;
            int keycode = -1;

            // Определяем направление движения
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

            // Проверяем, может ли враг двигаться
            boolean canMove = true;
            if (newY >= 0 && newY <= 454 && newX >= 0 && newX <= 454) {
                // Проверяем столкновение с игроками
                if (checkPlayerCollision(enemy, newX, newY)) {
                    canMove = false;
                    Gdx.app.log("Collision", "Enemy at " + enemy.positionX + ", " + enemy.positionY + " collides with player");
                }
                // Проверяем столкновение с другими объектами
                else if (checkEnemyCollision(enemy, newY, newX)) {
                    canMove = false;
                    Gdx.app.log("Collision", "Enemy at " + enemy.positionX + ", " + enemy.positionY + " collides with something");
                }
            } else {
                canMove = false;
                Gdx.app.log("Collision", "Enemy at " + enemy.positionX + ", " + enemy.positionY + " out of bounds");
            }

            // Если враг может двигаться, выполняем движение и обновляем анимацию
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
                    enemy.handleInput(keycode, stateTime); // Обновляем анимацию
                } catch (Exception e) {
                    Gdx.app.error("GameScreen", "Error handling input for enemy: " + e.getMessage());
                }
            } else {
                enemy.chooseRandomDirection(); // Если столкновение, меняем направление
                try {
                    enemy.handleInput(-1, stateTime); // Переключаем на состояние покоя
                } catch (Exception e) {
                    Gdx.app.error("GameScreen", "Error handling input for enemy: " + e.getMessage());
                }
            }
        }
    }

    // Метод для проверки столкновения с игроками
    private boolean checkPlayerCollision(Tank enemy, int newX, int newY) {
        if (enemy == null) return false;

        // Сохраняем текущие координаты
        int oldX = enemy.positionX;
        int oldY = enemy.positionY;

        // Устанавливаем новые координаты для проверки
        enemy.positionX = newX;
        enemy.positionY = newY;

        // Проверяем столкновение с игроками
        boolean collides = (player1 != null && player1.isAlive() && enemy.collidesWith(player1)) ||
                (player2 != null && player2.isAlive() && enemy.collidesWith(player2));

        // Восстанавливаем старые координаты
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

            // Проверяем попадания в танки
            checkBulletCollisions(bullet);

            if (!bullet.isActive()) {
                bullet.dispose();
                iterator.remove();
            }
        }
    }

    private void checkBulletCollisions(Bullet bullet) {
        if (bullet == null || !bullet.isActive()) return;

        // Получаем границы снаряда
        Rectangle bulletBounds = bullet.getBounds();

        // Проверяем попадание в игроков
        if (bullet.isFromEnemy()) {
            // Снаряд от врага - проверяем попадание в игроков
            if (player1 != null && player1.isAlive() && bulletBounds.overlaps(player1.getBounds())) {
                bullet.deactivate();
                if (player1.takeDamage()) {
                    // Танк уничтожен
                    if (explosionSound != null) {
                        explosionSound.play();
                    }
                } else {
                    // Танк получил урон
                    if (hitSound != null) {
                        hitSound.play();
                    }
                }
            }

            if (player2 != null && player2.isAlive() && bulletBounds.overlaps(player2.getBounds())) {
                bullet.deactivate();
                if (player2.takeDamage()) {
                    // Танк уничтожен
                    if (explosionSound != null) {
                        explosionSound.play();
                    }
                } else {
                    // Танк получил урон
                    if (hitSound != null) {
                        hitSound.play();
                    }
                }
            }
        } else {
            // Снаряд от игрока - проверяем попадание во врагов
            for (Tank enemy : enemies) {
                if (enemy != null && enemy.isAlive() && bulletBounds.overlaps(enemy.getBounds())) {
                    bullet.deactivate();
                    if (enemy.takeDamage()) {
                        // Враг уничтожен
                        if (explosionSound != null) {
                            explosionSound.play();
                        }
                        score += 100; // Увеличиваем счет
                    } else {
                        // Враг получил урон
                        if (hitSound != null) {
                            hitSound.play();
                        }
                    }
                    break;
                }
            }
        }
    }

    private void checkKeyPress() {
        // Управление первым танком (стрелки + Space для стрельбы)
        if (player1 == null || !player1.isAlive()) return;

        int keycode1 = -1;
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            keycode1 = Input.Keys.DOWN;
            int newY = player1.positionY + 1;
            if (newY <= 454 && !checkPlayerTankCollision(player1, newY, player1.positionX) && !checkPlayerEnemyCollision(player1, newY, player1.positionX)) {
                try {
                    player1.handleInput(keycode1, stateTime);
                } catch (Exception e) {
                    Gdx.app.error("GameScreen", "Error handling input for player1: " + e.getMessage());
                }
            }
        } else if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            keycode1 = Input.Keys.UP;
            int newY = player1.positionY - 1;
            if (newY >= 0 && !checkPlayerTankCollision(player1, newY, player1.positionX) && !checkPlayerEnemyCollision(player1, newY, player1.positionX)) {
                try {
                    player1.handleInput(keycode1, stateTime);
                } catch (Exception e) {
                    Gdx.app.error("GameScreen", "Error handling input for player1: " + e.getMessage());
                }
            }
        } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            keycode1 = Input.Keys.LEFT;
            int newX = player1.positionX - 1;
            if (newX >= 0 && !checkPlayerTankCollision(player1, player1.positionY, newX) && !checkPlayerEnemyCollision(player1, player1.positionY, newX)) {
                try {
                    player1.handleInput(keycode1, stateTime);
                } catch (Exception e) {
                    Gdx.app.error("GameScreen", "Error handling input for player1: " + e.getMessage());
                }
            }
        } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            keycode1 = Input.Keys.RIGHT;
            int newX = player1.positionX + 1;
            if (newX <= 454 && !checkPlayerTankCollision(player1, player1.positionY, newX) && !checkPlayerEnemyCollision(player1, player1.positionY, newX)) {
                try {
                    player1.handleInput(keycode1, stateTime);
                } catch (Exception e) {
                    Gdx.app.error("GameScreen", "Error handling input for player1: " + e.getMessage());
                }
            }
        } else if (Gdx.input.isKeyPressed(Input.Keys.SPACE) && player1ShootCooldown <= 0) {
            keycode1 = Input.Keys.SPACE;
            try {
                player1.handleInput(keycode1, stateTime);
                Bullet bullet = player1.shoot();
                if (bullet != null) {
                    bullets.add(bullet);
                    player1ShootCooldown = SHOOT_COOLDOWN;
                }
            } catch (Exception e) {
                Gdx.app.error("GameScreen", "Error handling input for player1: " + e.getMessage());
            }
        } else {
            try {
                player1.handleInput(-1, stateTime);
            } catch (Exception e) {
                Gdx.app.error("GameScreen", "Error handling input for player1: " + e.getMessage());
            }
        }

        // Управление вторым танком (WASD + Enter для стрельбы), если он существует
        if (player2 != null && player2.isAlive()) {
            int keycode2 = -1;
            if (Gdx.input.isKeyPressed(Input.Keys.S)) {
                keycode2 = Input.Keys.DOWN;
                int newY = player2.positionY + 1;
                if (newY <= 454 && !checkPlayerTankCollision(player2, newY, player2.positionX) && !checkPlayerEnemyCollision(player2, newY, player2.positionX)) {
                    try {
                        player2.handleInput(keycode2, stateTime);
                    } catch (Exception e) {
                        Gdx.app.error("GameScreen", "Error handling input for player2: " + e.getMessage());
                    }
                }
            } else if (Gdx.input.isKeyPressed(Input.Keys.W)) {
                keycode2 = Input.Keys.UP;
                int newY = player2.positionY - 1;
                if (newY >= 0 && !checkPlayerTankCollision(player2, newY, player2.positionX) && !checkPlayerEnemyCollision(player2, newY, player2.positionX)) {
                    try {
                        player2.handleInput(keycode2, stateTime);
                    } catch (Exception e) {
                        Gdx.app.error("GameScreen", "Error handling input for player2: " + e.getMessage());
                    }
                }
            } else if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                keycode2 = Input.Keys.LEFT;
                int newX = player2.positionX - 1;
                if (newX >= 0 && !checkPlayerTankCollision(player2, player2.positionY, newX) && !checkPlayerEnemyCollision(player2, player2.positionY, newX)) {
                    try {
                        player2.handleInput(keycode2, stateTime);
                    } catch (Exception e) {
                        Gdx.app.error("GameScreen", "Error handling input for player2: " + e.getMessage());
                    }
                }
            } else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                keycode2 = Input.Keys.RIGHT;
                int newX = player2.positionX + 1;
                if (newX <= 454 && !checkPlayerTankCollision(player2, player2.positionY, newX) && !checkPlayerEnemyCollision(player2, player2.positionY, newX)) {
                    try {
                        player2.handleInput(keycode2, stateTime);
                    } catch (Exception e) {
                        Gdx.app.error("GameScreen", "Error handling input for player2: " + e.getMessage());
                    }
                }
            } else if (Gdx.input.isKeyPressed(Input.Keys.ENTER) && player2ShootCooldown <= 0) {
                keycode2 = Input.Keys.SPACE;
                try {
                    player2.handleInput(keycode2, stateTime);
                    Bullet bullet = player2.shoot();
                    if (bullet != null) {
                        bullets.add(bullet);
                        player2ShootCooldown = SHOOT_COOLDOWN;
                    }
                } catch (Exception e) {
                    Gdx.app.error("GameScreen", "Error handling input for player2: " + e.getMessage());
                }
            } else {
                try {
                    player2.handleInput(-1, stateTime);
                } catch (Exception e) {
                    Gdx.app.error("GameScreen", "Error handling input for player2: " + e.getMessage());
                }
            }
        }
    }

    // Метод для проверки столкновения игрока с другим игроком
    private boolean checkPlayerTankCollision(Tank tank, float newY, float newX) {
        if (tank == null) return false;

        // Сохраняем текущие координаты
        float oldX = tank.positionX;
        float oldY = tank.positionY;

        // Устанавливаем новые координаты для проверки
        tank.positionX = (int) newX;
        tank.positionY = (int) newY;

        // Проверяем столкновение с игроками
        boolean collides = false;

        if (tank == player1 && player2 != null && player2.isAlive() && tank.collidesWith(player2)) {
            collides = true;
        } else if (tank == player2 && player1 != null && player1.isAlive() && tank.collidesWith(player1)) {
            collides = true;
        }

        // Восстанавливаем старые координаты
        tank.positionX = (int) oldX;
        tank.positionY = (int) oldY;

        return collides;
    }

    // Метод для проверки столкновения игрока с врагами
    private boolean checkPlayerEnemyCollision(Tank tank, float newY, float newX) {
        if (tank == null) return false;

        // Сохраняем текущие координаты
        float oldX = tank.positionX;
        float oldY = tank.positionY;

        // Устанавливаем новые координаты для проверки
        tank.positionX = (int) newX;
        tank.positionY = (int) newY;

        // Проверяем столкновение с врагами
        boolean collides = false;
        for (Tank enemy : enemies) {
            if (enemy != null && enemy.isAlive() && tank.collidesWith(enemy)) {
                collides = true;
                break;
            }
        }

        // Восстанавливаем старые координаты
        tank.positionX = (int) oldX;
        tank.positionY = (int) oldY;

        return collides;
    }

    // Метод для проверки столкновения врага с другими врагами
    private boolean checkEnemyCollision(Tank tank, float newY, float newX) {
        if (tank == null) return false;

        // Сохраняем текущие координаты
        float oldX = tank.positionX;
        float oldY = tank.positionY;

        // Устанавливаем новые координаты для проверки
        tank.positionX = (int) newX;
        tank.positionY = (int) newY;

        // Проверяем столкновение с другими врагами
        boolean collides = false;
        for (Tank otherEnemy : enemies) {
            if (otherEnemy != null && tank != otherEnemy && otherEnemy.isAlive() && tank.collidesWith(otherEnemy)) {
                collides = true;
                break;
            }
        }

        // Восстанавливаем старые координаты
        tank.positionX = (int) oldX;
        tank.positionY = (int) oldY;

        return collides;
    }

    @Override
    public void pause() {
        // Можно автоматически ставить игру на паузу при сворачивании окна
        isPaused = true;
    }

    @Override
    public void resume() {
        // Можно оставить игру на паузе при восстановлении окна, чтобы игрок сам решил когда продолжить
        // isPaused = false;
    }

    @Override
    public void show() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        batch.dispose();
        textBatch.dispose(); // Освобождаем ресурсы текстового batch
        for (Bullet bullet : bullets) {
            if (bullet != null) {
                bullet.dispose();
            }
        }
        font.dispose();
        largeFont.dispose(); // Освобождаем ресурсы большого шрифта
        if (explosionSound != null) explosionSound.dispose();
        if (hitSound != null) hitSound.dispose();
    }

    @Override
    public void resize(int width, int height) {
    }
}
