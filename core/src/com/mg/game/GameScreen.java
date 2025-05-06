package com.mg.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class GameScreen implements Screen {

    private int playerCount;
    private gdxGame game;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private float stateTime;
    private Tank player1;
    private Tank player2;

    public GameScreen(gdxGame game, int playerCount) {
        this.playerCount = playerCount;
        this.game = game;
        camera = new OrthographicCamera();
        camera.setToOrtho(true, 640, 480);
        batch = new SpriteBatch();
        stateTime = 0F;

        // Инициализация первого танка
        player1 = new Tank("yellow", 1);
        player1.positionX = 50;
        player1.positionY = 50;
        Gdx.app.log("GameScreen", "Player 1 color: " + player1.getColour());

        // Инициализация второго танка, если выбран режим на 2 игрока
        if (playerCount == 2) {
            player2 = new Tank("green", 1);
            player2.positionX = 400;
            player2.positionY = 400;
            Gdx.app.log("GameScreen", "Player 2 color: " + player2.getColour());
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

        batch.end();
    }

    private void checkKeyPress() {
        // Управление первым танком (стрелки)
        int keycode1 = -1;
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            keycode1 = Input.Keys.DOWN;
            // Проверяем столкновение перед движением
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
        } else if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            keycode1 = Input.Keys.SPACE;
            player1.handleInput(keycode1, stateTime);
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
            } else if (Gdx.input.isKeyPressed(Input.Keys.ENTER)) {
                keycode2 = Input.Keys.SPACE;
                player2.handleInput(keycode2, stateTime);
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

        // Проверяем столкновение
        boolean collides = (tank == player1 && player2 != null && tank.collidesWith(player2)) ||
                (tank == player2 && tank.collidesWith(player1));

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
    }

    @Override
    public void resize(int width, int height) {
    }
}