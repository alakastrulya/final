package com.mg.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mg.game.map.MapLoader;
import com.mg.game.map.MapTile;
import com.mg.game.Bullet;
import java.util.ArrayList;

public class GameScreen implements Screen {

    private int playerCount;
    private gdxGame game;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private float stateTime;
    private Tank player1;
    private Tank player2;
    private ArrayList<Bullet> bullets = new ArrayList<>();
    private float player1ShootCooldown = 0f;
    private float player2ShootCooldown = 0f;
    private static final float SHOOT_COOLDOWN = 0.5f;
    private MapLoader mapLoader;
    private static final float TILE_SCALE = 0.87f;

    public GameScreen(gdxGame game, int playerCount) {
        this.playerCount = playerCount;
        this.game = game;
        camera = new OrthographicCamera();
        camera.setToOrtho(true, 640, 480);
        batch = new SpriteBatch();
        stateTime = 0F;
        player1 = new Tank("yellow", 1);
        Music levelSound = Gdx.audio.newMusic(Gdx.files.internal("sounds/startLevel.mp3"));
        levelSound.play();
        Assets.loadGameAssets(player1.getColour(), player1.getLevel());
        Assets.loadLevel(1);
        mapLoader = new MapLoader();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(192f / 255, 192f / 255, 192f / 255, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        camera.update();
        stateTime += Gdx.graphics.getDeltaTime();
        checkKeyPress();
        TextureRegion frame = player1.getCurrentFrame();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // 1. Фон
        batch.draw(Assets.levelBack, 0, 0, 480, 480);

        // 2. Карта
        int offsetX = 1;
        int offsetY = -70;
        float tileScale = 0.8f;

        for (MapTile tile : mapLoader.tiles) {
            int flippedY = (480 / MapLoader.TILE_SIZE - tile.y - 1);
            float scaledSize = MapLoader.TILE_SIZE / TILE_SCALE;
            batch.draw(
                    tile.region,
                    tile.x * scaledSize + offsetX,
                    flippedY * scaledSize + offsetY,
                    scaledSize,
                    scaledSize
            );
        }

        // 3. Танки
        batch.draw(player1.getCurrentFrame(), player1.positionX, player1.positionY, 26 / TILE_SCALE, 26 / TILE_SCALE);
        if (player2 != null) {
            batch.draw(player2.getCurrentFrame(), player2.positionX, player2.positionY, 26 / TILE_SCALE, 26 / TILE_SCALE);
        }

        // 4. Пули
        for (Bullet bullet : bullets) {
            if (bullet.isActive()) {
                batch.draw(bullet.getTexture(), bullet.getPositionX(), bullet.getPositionY(), 4, 4);
            }
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
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void show() {}
    @Override public void hide() {}
    @Override public void dispose() {}
    @Override public void resize(int width, int height) {}
}
