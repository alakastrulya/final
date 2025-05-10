package com.mg.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import com.badlogic.gdx.utils.Align;

public class LevelCompleteScreen implements Screen {
    private gdxGame game;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private float stateTime;
    private int currentLevel;
    private int hiScore;
    private int player1Score;
    private int player2Score;
    private int[] player1PointsBreakdown;
    private int[] player2PointsBreakdown;
    private int[] tankCounts;
    private boolean twoPlayerMode;
    private float animationDelay = 0.5f;
    private int animationStage = 0;
    private float nextAnimationTime = 0;

    // Textures for the score screen
    private Texture backgroundTexture;
    private Texture[] digitTextures;
    private Texture hiScoreTexture;
    private Texture stageTexture;
    private Texture player1Texture;
    private Texture player2Texture;
    private Texture ptsTexture;
    private Texture totalTexture;
    private Texture tankIconTexture;
    private Texture arrowTexture;

    // Font as fallback
    private BitmapFont font;

    public LevelCompleteScreen(gdxGame game, int currentLevel, int hiScore, int player1Score,
                               int player2Score, int[] player1PointsBreakdown,
                               int[] player2PointsBreakdown, int[] tankCounts, boolean twoPlayerMode) {
        this.game = game;
        this.currentLevel = currentLevel;
        this.hiScore = hiScore;
        this.player1Score = player1Score;
        this.player2Score = player2Score;
        this.player1PointsBreakdown = player1PointsBreakdown;
        this.player2PointsBreakdown = player2PointsBreakdown;
        this.tankCounts = tankCounts;
        this.twoPlayerMode = twoPlayerMode;

        camera = new OrthographicCamera();
        camera.setToOrtho(true, 640, 480);
        batch = new SpriteBatch();
        stateTime = 0f;

        // Load textures
        loadTextures();

        // Create fallback font
        font = new BitmapFont(true);
        font.getData().setScale(2f);
    }
    private Texture flip(String path) {
        Texture tex = new Texture(Gdx.files.internal(path));
        TextureRegion reg = new TextureRegion(tex);
        reg.flip(false, true);
        Pixmap pixmap = reg.getTexture().getTextureData().consumePixmap();
        Texture flipped = new Texture(pixmap);
        pixmap.dispose();
        tex.dispose();
        return flipped;
    }


    private void loadTextures() {
        try {
            backgroundTexture = createColorTexture(Color.BLACK);

            digitTextures = new Texture[10];
            for (int i = 0; i < 10; i++) {
                digitTextures[i] = flip("sprites/ui/digit" + i + ".png");
            }

            hiScoreTexture = flip("sprites/ui/hi-score.png");
            stageTexture = flip("sprites/ui/stage.png");
            player1Texture = flip("sprites/ui/i-player.png");
            player2Texture = flip("sprites/ui/ii-player.png");
            ptsTexture = flip("sprites/ui/pts.png");
            totalTexture = flip("sprites/ui/total.png");
            tankIconTexture = flip("sprites/ui/tank-icon.png");
            arrowTexture = flip("sprites/ui/arrow.png");

            Gdx.app.log("LevelCompleteScreen", "All textures loaded and flipped.");
        } catch (Exception e) {
            Gdx.app.error("LevelCompleteScreen", "Error loading textures: " + e.getMessage());
        }
    }


    private Texture createColorTexture(Color color) {
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }


    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();
        stateTime += delta;

        if (stateTime > nextAnimationTime && animationStage < 10) {
            animationStage++;
            nextAnimationTime = stateTime + animationDelay;
        }

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        batch.draw(backgroundTexture, 0, 0, 640, 480);

        if (hiScoreTexture != null && animationStage >= 1) {
            batch.setColor(Color.RED);
            batch.draw(hiScoreTexture, 100, 20, 200, 30);
            batch.setColor(Color.ORANGE);
            drawNumber(hiScore, 400, 20, 5, 30, 30);
        }

        if (stageTexture != null && animationStage >= 2) {
            batch.setColor(Color.WHITE);
            batch.draw(stageTexture, 240, 70, 120, 30);
            drawNumber(currentLevel, 380, 70, 1, 30, 30);
        }

        if (player1Texture != null && animationStage >= 3) {
            batch.setColor(Color.RED);
            batch.draw(player1Texture, 100, 120, 200, 30);
            batch.setColor(Color.ORANGE);
            drawNumber(player1Score, 180, 170, 4, 25, 25);
        }

        if (twoPlayerMode && player2Texture != null && animationStage >= 4) {
            batch.setColor(Color.RED);
            batch.draw(player2Texture, 400, 120, 200, 30);
            batch.setColor(Color.ORANGE);
            drawNumber(player2Score, 480, 170, 4, 25, 25);
        }

        if (animationStage >= 5) {
            drawPointsBreakdown(player1PointsBreakdown, tankCounts, 100, 220, true);
        }

        if (twoPlayerMode && animationStage >= 6) {
            drawPointsBreakdown(player2PointsBreakdown, tankCounts, 400, 220, false);
        }

        if (totalTexture != null && animationStage >= 7) {
            batch.setColor(Color.WHITE);
            batch.draw(totalTexture, 100, 420, 120, 30);
            if (twoPlayerMode) {
                batch.draw(totalTexture, 400, 420, 120, 30);
            }
        }

        if (animationStage >= 8 && (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE))) {
            if (currentLevel < 8) {
                game.setScreen(new GameScreen(game, twoPlayerMode ? 2 : 1, currentLevel + 1));
            } else {
                game.setScreen(game.menuScreen);
            }
            dispose();
        }

        batch.end();
    }

    private void drawNumber(int number, float x, float y, int digits, float width, float height) {
        String numStr = String.valueOf(number);
        // Pad with leading zeros if needed
        while (numStr.length() < digits) {
            numStr = "0" + numStr;
        }

        for (int i = 0; i < numStr.length(); i++) {
            int digit = Character.getNumericValue(numStr.charAt(i));
            if (digit >= 0 && digit <= 9 && digitTextures[digit] != null) {
                batch.draw(digitTextures[digit], x + i * width, y, width, height);
            } else {
                // Fallback to font
                font.draw(batch, String.valueOf(digit), x + i * width, y + height);
            }
        }
    }

    private void drawPointsBreakdown(int[] pointsBreakdown, int[] tankCounts, float x, float y, boolean isPlayer1) {
        float rowHeight = 40;

        for (int i = 0; i < pointsBreakdown.length; i++) {
            float rowY = y + i * rowHeight;

            // Draw points
            batch.setColor(Color.WHITE);
            drawNumber(pointsBreakdown[i], x, rowY, 4, 20, 20);

            // Draw PTS text
            if (ptsTexture != null) {
                batch.draw(ptsTexture, x + 100, rowY, 60, 20);
            } else {
                font.draw(batch, "PTS", x + 100, rowY + 15);
            }

            // Draw tank icon and count
            if (tankIconTexture != null && arrowTexture != null) {
                batch.draw(arrowTexture, x + 170, rowY, 20, 20);
                batch.draw(tankIconTexture, x + 200, rowY, 25, 25);
                batch.draw(arrowTexture, x + 235, rowY, 20, 20);
                drawNumber(tankCounts[i], x + 260, rowY, 1, 20, 20);
            } else {
                font.draw(batch, "→ 🛦 → " + tankCounts[i], x + 170, rowY + 15);
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
        font.dispose();

        // Dispose all textures
        if (backgroundTexture != null) backgroundTexture.dispose();
        if (digitTextures != null) {
            for (Texture texture : digitTextures) {
                if (texture != null) texture.dispose();
            }
        }
        if (hiScoreTexture != null) hiScoreTexture.dispose();
        if (stageTexture != null) stageTexture.dispose();
        if (player1Texture != null) player1Texture.dispose();
        if (player2Texture != null) player2Texture.dispose();
        if (ptsTexture != null) ptsTexture.dispose();
        if (totalTexture != null) totalTexture.dispose();
        if (tankIconTexture != null) tankIconTexture.dispose();
        if (arrowTexture != null) arrowTexture.dispose();
    }
}
