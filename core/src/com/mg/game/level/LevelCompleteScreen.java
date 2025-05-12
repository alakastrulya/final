package com.mg.game.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mg.game.gdxGame;

public class LevelCompleteScreen implements Screen {

    private final gdxGame game;
    private final int level;
    private final int hiScore = 1000;
    private final int player1Score;
    private final int player2Score;
    private final int[] player1Breakdown;
    private final int[] player2Breakdown;
    private final boolean isTwoPlayer;

    private final OrthographicCamera camera;
    private final SpriteBatch batch;
    private final BitmapFont fontLarge;
    BitmapFont pixelFont = new BitmapFont(Gdx.files.internal("fonts/pixel.fnt"));


    public LevelCompleteScreen(gdxGame game, int level, int hiScore, int player1Score, int player2Score,
                               int[] player1Breakdown, int[] player2Breakdown, int[] tankCounts, boolean isTwoPlayer) {
        this.game = game;
        this.level = level;
        this.player1Score = player1Score;
        this.player2Score = player2Score;
        this.player1Breakdown = player1Breakdown;
        this.player2Breakdown = player2Breakdown;
        this.isTwoPlayer = isTwoPlayer;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 640, 480);
        batch = new SpriteBatch();

        pixelFont.getData().setScale(2f);

        fontLarge = new BitmapFont(Gdx.files.internal("fonts/pixel.fnt"));
        fontLarge.getData().setScale(3f);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // HI-SCORE
        pixelFont.setColor(Color.RED);
        pixelFont.draw(batch, "HI-SCORE", 160, 460); // a bit to the left
        pixelFont.setColor(Color.GOLD);
        pixelFont.draw(batch, String.valueOf(hiScore), 375, 460); // to the right, no leading zeros

        // STAGE
        fontLarge.setColor(Color.WHITE);
        GlyphLayout stageLayout = new GlyphLayout(fontLarge, "STAGE " + level);
        fontLarge.draw(batch, stageLayout, (640 - stageLayout.width) / 2, 400);

        // Player labels
        pixelFont.setColor(Color.RED);
        pixelFont.draw(batch, "I-PLAYER", 80, 340);
        if (isTwoPlayer) pixelFont.draw(batch, "II-PLAYER", 350, 340);

        // Player scores
        pixelFont.setColor(Color.GOLD);
        pixelFont.draw(batch, String.format("%04d", player1Score), 200, 305);
        if (isTwoPlayer) pixelFont.draw(batch, String.format("%04d", player2Score), 520, 305);

        // Score breakdown
        for (int i = 0; i < 4; i++) {
            int y = 250 - i * 30;

            // Player 1
            pixelFont.setColor(Color.GOLD);
            pixelFont.draw(batch, String.format("%04d PTS", player1Breakdown[i] * 100), 50, y);
            pixelFont.setColor(Color.WHITE);
            pixelFont.draw(batch, String.valueOf(player1Breakdown[i]), 260, y);

            // Player 2
            if (isTwoPlayer) {
                pixelFont.setColor(Color.GOLD);
                pixelFont.draw(batch, String.format("%04d PTS", player2Breakdown[i] * 100), 350, y);
                pixelFont.setColor(Color.WHITE);
                pixelFont.draw(batch, String.valueOf(player2Breakdown[i]), 570, y);
            }
        }

        // TOTAL score
        int totalScore = 0;
        for (int i = 0; i < 4; i++) {
            totalScore += player1Breakdown[i] * 100;
            totalScore += player2Breakdown[i] * 100;
        }

        pixelFont.setColor(Color.WHITE);
        pixelFont.draw(batch, "TOTAL", 210, 60);
        pixelFont.draw(batch, String.valueOf(totalScore), 360, 60);

        batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            game.setScreen(game.menuScreen);
        }
    }

    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void show() {}

    @Override
    public void dispose() {
        batch.dispose();
        pixelFont.dispose();
        fontLarge.dispose();
    }
}
