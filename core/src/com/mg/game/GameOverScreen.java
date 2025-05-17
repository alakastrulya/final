package com.mg.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mg.game.assets.Assets;
import com.badlogic.gdx.Input.Keys;

public class GameOverScreen implements Screen {
    private gdxGame game;
    private int playerCount;
    private int player1Score;
    private int player2Score;
    private OrthographicCamera camera;
    private SpriteBatch batch;

    public GameOverScreen(gdxGame game, int playerCount, int player1Score, int player2Score) {
        this.game = game;
        this.playerCount = playerCount;
        this.player1Score = player1Score;
        this.player2Score = player2Score;
        this.camera = new OrthographicCamera();
        camera.setToOrtho(true, 640, 480); // Перевёрнутая система координат
        this.batch = new SpriteBatch();
    }

    @Override
    public void show() {
        Gdx.app.log("GameOverScreen", "GameOverScreen shown");
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        if (Assets.gameOverTexture != null) {
            batch.draw(
                    Assets.gameOverTexture,
                    0, 480,         // x, y (верхний левый угол)
                    640, -480       // width, height (отрицательная высота инвертирует)
            );
        } else {
            Gdx.app.log("GameOverScreen", "gameOverTexture is null");
        }

        batch.end();

        // Обработка ввода для перезапуска по Enter
        if (Gdx.input.isKeyJustPressed(Keys.ENTER)) {
            Gdx.app.log("GameOverScreen", "Restarting game by pressing Enter");
            game.setScreen(new GameScreen(game, playerCount));
            dispose();
        }
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(true, width, height);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        Gdx.app.log("GameOverScreen", "Disposing GameOverScreen resources");
        if (batch != null) {
            batch.dispose();
            batch = null;
        }
    }
}