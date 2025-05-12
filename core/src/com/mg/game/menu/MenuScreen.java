package com.mg.game.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mg.game.GameScreen;
import com.mg.game.assets.Assets;
import com.mg.game.gdxGame;

public class MenuScreen implements Screen {
    private gdxGame game;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private int selectorPosition = 305;
    private float stateTime;
    private boolean assetsLoaded = false;

    public MenuScreen(gdxGame game) {
        this.game = game;
        camera = new OrthographicCamera();
        camera.setToOrtho(true, 640, 480);
        batch = new SpriteBatch();
        stateTime = 0f;

        // Load menu assets
        Assets.loadMenuAssets();

        // Pre-load score screen textures to ensure they're available
        Assets.loadScoreScreenTextures();

        assetsLoaded = true;
        Gdx.app.log("MenuScreen", "Assets loaded successfully");
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();
        stateTime += Gdx.graphics.getDeltaTime();

        if (!assetsLoaded) {
            // If assets aren't loaded yet, show loading screen
            batch.begin();
            // Draw loading text (placeholder, as no font is defined)
            batch.end();
            return;
        }

        Assets.current_frame = Assets.movingTankAnimation.getKeyFrame(stateTime, true);
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        batch.draw(Assets.spriteBack, 0, 0, 640, 480);
        batch.draw(Assets.current_frame, 190, selectorPosition, 26, 26);
        batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            if (selectorPosition != 342) {
                Assets.selectionSound.play();
                selectorPosition = 342;
            }
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            if (selectorPosition != 305) {
                Assets.selectionSound.play();
                selectorPosition = 305;
            }
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            if (selectorPosition == 305) {
                game.setScreen(new GameScreen(game, 1));
            } else if (selectorPosition == 342) {
                // Ensure assets are loaded before starting 2-player mode
                Assets.loadScoreScreenTextures();
                game.setScreen(new GameScreen(game, 2));
            }
        }
    }

    @Override
    public void resize(int width, int height) {
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
}