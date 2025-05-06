package com.mg.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Assets {

    public static Texture textureBack;
    public static Sprite spriteBack;
    public static Texture yellowTankRight1_Texture;
    public static Texture yellowTankRight2_Texture;
    public static Animation<TextureRegion> movingTankAnimation;
    public static TextureRegion[] sheet_frames;
    public static TextureRegion current_frame; // Добавляем поле current_frame
    public static Sprite levelBack;
    public static Sound selectionSound;
    public static Sound levelBeginSound;

    public static Animation<TextureRegion> movingForwardAnimation;
    public static Animation<TextureRegion> standByForwardAnimation;
    public static Animation<TextureRegion> movingBackwardAnimation;
    public static Animation<TextureRegion> standByBackwardAnimation;
    public static Animation<TextureRegion> movingLeftAnimation;
    public static Animation<TextureRegion> standByLeftAnimation;
    public static Animation<TextureRegion> movingRightAnimation;
    public static Animation<TextureRegion> standByRightAnimation;

    public static Texture tileSet; // новый tileset

    public static float elapsedTime;
    public static String colour;
    public static int level;

    public static void loadMenuAssets() {
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
    }

    public static void loadGameAssets(String colour, int level) {
        loadLevel(level);
        loadTankAnimations(colour, level);
        loadSounds();
    }

    public static void loadTankAnimations(String colour, int level) {
        Texture right1 = new Texture(Gdx.files.internal("sprites/tanks/" + colour + "/level_" + level + "/right1.png"));
        Texture right2 = new Texture(Gdx.files.internal("sprites/tanks/" + colour + "/level_" + level + "/right2.png"));
        Texture left1 = new Texture(Gdx.files.internal("sprites/tanks/" + colour + "/level_" + level + "/left1.png"));
        Texture left2 = new Texture(Gdx.files.internal("sprites/tanks/" + colour + "/level_" + level + "/left2.png"));
        Texture backward1 = new Texture(Gdx.files.internal("sprites/tanks/" + colour + "/level_" + level + "/down1.png"));
        Texture backward2 = new Texture(Gdx.files.internal("sprites/tanks/" + colour + "/level_" + level + "/down2.png"));
        Texture forward1 = new Texture(Gdx.files.internal("sprites/tanks/" + colour + "/level_" + level + "/up1.png"));
        Texture forward2 = new Texture(Gdx.files.internal("sprites/tanks/" + colour + "/level_" + level + "/up2.png"));

        TextureRegion[] movingForwardSheetFrames = new TextureRegion[2];
        TextureRegion standByForwardFrame = new TextureRegion(forward1, 0, 0, 13, 13);
        movingForwardSheetFrames[0] = new TextureRegion(forward1, 0, 0, 13, 13);
        movingForwardSheetFrames[1] = new TextureRegion(forward2, 0, 0, 13, 13);
        movingForwardAnimation = new Animation<>(0.1F, movingForwardSheetFrames);
        standByForwardAnimation = new Animation<>(0.1F, standByForwardFrame);

        TextureRegion[] movingBackwardSheetFrames = new TextureRegion[2];
        TextureRegion standByBackwardFrame = new TextureRegion(backward1, 0, 0, 13, 13);
        movingBackwardSheetFrames[0] = new TextureRegion(backward1, 0, 0, 13, 13);
        movingBackwardSheetFrames[1] = new TextureRegion(backward2, 0, 0, 13, 13);
        movingBackwardAnimation = new Animation<>(0.1F, movingBackwardSheetFrames);
        standByBackwardAnimation = new Animation<>(0.1F, standByBackwardFrame);

        TextureRegion[] movingLeftSheetFrames = new TextureRegion[2];
        TextureRegion standByLeftFrame = new TextureRegion(left1, 0, 0, 13, 13);
        movingLeftSheetFrames[0] = new TextureRegion(left1, 0, 0, 13, 13);
        movingLeftSheetFrames[1] = new TextureRegion(left2, 0, 0, 13, 13);
        movingLeftAnimation = new Animation<>(0.1F, movingLeftSheetFrames);
        standByLeftAnimation = new Animation<>(0.1F, standByLeftFrame);

        TextureRegion[] movingRightSheetFrames = new TextureRegion[2];
        TextureRegion standByRightFrame = new TextureRegion(right1, 0, 0, 13, 13);
        movingRightSheetFrames[0] = new TextureRegion(right1, 0, 0, 13, 13);
        movingRightSheetFrames[1] = new TextureRegion(right2, 0, 0, 13, 13);
        movingRightAnimation = new Animation<>(0.1F, movingRightSheetFrames);
        standByRightAnimation = new Animation<>(0.1F, standByRightFrame);
    }

    public static void loadSounds() {
        levelBeginSound = Gdx.audio.newSound(Gdx.files.internal("sounds/startLevel.mp3"));
    }

    public static void loadLevel(int level) {
        Texture levelBase = new Texture(Gdx.files.internal("sprites/levels/levelBase.png"));
        levelBase.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        levelBack = new Sprite(levelBase);

        // Загружаем tileset для карты
        tileSet = new Texture(Gdx.files.internal("sprites/tiles/tileset.png")); // здесь файл tileset.png
    }
}
