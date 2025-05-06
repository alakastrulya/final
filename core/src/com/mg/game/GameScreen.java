package com.mg.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.util.ArrayList;
import java.util.Iterator;

public class GameScreen implements Screen {

    private int playerCount;
    private gdxGame game;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private float stateTime;
    private Tank player1;
    private Tank player2;
    private ArrayList<Bullet> bullets;
    private ArrayList<Tank> enemies;
    private float player1ShootCooldown = 0f;
    private float player2ShootCooldown = 0f;
    private static final float SHOOT_COOLDOWN = 0.5f; // Задержка между выстрелами в секундах

    public GameScreen(gdxGame game, int playerCount) {
        this.playerCount = playerCount;
        this.game = game;
        camera = new OrthographicCamera();
        camera.setToOrtho(true, 640, 480);
        batch = new SpriteBatch();
        stateTime = 0F;
        bullets = new ArrayList<>();
        enemies = new ArrayList<>();

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
            Tank enemy = new Tank("red", 1, true); // Предполагаем, что есть текстуры для красных танков
            enemy.positionX = 100 + i * 150;
            enemy.positionY = 100 + i * 100;
            enemies.add(enemy);
        }

        Music levelSound = Gdx.audio.newMusic(Gdx.files.internal("sounds/startLevel.mp3"));
        levelSound.play();
        Assets.loadLevel(1);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor((float)192/255, (float)192/255, (float)192/255, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        camera.update();
        stateTime += Gdx.graphics.getDeltaTime();

        // Обновляем кулдауны
        player1ShootCooldown -= delta;
        if (player2 != null) {
            player2ShootCooldown -= delta;
        }

        // Обновляем врагов
        updateEnemies(delta);

        // Обновляем снаряды
        updateBullets(delta);

        checkKeyPress();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(Assets.levelBack, 0, 0, 480, 480);

        // Рисуем первый танк
        TextureRegion frame1 = player1.getCurrentFrame();
        batch.draw(frame1, player1.positionX, player1.positionY, 26, 26);

        // Рисуем второй танк, если он существует
        if (player2 != null) {
            TextureRegion frame2 = player2.getCurrentFrame();
            batch.draw(frame2, player2.positionX, player2.positionY, 26, 26);
        }

        // Рисуем врагов
        for (Tank enemy : enemies) {
            TextureRegion enemyFrame = enemy.getCurrentFrame();
            batch.draw(enemyFrame, enemy.positionX, enemy.positionY, 26, 26);
        }

        // Рисуем снаряды
        for (Bullet bullet : bullets) {
            if (bullet.isActive()) {
                batch.draw(bullet.getTexture(), bullet.getPositionX(), bullet.getPositionY(), 4, 4);
            }
        }

        batch.end();
    }

    private void updateEnemies(float delta) {
        for (Tank enemy : enemies) {
            // Проверяем столкновение перед движением врага
            int newX = enemy.positionX;
            int newY = enemy.positionY;

            switch (enemy.getDirection()) {
                case FORWARD:
                    newY = enemy.positionY + 1;
                    break;
                case BACKWARD:
                    newY = enemy.positionY - 1;
                    break;
                case LEFT:
                    newX = enemy.positionX - 1;
                    break;
                case RIGHT:
                    newX = enemy.positionX + 1;
                    break;
            }

            boolean canMove = true;
            if (newY >= 0 && newY <= 454 && newX >= 0 && newX <= 454) {
                if (wouldCollide(enemy, newY, newX)) {
                    canMove = false;
                }
            } else {
                canMove = false;
            }

            // Если враг может двигаться, обновляем его состояние
            if (canMove) {
                Bullet bullet = enemy.updateEnemy(delta, player1, player2);
                if (bullet != null) {
                    bullets.add(bullet);
                }
            } else {
                enemy.chooseRandomDirection(); // Если столкновение, меняем направление
            }
        }
    }

    private void updateBullets(float delta) {
        Iterator<Bullet> iterator = bullets.iterator();
        while (iterator.hasNext()) {
            Bullet bullet = iterator.next();
            bullet.update(delta);
            if (!bullet.isActive()) {
                bullet.dispose();
                iterator.remove();
            }
        }
    }

    private void checkKeyPress() {
        // Управление первым танком (стрелки + Space для стрельбы)
        int keycode1 = -1;
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            keycode1 = Input.Keys.DOWN;
            int newY = player1.positionY + 1;
            if (newY <= 454 && !wouldCollide(player1, newY, player1.positionX)) {
                player1.handleInput(keycode1, stateTime);
            }
        } else if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            keycode1 = Input.Keys.UP;
            int newY = player1.positionY - 1;
            if (newY >= 0 && !wouldCollide(player1, newY, player1.positionX)) {
                player1.handleInput(keycode1, stateTime);
            }
        } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            keycode1 = Input.Keys.LEFT;
            int newX = player1.positionX - 1;
            if (newX >= 0 && !wouldCollide(player1, player1.positionY, newX)) {
                player1.handleInput(keycode1, stateTime);
            }
        } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            keycode1 = Input.Keys.RIGHT;
            int newX = player1.positionX + 1;
            if (newX <= 454 && !wouldCollide(player1, player1.positionY, newX)) {
                player1.handleInput(keycode1, stateTime);
            }
        } else if (Gdx.input.isKeyPressed(Input.Keys.SPACE) && player1ShootCooldown <= 0) {
            keycode1 = Input.Keys.SPACE;
            player1.handleInput(keycode1, stateTime);
            bullets.add(player1.shoot());
            player1ShootCooldown = SHOOT_COOLDOWN;
        } else {
            player1.handleInput(-1, stateTime);
        }

        // Управление вторым танком (WASD + Enter для стрельбы), если он существует
        if (player2 != null) {
            int keycode2 = -1;
            if (Gdx.input.isKeyPressed(Input.Keys.S)) {
                keycode2 = Input.Keys.DOWN;
                int newY = player2.positionY + 1;
                if (newY <= 454 && !wouldCollide(player2, newY, player2.positionX)) {
                    player2.handleInput(keycode2, stateTime);
                }
            } else if (Gdx.input.isKeyPressed(Input.Keys.W)) {
                keycode2 = Input.Keys.UP;
                int newY = player2.positionY - 1;
                if (newY >= 0 && !wouldCollide(player2, newY, player2.positionX)) {
                    player2.handleInput(keycode2, stateTime);
                }
            } else if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                keycode2 = Input.Keys.LEFT;
                int newX = player2.positionX - 1;
                if (newX >= 0 && !wouldCollide(player2, player2.positionY, newX)) {
                    player2.handleInput(keycode2, stateTime);
                }
            } else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                keycode2 = Input.Keys.RIGHT;
                int newX = player2.positionX + 1;
                if (newX <= 454 && !wouldCollide(player2, player2.positionY, newX)) {
                    player2.handleInput(keycode2, stateTime);
                }
            } else if (Gdx.input.isKeyPressed(Input.Keys.ENTER) && player2ShootCooldown <= 0) {
                keycode2 = Input.Keys.SPACE;
                player2.handleInput(keycode2, stateTime);
                bullets.add(player2.shoot());
                player2ShootCooldown = SHOOT_COOLDOWN;
            } else {
                player2.handleInput(-1, stateTime);
            }
        }
    }

    // Метод для проверки столкновения при предполагаемом новом положении
    private boolean wouldCollide(Tank tank, float newY, float newX) {
        // Сохраняем текущие координаты
        float oldX = tank.positionX;
        float oldY = tank.positionY;

        // Устанавливаем новые координаты для проверки
        tank.positionX = (int) newX;
        tank.positionY = (int) newY;

        // Проверяем столкновение с игроками
        boolean collides = (tank == player1 && player2 != null && tank.collidesWith(player2)) ||
                (tank == player2 && tank.collidesWith(player1));

        // Проверяем столкновение с врагами
        for (Tank enemy : enemies) {
            if (tank != enemy && tank.collidesWith(enemy)) {
                collides = true;
                break;
            }
        }

        // Если танк — враг, проверяем столкновение с другими врагами
        if (tank.isEnemy()) {
            for (Tank otherEnemy : enemies) {
                if (tank != otherEnemy && tank.collidesWith(otherEnemy)) {
                    collides = true;
                    break;
                }
            }
        }

        // Восстанавливаем старые координаты
        tank.positionX = (int) oldX;
        tank.positionY = (int) oldY;

        return collides;
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
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
        for (Bullet bullet : bullets) {
            bullet.dispose();
        }
    }

    @Override
    public void resize(int width, int height) {
    }
}