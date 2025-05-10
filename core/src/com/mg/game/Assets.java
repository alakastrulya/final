package com.mg.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.util.HashMap;
import java.util.Map;

public class Assets {
    // Menu assets
    public static Texture textureBack;
    public static Sprite spriteBack;
    public static Texture yellowTankRight1_Texture;
    public static Texture yellowTankRight2_Texture;
    public static Animation<TextureRegion> movingTankAnimation;
    public static TextureRegion[] sheet_frames;
    public static TextureRegion current_frame;
    public static Sound selectionSound;

    // Game assets
    public static Sprite levelBack;
    public static Sound levelBeginSound;
    public static Texture enemyIcon;
    public static Texture healthIcon;
    public static Texture pixel;
    public static Sound explosionSound;
    public static Sound hitSound;

    // UI assets
    public static Texture pauseTexture;
    public static Texture gameOverTexture;

    // Level intro assets
    public static Texture curtainTexture;
    public static Texture stageTexture;
    public static Texture[] numberTextures;

    // Score screen assets
    public static Texture[] digitTextures;
    public static Texture hiScoreTexture;
    public static Texture iPlayerTexture;
    public static Texture iiPlayerTexture;
    public static Texture ptsTexture;
    public static Texture totalTexture;
    public static Texture tankIconTexture;
    public static Texture arrowTexture;

    // Tank animations
    private static Map<String, Animation<TextureRegion>> movingForwardAnimations = new HashMap<>();
    private static Map<String, Animation<TextureRegion>> standByForwardAnimations = new HashMap<>();
    private static Map<String, Animation<TextureRegion>> movingBackwardAnimations = new HashMap<>();
    private static Map<String, Animation<TextureRegion>> standByBackwardAnimations = new HashMap<>();
    private static Map<String, Animation<TextureRegion>> movingLeftAnimations = new HashMap<>();
    private static Map<String, Animation<TextureRegion>> standByLeftAnimations = new HashMap<>();
    private static Map<String, Animation<TextureRegion>> movingRightAnimations = new HashMap<>();
    private static Map<String, Animation<TextureRegion>> standByRightAnimations = new HashMap<>();

    // Explosion animation
    public static Animation<TextureRegion> explosionAnimation;
    private static Texture[] explosionTextures; // Array to hold individual textures
    private static TextureRegion[] explosionFrames;

    // Map assets
    public static Texture tileSet;

    // Game state
    public static float elapsedTime;
    public static String colour;
    public static int level;

    // Animation getters
    public static Animation<TextureRegion> getMovingForwardAnimation(String colour) {
        return movingForwardAnimations.getOrDefault(colour, null);
    }

    public static Animation<TextureRegion> getStandByForwardAnimation(String colour) {
        return standByForwardAnimations.getOrDefault(colour, null);
    }

    public static Animation<TextureRegion> getMovingBackwardAnimation(String colour) {
        return movingBackwardAnimations.getOrDefault(colour, null);
    }

    public static Animation<TextureRegion> getStandByBackwardAnimation(String colour) {
        return standByBackwardAnimations.getOrDefault(colour, null);
    }

    public static Animation<TextureRegion> getMovingLeftAnimation(String colour) {
        return movingLeftAnimations.getOrDefault(colour, null);
    }

    public static Animation<TextureRegion> getStandByLeftAnimation(String colour) {
        return standByLeftAnimations.getOrDefault(colour, null);
    }

    public static Animation<TextureRegion> getMovingRightAnimation(String colour) {
        return movingRightAnimations.getOrDefault(colour, null);
    }

    public static Animation<TextureRegion> getStandByRightAnimation(String colour) {
        return standByRightAnimations.getOrDefault(colour, null);
    }

    // Load methods
    public static void loadMenuAssets() {
        try {
            textureBack = new Texture(Gdx.files.internal("sprites/menu/menu.jpg"));
            textureBack.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            spriteBack = new Sprite(textureBack);
            spriteBack.flip(false, true);

            yellowTankRight1_Texture = new Texture(Gdx.files.internal("sprites/tanks/yellow/level_1/right1.png"));
            yellowTankRight2_Texture = new Texture(Gdx.files.internal("sprites/tanks/yellow/level_1/right2.png"));
            sheet_frames = new TextureRegion[2];

            sheet_frames[1] = new TextureRegion(yellowTankRight1_Texture, 0, 0, 13, 13);
            sheet_frames[0] = new TextureRegion(yellowTankRight2_Texture, 0, 0, 13, 13);

            movingTankAnimation = new Animation<>(0.1F, sheet_frames);
            selectionSound = Gdx.audio.newSound(Gdx.files.internal("sounds/menuSelect.mp3"));

            // Load UI textures
            loadUITextures();
        } catch (Exception e) {
            Gdx.app.error("Assets", "Error loading menu assets: " + e.getMessage());
        }
    }

    public static void loadGameAssets(String colour, int level) {
        loadLevel(level);
        loadTankAnimations(colour, level);
        loadSounds();
        loadUITextures();
        loadCurtainTextures();
        loadStageTextures();
        loadExplosionAnimation();
    }

    public static void loadLevel(int level) {
        try {
            Texture levelBase = new Texture(Gdx.files.internal("sprites/levels/levelBase.png"));
            levelBase.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            levelBack = new Sprite(levelBase);

            // Load tileset for map
            tileSet = new Texture(Gdx.files.internal("sprites/tiles/tileset.png"));
        } catch (Exception e) {
            Gdx.app.error("Assets", "Error loading level: " + e.getMessage());
            levelBack = null;
            tileSet = null;
        }
    }

    public static void loadTankAnimations(String colour, int level) {
        try {
            Texture right1 = new Texture(Gdx.files.internal("sprites/tanks/" + colour + "/level_" + level + "/right1.png"));
            Texture right2 = new Texture(Gdx.files.internal("sprites/tanks/" + colour + "/level_" + level + "/right2.png"));
            Texture left1 = new Texture(Gdx.files.internal("sprites/tanks/" + colour + "/level_" + level + "/left1.png"));
            Texture left2 = new Texture(Gdx.files.internal("sprites/tanks/" + colour + "/level_" + level + "/left2.png"));
            Texture backward1 = new Texture(Gdx.files.internal("sprites/tanks/" + colour + "/level_" + level + "/down1.png"));
            Texture backward2 = new Texture(Gdx.files.internal("sprites/tanks/" + colour + "/level_" + level + "/down2.png"));
            Texture forward1 = new Texture(Gdx.files.internal("sprites/tanks/" + colour + "/level_" + level + "/up1.png"));
            Texture forward2 = new Texture(Gdx.files.internal("sprites/tanks/" + colour + "/level_" + level + "/up2.png"));

            // Create forward animations
            TextureRegion[] movingForwardSheetFrames = new TextureRegion[2];
            TextureRegion standByForwardFrame = new TextureRegion(forward1, 0, 0, 13, 13);
            movingForwardSheetFrames[0] = new TextureRegion(forward1, 0, 0, 13, 13);
            movingForwardSheetFrames[1] = new TextureRegion(forward2, 0, 0, 13, 13);
            movingForwardAnimations.put(colour, new Animation<>(0.05F, movingForwardSheetFrames));
            standByForwardAnimations.put(colour, new Animation<>(0.05F, standByForwardFrame));

            // Create backward animations
            TextureRegion[] movingBackwardSheetFrames = new TextureRegion[2];
            TextureRegion standByBackwardFrame = new TextureRegion(backward1, 0, 0, 13, 13);
            movingBackwardSheetFrames[0] = new TextureRegion(backward1, 0, 0, 13, 13);
            movingBackwardSheetFrames[1] = new TextureRegion(backward2, 0, 0, 13, 13);
            movingBackwardAnimations.put(colour, new Animation<>(0.05F, movingBackwardSheetFrames));
            standByBackwardAnimations.put(colour, new Animation<>(0.05F, standByBackwardFrame));

            // Create left animations
            TextureRegion[] movingLeftSheetFrames = new TextureRegion[2];
            TextureRegion standByLeftFrame = new TextureRegion(left1, 0, 0, 13, 13);
            movingLeftSheetFrames[0] = new TextureRegion(left1, 0, 0, 13, 13);
            movingLeftSheetFrames[1] = new TextureRegion(left2, 0, 0, 13, 13);
            movingLeftAnimations.put(colour, new Animation<>(0.05F, movingLeftSheetFrames));
            standByLeftAnimations.put(colour, new Animation<>(0.05F, standByLeftFrame));

            // Create right animations
            TextureRegion[] movingRightSheetFrames = new TextureRegion[2];
            TextureRegion standByRightFrame = new TextureRegion(right1, 0, 0, 13, 13);
            movingRightSheetFrames[0] = new TextureRegion(right1, 0, 0, 13, 13);
            movingRightSheetFrames[1] = new TextureRegion(right2, 0, 0, 13, 13);
            movingRightAnimations.put(colour, new Animation<>(0.05F, movingRightSheetFrames));
            standByRightAnimations.put(colour, new Animation<>(0.05F, standByRightFrame));

            Gdx.app.log("Assets", "Successfully loaded animations for " + colour + " tank");
        } catch (Exception e) {
            Gdx.app.error("Assets", "Error loading animations for " + colour + " tank: " + e.getMessage());
        }
    }

    public static void loadSounds() {
        try {
            levelBeginSound = Gdx.audio.newSound(Gdx.files.internal("sounds/startLevel.mp3"));
            explosionSound = Gdx.audio.newSound(Gdx.files.internal("sounds/explosion.mp3"));
            hitSound = Gdx.audio.newSound(Gdx.files.internal("sounds/hit.mp3"));
        } catch (Exception e) {
            Gdx.app.error("Assets", "Error loading sounds: " + e.getMessage());
            levelBeginSound = null;
            explosionSound = null;
            hitSound = null;
        }
    }

    public static void loadUITextures() {
        try {
            // Load textures for pause and game over as separate files
            pauseTexture = new Texture(Gdx.files.internal("sprites/ui/pause.png"));
            gameOverTexture = new Texture(Gdx.files.internal("sprites/ui/gameover.png"));

            // Create sprites from textures and flip them vertically,
            // since the camera is flipped (setToOrtho(true) in GameScreen)
            Sprite pauseSprite = new Sprite(pauseTexture);
            pauseSprite.flip(false, true);
            pauseTexture = pauseSprite.getTexture();
            enemyIcon = new Texture(Gdx.files.internal("sprites/tanks/icon/image.png"));
            healthIcon = new Texture(Gdx.files.internal("sprites/tanks/icon/iconHealth.png"));
            pixel = new Texture(Gdx.files.internal("sprites/ui/pixel.png"));


            Sprite gameOverSprite = new Sprite(gameOverTexture);
            gameOverSprite.flip(false, true);
            gameOverTexture = gameOverSprite.getTexture();
        } catch (Exception e) {
            Gdx.app.error("Assets", "Error loading UI textures: " + e.getMessage());
            pauseTexture = null;
            gameOverTexture = null;
            healthIcon = null;
        }
    }

    public static void loadCurtainTextures() {
        try {
            // Create a simple gray texture for curtains
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.GRAY);
            pixmap.fill();
            curtainTexture = new Texture(pixmap);
            pixmap.dispose();
        } catch (Exception e) {
            Gdx.app.error("Assets", "Error loading curtain textures: " + e.getMessage());
            // Don't set curtainTexture to null to avoid NullPointerException
            try {
                // Try creating the texture again
                Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
                pixmap.setColor(Color.GRAY);
                pixmap.fill();
                curtainTexture = new Texture(pixmap);
                pixmap.dispose();
            } catch (Exception ex) {
                Gdx.app.error("Assets", "Critical error creating curtain texture: " + ex.getMessage());
                curtainTexture = null;
            }
        }
    }

    public static void loadStageTextures() {
        try {
            // Load texture for "STAGE" text
            stageTexture = new Texture(Gdx.files.internal("sprites/ui/stage.png"));

            // Load textures for numbers (assuming we have 8 levels)
            numberTextures = new Texture[9]; // 0-8
            for (int i = 1; i <= 8; i++) {
                try {
                    numberTextures[i] = new Texture(Gdx.files.internal("sprites/ui/number" + i + ".png"));
                } catch (Exception e) {
                    Gdx.app.error("Assets", "Error loading number texture " + i + ": " + e.getMessage());
                    // If failed to load texture for a specific number, create an empty one
                    Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
                    pixmap.setColor(Color.CLEAR);
                    pixmap.fill();
                    numberTextures[i] = new Texture(pixmap);
                    pixmap.dispose();
                }
            }
        } catch (Exception e) {
            Gdx.app.error("Assets", "Error loading stage textures: " + e.getMessage());
            stageTexture = null;
            numberTextures = null;
        }
    }

    public static void loadScoreScreenTextures() {
        try {
            // Load digit textures (0–9) and flip vertically
            digitTextures = new Texture[10];
            for (int i = 0; i < 10; i++) {
                TextureRegion region = new TextureRegion(new Texture(Gdx.files.internal("sprites/ui/digit" + i + ".png")));
                region.flip(false, true);
                Pixmap pixmap = region.getTexture().getTextureData().consumePixmap();
                digitTextures[i] = new Texture(pixmap);
                pixmap.dispose();
            }

            // Flip all UI textures vertically using TextureRegion
            hiScoreTexture = flip("sprites/ui/hi-score.png");
            iPlayerTexture = flip("sprites/ui/i-player.png");
            iiPlayerTexture = flip("sprites/ui/ii-player.png");
            ptsTexture = flip("sprites/ui/pts.png");
            totalTexture = flip("sprites/ui/total.png");
            tankIconTexture = flip("sprites/ui/tank-icon.png");
            arrowTexture = flip("sprites/ui/arrow.png");

            Gdx.app.log("Assets", "Score screen textures flipped and loaded.");
        } catch (Exception e) {
            Gdx.app.error("Assets", "Error loading score screen textures: " + e.getMessage());
        }
    }

    public static Texture flip(String path) {
        Texture tex = new Texture(Gdx.files.internal(path));
        TextureRegion reg = new TextureRegion(tex);
        reg.flip(false, true);
        Pixmap pixmap = reg.getTexture().getTextureData().consumePixmap();
        Texture flipped = new Texture(pixmap);
        pixmap.dispose();
        tex.dispose();
        return flipped;
    }


    public static void loadExplosionAnimation() {
        try {
            explosionTextures = new Texture[4];
            explosionFrames = new TextureRegion[4];
            for (int i = 0; i < 4; i++) {
                String filePath = "sprites/effects/explosion" + (i + 1) + ".png";
                System.out.println("этот бро  по идее должен заработать");
                try {
                    explosionTextures[i] = new Texture(Gdx.files.internal(filePath));
                    explosionFrames[i] = new TextureRegion(explosionTextures[i]);
                    // Переворачиваем текстуру по вертикали для соответствия системе координат
                    explosionFrames[i].flip(false, true);
                    Gdx.app.log("Assets", "Успешно загружен " + filePath);
                } catch (Exception e) {
                    Gdx.app.error("Assets", "Не удалось загрузить " + filePath + ": " + e.getMessage());
                    throw e;
                }
            }
            // Увеличиваем длительность кадра для лучшей видимости
            explosionAnimation = new Animation<>(0.15f, explosionFrames);
            Gdx.app.log("Assets", "Успешно загружена анимация взрыва с 4 кадрами");
        } catch (Exception e) {
            Gdx.app.error("Assets", "Ошибка загрузки анимации взрыва: " + e.getMessage());
            explosionAnimation = null;
        }
    }

public static void dispose() {
    if (textureBack != null) textureBack.dispose();
    if (yellowTankRight1_Texture != null) yellowTankRight1_Texture.dispose();
    if (yellowTankRight2_Texture != null) yellowTankRight2_Texture.dispose();
    if (selectionSound != null) selectionSound.dispose();
    if (levelBeginSound != null) levelBeginSound.dispose();
    if (explosionSound != null) explosionSound.dispose();
    if (hitSound != null) hitSound.dispose();
    if (tileSet != null) tileSet.dispose();
    if (pauseTexture != null) pauseTexture.dispose();
    if (gameOverTexture != null) gameOverTexture.dispose();
    if (enemyIcon != null) enemyIcon.dispose();
    if (healthIcon != null) healthIcon.dispose();
    if (pixel != null) pixel.dispose();
    if (curtainTexture != null) curtainTexture.dispose();
    if (stageTexture != null) stageTexture.dispose();

    // Dispose explosion textures
    if (explosionTextures != null) {
        for (Texture texture : explosionTextures) {
            if (texture != null) texture.dispose();
        }
    }

    // Dispose number textures
    if (numberTextures != null) {
        for (Texture texture : numberTextures) {
            if (texture != null) texture.dispose();
        }
    }

    // Dispose score screen textures
    if (digitTextures != null) {
        for (Texture texture : digitTextures) {
            if (texture != null) texture.dispose();
        }
    }
    if (hiScoreTexture != null) hiScoreTexture.dispose();
    if (iPlayerTexture != null) iPlayerTexture.dispose();
    if (iiPlayerTexture != null) iiPlayerTexture.dispose();
    if (ptsTexture != null) ptsTexture.dispose();
    if (totalTexture != null) totalTexture.dispose();
    if (tankIconTexture != null) tankIconTexture.dispose();
    if (arrowTexture != null) arrowTexture.dispose();

    // Dispose all animations
    disposeAnimations();
}

private static void disposeAnimations() {
    // This method would dispose all animation textures if needed
    // Note: Since animations use textures that are already disposed elsewhere,
    // we don't need to dispose them again here
}
}
