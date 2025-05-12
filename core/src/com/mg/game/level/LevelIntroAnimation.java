package com.mg.game.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mg.game.assets.Assets;

public class LevelIntroAnimation {
    // Animation states
    public enum State {
        CURTAINS_CLOSING, // The blinds are closing
        SHOW_STAGE_TEXT,  // Showing the text "STAGE X"
        CURTAINS_OPENING, // The blinds are opening
        FINISHED          // Animation is completed
    }

    private State currentState;
    private float stateTime;
    private float curtainTopY;      // Position of the upper curtain
    private float curtainBottomY;   // Position of the lower curtain
    private float curtainHeight;    // Curtain height
    private float curtainSpeed;     // The speed of the blinds
    private int levelNumber;        // Level number
    private BitmapFont font;        // Font for the text "STAGE X" (fallback)
    private float textAlpha;        // Text transparency
    private float textDisplayTime;  // Text display time
    private boolean isFinished;     // Animation completion flag

    // Textures for curtains and text
    private Texture curtainTexture;
    private Texture stageTexture;
    private Texture numberTexture;

    // Scale for displaying textures
    private float textScale = 2.0f; // Increase the size by 2 times

    // Curtain Color #636363
    private Color curtainColor;

    // Blind positioning settings
    private float topCurtainFinalY;     // The final position of the upper curtain
    private float bottomCurtainFinalY;  // The final position of the lower curtain
    private float centerGap;            // The distance between the blinds in the center

    // Flag to determine if the coordinate system is inverted
    private boolean isYFlipped;

    public LevelIntroAnimation(int levelNumber) {
        this.levelNumber = levelNumber;
        this.currentState = State.CURTAINS_CLOSING;
        this.stateTime = 0f;

        // Checking whether the coordinate system is inverted
        // GameScreen uses camera.setToOrtho(true, ...), which means an inverted system
        this.isYFlipped = true; // We assume that the coordinate system is inverted

        // Adjusting the size of the blinds
        this.curtainHeight = Gdx.graphics.getHeight() / 2f + (centerGap / 2f);

        // Calculating the final positions of the blinds, taking into account the inverted coordinate system
        if (isYFlipped) {
            float centerY = Gdx.graphics.getHeight() / 2f;
            this.topCurtainFinalY = centerY - curtainHeight;
            this.bottomCurtainFinalY = centerY;



            // The initial positions of the blinds (outside the screen)
            this.curtainTopY = -curtainHeight; // The upper curtain is completely outside the upper border of the screen
            this.curtainBottomY = Gdx.graphics.getHeight(); // The lower curtain starts from the bottom border of the screen
        } else {
            // In the usual system, Y=0 at the bottom of the screen, Y=height at the top
            this.topCurtainFinalY = (Gdx.graphics.getHeight() + centerGap) / 2;
            this.bottomCurtainFinalY = (Gdx.graphics.getHeight() - centerGap) / 2 - curtainHeight;

            // The initial positions of the blinds (outside the screen)
            this.curtainTopY = Gdx.graphics.getHeight(); // The upper curtain starts from the upper border of the screen
            this.curtainBottomY = -curtainHeight; // The lower curtain is completely behind the lower border of the screen
        }

        // Adding debugging output
        Gdx.app.log("LevelIntroAnimation", "Screen height: " + Gdx.graphics.getHeight());
        Gdx.app.log("LevelIntroAnimation", "Curtain height: " + curtainHeight);
        Gdx.app.log("LevelIntroAnimation", "Initial top curtain Y: " + curtainTopY);
        Gdx.app.log("LevelIntroAnimation", "Final top curtain Y: " + topCurtainFinalY);

        this.curtainSpeed = Gdx.graphics.getHeight() / 1.5f;  // The speed of the blinds
        this.textAlpha = 0f;
        this.textDisplayTime = 2.0f;                         // The time of displaying the text "STAGE X"
        this.isFinished = false;

        // Creating a font for the text "STAGE X" (backup version)
        font = new BitmapFont(Gdx.files.internal("fonts/pixel.fnt"));
        font.getData().setScale(5.0f); // Large font size
        font.setColor(Color.WHITE);

        // Creating color #636363 (99, 99, 99 in RGB)
        curtainColor = new Color(0x99/255f, 0x99/255f, 0x99/255f, 1);

        // Creating a texture for curtains
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(curtainColor);
        pixmap.fill();
        curtainTexture = new Texture(pixmap);
        pixmap.dispose();

        // Loading textures for "STAGE" and numbers
        loadStageTextures();
    }

    private void loadStageTextures() {
        try {
            // Loading the texture for "STAGE"
            stageTexture = new Texture(Gdx.files.internal("sprites/ui/stage.png"));
            Gdx.app.log("LevelIntroAnimation", "Loaded stage.png: " + stageTexture.getWidth() + "x" + stageTexture.getHeight());

            // Loading the texture for the level number
            numberTexture = new Texture(Gdx.files.internal("sprites/ui/number" + levelNumber + ".png"));
            Gdx.app.log("LevelIntroAnimation", "Loaded number" + levelNumber + ".png: " + numberTexture.getWidth() + "x" + numberTexture.getHeight());
        } catch (Exception e) {
            Gdx.app.error("LevelIntroAnimation", "Error loading textures: " + e.getMessage());
            stageTexture = null;
            numberTexture = null;
        }
    }

    public void update(float delta) {
        stateTime += delta;

        switch (currentState) {
            case CURTAINS_CLOSING:
                if (isYFlipped) {
                    // In the inverted coordinate system
                    // Move the upper curtain down (increase Y)
                    curtainTopY += curtainSpeed * delta;
                    // Move the lower curtain up (decrease Y)
                    curtainBottomY -= curtainSpeed * delta;

                    // Debugging output
                    if (stateTime < 0.1f) {
                        Gdx.app.log("LevelIntroAnimation", "Top curtain Y: " + curtainTopY);
                    }

                    // Check whether the blinds have reached their final positions
                    if (curtainTopY >= topCurtainFinalY && curtainBottomY <= bottomCurtainFinalY) {
                        // We fix the positions of the blinds exactly at the end positions
                        curtainTopY = topCurtainFinalY;
                        curtainBottomY = bottomCurtainFinalY;

                        currentState = State.SHOW_STAGE_TEXT;
                        stateTime = 0f;

                        // Playing the sound of the beginning of the level
                        if (Assets.levelBeginSound != null) {
                            Assets.levelBeginSound.play();
                        }
                    }
                } else {
                    // In the usual coordinate system
                    // Move the upper curtain down (decrease Y)
                    curtainTopY -= curtainSpeed * delta;
                    // Move the lower curtain up (increase Y)
                    curtainBottomY += curtainSpeed * delta;

                    // Check whether the blinds have reached their final positions
                    if (curtainTopY <= topCurtainFinalY && curtainBottomY >= bottomCurtainFinalY) {
                        // We fix the positions of the blinds exactly at the end positions
                        curtainTopY = topCurtainFinalY;
                        curtainBottomY = bottomCurtainFinalY;

                        currentState = State.SHOW_STAGE_TEXT;
                        stateTime = 0f;

                        // Playing the sound of the beginning of the level
                        if (Assets.levelBeginSound != null) {
                            Assets.levelBeginSound.play();
                        }
                    }
                }
                break;

            case SHOW_STAGE_TEXT:
                // Smoothly displaying the text
                if (stateTime < 0.5f) {
                    textAlpha = stateTime / 0.5f; // Smooth appearance in 0.5 seconds
                } else if (stateTime > textDisplayTime - 0.5f) {
                    textAlpha = (textDisplayTime - stateTime) / 0.5f; // Smooth disappearance in 0.5 seconds
                } else {
                    textAlpha = 1.0f;
                }

                // If the text display time has expired
                if (stateTime >= textDisplayTime) {
                    currentState = State.CURTAINS_OPENING;
                    stateTime = 0f;
                }
                break;

            case CURTAINS_OPENING:
                if (isYFlipped) {
                    // In an inverted coordinate system
                    // Move the upper curtain up (decrease Y)
                    curtainTopY -= curtainSpeed * delta;
                    // Move the lower curtain down (increase Y)
                    curtainBottomY += curtainSpeed * delta;

                    // If the blinds have gone off the screen
                    if (curtainTopY <= -curtainHeight && curtainBottomY >= Gdx.graphics.getHeight()) {
                        currentState = State.FINISHED;
                        isFinished = true;
                    }
                } else {
                    // In the usual coordinate system
                    // Move the upper curtain up (increase Y)
                    curtainTopY += curtainSpeed * delta;
                    // Move the lower curtain down (decrease Y)
                    curtainBottomY -= curtainSpeed * delta;

                    // If the blinds have gone off the screen
                    if (curtainTopY >= Gdx.graphics.getHeight() && curtainBottomY <= -curtainHeight) {
                        currentState = State.FINISHED;
                        isFinished = true;
                    }
                }
                break;

            case FINISHED:
                // We are not doing anything, the animation is completed
                break;
        }
    }

    public void render(SpriteBatch batch) {
        // Upper curtain
        batch.draw(curtainTexture, 0, curtainTopY, Gdx.graphics.getWidth(), curtainHeight);

        // Lower curtain
        batch.draw(curtainTexture, 0, curtainBottomY, Gdx.graphics.getWidth(), curtainHeight);

        // Text "STAGE X"
        if (currentState == State.SHOW_STAGE_TEXT && textAlpha > 0f) {
            font.setColor(1, 1, 1, textAlpha);
            String text = "STAGE " + levelNumber;
            GlyphLayout layout = new GlyphLayout(font, text);
            float textX = (Gdx.graphics.getWidth() - layout.width) / 2f;

            // The center between the blinds
            float textY = Gdx.graphics.getHeight() / 2f + layout.height / 2f;

            font.draw(batch, layout, textX, textY);
        }
    }




    public boolean isFinished() {
        return isFinished;
    }

    public void dispose() {
        if (curtainTexture != null) {
            curtainTexture.dispose();
        }
        if (stageTexture != null) {
            stageTexture.dispose();
        }
        if (numberTexture != null) {
            numberTexture.dispose();
        }
        if (font != null) {
            font.dispose();
        }
    }
}