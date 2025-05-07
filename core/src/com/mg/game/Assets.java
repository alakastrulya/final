package com.mg.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.util.HashMap;
import java.util.Map;

public class Assets {
    public static Texture textureBack;
    public static Sprite spriteBack;
    public static Texture yellowTankRight1_Texture;
    public static Texture yellowTankRight2_Texture;
    public static Animation<TextureRegion> movingTankAnimation;
    public static TextureRegion[] sheet_frames;
    public static TextureRegion current_frame;
    public static Sprite levelBack;
    public static Sound selectionSound;
    public static Sound levelBeginSound;

    // Карты для хранения анимаций для каждого цвета
    private static Map<String, Animation<TextureRegion>> movingForwardAnimations = new HashMap<>();
    private static Map<String, Animation<TextureRegion>> standByForwardAnimations = new HashMap<>();
    private static Map<String, Animation<TextureRegion>> movingBackwardAnimations = new HashMap<>();
    private static Map<String, Animation<TextureRegion>> standByBackwardAnimations = new HashMap<>();
    private static Map<String, Animation<TextureRegion>> movingLeftAnimations = new HashMap<>();
    private static Map<String, Animation<TextureRegion>> standByLeftAnimations = new HashMap<>();
    private static Map<String, Animation<TextureRegion>> movingRightAnimations = new HashMap<>();
    private static Map<String, Animation<TextureRegion>> standByRightAnimations = new HashMap<>();

    // Текстуры для карты
    public static Texture tileSet;

    // Текстуры для интерфейса
    public static Texture pauseTexture;
    public static Texture gameOverTexture;

    public static float elapsedTime;
    public static String colour;
    public static int level;

    // Методы для получения анимаций для конкретного цвета
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

            // Загружаем текстуры для интерфейса
            loadUITextures();
        } catch (Exception e) {
            Gdx.app.error("Assets", "Error loading menu assets: " + e.getMessage());
        }
    }

    public static void loadGameAssets(String colour, int level) {
        loadLevel(1);
        loadTankAnimations(colour, level);
        loadSounds();
        loadUITextures();
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

            // Создаем анимации для движения вперед
            TextureRegion[] movingForwardSheetFrames = new TextureRegion[2];
            TextureRegion standByForwardFrame = new TextureRegion(forward1, 0, 0, 13, 13);
            movingForwardSheetFrames[0] = new TextureRegion(forward1, 0, 0, 13, 13);
            movingForwardSheetFrames[1] = new TextureRegion(forward2, 0, 0, 13, 13);
            movingForwardAnimations.put(colour, new Animation<>(0.1F, movingForwardSheetFrames));
            standByForwardAnimations.put(colour, new Animation<>(0.1F, standByForwardFrame));

            // Создаем анимации для движения назад
            TextureRegion[] movingBackwardSheetFrames = new TextureRegion[2];
            TextureRegion standByBackwardFrame = new TextureRegion(backward1, 0, 0, 13, 13);
            movingBackwardSheetFrames[0] = new TextureRegion(backward1, 0, 0, 13, 13);
            movingBackwardSheetFrames[1] = new TextureRegion(backward2, 0, 0, 13, 13);
            movingBackwardAnimations.put(colour, new Animation<>(0.1F, movingBackwardSheetFrames));
            standByBackwardAnimations.put(colour, new Animation<>(0.1F, standByBackwardFrame));

            // Создаем анимации для движения влево
            TextureRegion[] movingLeftSheetFrames = new TextureRegion[2];
            TextureRegion standByLeftFrame = new TextureRegion(left1, 0, 0, 13, 13);
            movingLeftSheetFrames[0] = new TextureRegion(left1, 0, 0, 13, 13);
            movingLeftSheetFrames[1] = new TextureRegion(left2, 0, 0, 13, 13);
            movingLeftAnimations.put(colour, new Animation<>(0.1F, movingLeftSheetFrames));
            standByLeftAnimations.put(colour, new Animation<>(0.1F, standByLeftFrame));

            // Создаем анимации для движения вправо
            TextureRegion[] movingRightSheetFrames = new TextureRegion[2];
            TextureRegion standByRightFrame = new TextureRegion(right1, 0, 0, 13, 13);
            movingRightSheetFrames[0] = new TextureRegion(right1, 0, 0, 13, 13);
            movingRightSheetFrames[1] = new TextureRegion(right2, 0, 0, 13, 13);
            movingRightAnimations.put(colour, new Animation<>(0.1F, movingRightSheetFrames));
            standByRightAnimations.put(colour, new Animation<>(0.1F, standByRightFrame));

            Gdx.app.log("Assets", "Successfully loaded animations for " + colour + " tank");
        } catch (Exception e) {
            Gdx.app.error("Assets", "Error loading animations for " + colour + " tank: " + e.getMessage());
        }
    }

    public static void loadSounds() {
        try {
            levelBeginSound = Gdx.audio.newSound(Gdx.files.internal("sounds/startLevel.mp3"));
        } catch (Exception e) {
            Gdx.app.error("Assets", "Error loading sounds: " + e.getMessage());
            levelBeginSound = null;
        }
    }

    public static void loadLevel(int level) {
        try {
            Texture levelBase = new Texture(Gdx.files.internal("sprites/levels/levelBase.png"));
            levelBase.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            levelBack = new Sprite(levelBase);

            // Загружаем tileset для карты
            tileSet = new Texture(Gdx.files.internal("sprites/tiles/tileset.png"));
        } catch (Exception e) {
            Gdx.app.error("Assets", "Error loading level: " + e.getMessage());
            levelBack = null;
            tileSet = null;
        }
    }

    // Метод для загрузки текстур интерфейса из отдельных файлов
    public static void loadUITextures() {
        try {
            // Загружаем текстуры для паузы и конца игры как отдельные файлы
            pauseTexture = new Texture(Gdx.files.internal("sprites/ui/pause.png"));
            gameOverTexture = new Texture(Gdx.files.internal("sprites/ui/gameover.png"));

            // Создаем спрайты из текстур и переворачиваем их по вертикали,
            // так как камера перевернута (setToOrtho(true) в GameScreen)
            Sprite pauseSprite = new Sprite(pauseTexture);
            pauseSprite.flip(false, true);
            pauseTexture = pauseSprite.getTexture();

            Sprite gameOverSprite = new Sprite(gameOverTexture);
            gameOverSprite.flip(false, true);
            gameOverTexture = gameOverSprite.getTexture();
        } catch (Exception e) {
            Gdx.app.error("Assets", "Ошибка загрузки текстур интерфейса: " + e.getMessage());
            pauseTexture = null;
            gameOverTexture = null;
        }
    }

    public static void dispose() {
        if (textureBack != null) textureBack.dispose();
        if (yellowTankRight1_Texture != null) yellowTankRight1_Texture.dispose();
        if (yellowTankRight2_Texture != null) yellowTankRight2_Texture.dispose();
        if (selectionSound != null) selectionSound.dispose();
        if (levelBeginSound != null) levelBeginSound.dispose();
        if (tileSet != null) tileSet.dispose();
        if (pauseTexture != null) pauseTexture.dispose();
        if (gameOverTexture != null) gameOverTexture.dispose();
    }
}