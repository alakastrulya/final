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
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            player1.handleInput(Input.Keys.DOWN, stateTime);
        } else if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            player1.handleInput(Input.Keys.UP, stateTime);
        } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            player1.handleInput(Input.Keys.LEFT, stateTime);
        } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            player1.handleInput(Input.Keys.RIGHT, stateTime);
        } else if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            player1.handleInput(Input.Keys.SPACE, stateTime);
        } else {
            player1.handleInput(-1, stateTime);
        }

        if (player2 != null) {
            if (Gdx.input.isKeyPressed(Input.Keys.S)) {
                player2.handleInput(Input.Keys.DOWN, stateTime);
            } else if (Gdx.input.isKeyPressed(Input.Keys.W)) {
                player2.handleInput(Input.Keys.UP, stateTime);
            } else if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                player2.handleInput(Input.Keys.LEFT, stateTime);
            } else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                player2.handleInput(Input.Keys.RIGHT, stateTime);
            } else if (Gdx.input.isKeyPressed(Input.Keys.ENTER)) {
                player2.handleInput(Input.Keys.SPACE, stateTime);
            } else {
                player2.handleInput(-1, stateTime);
            }
        }
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