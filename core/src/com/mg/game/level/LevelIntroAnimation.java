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
    // Состояния анимации
    public enum State {
        CURTAINS_CLOSING, // Шторки закрываются
        SHOW_STAGE_TEXT,  // Показываем текст "STAGE X"
        CURTAINS_OPENING, // Шторки открываются
        FINISHED          // Анимация завершена
    }

    private State currentState;
    private float stateTime;
    private float curtainTopY;      // Позиция верхней шторки
    private float curtainBottomY;   // Позиция нижней шторки
    private float curtainHeight;    // Высота шторки
    private float curtainSpeed;     // Скорость движения шторок
    private int levelNumber;        // Номер уровня
    private BitmapFont font;        // Шрифт для текста "STAGE X" (запасной вариант)
    private float textAlpha;        // Прозрачность текста
    private float textDisplayTime;  // Время отображения текста
    private boolean isFinished;     // Флаг завершения анимации

    // Текстуры для шторок и текста
    private Texture curtainTexture;
    private Texture stageTexture;
    private Texture numberTexture;

    // Масштаб для отображения текстур
    private float textScale = 2.0f; // Увеличиваем размер в 2 раза

    // Цвет шторок #636363
    private Color curtainColor;

    // Настройки позиционирования шторок
    private float topCurtainFinalY;     // Конечная позиция верхней шторки
    private float bottomCurtainFinalY;  // Конечная позиция нижней шторки
    private float centerGap;            // Расстояние между шторками в центре

    // Флаг для определения, перевернута ли система координат
    private boolean isYFlipped;

    public LevelIntroAnimation(int levelNumber) {
        this.levelNumber = levelNumber;
        this.currentState = State.CURTAINS_CLOSING;
        this.stateTime = 0f;

        // Проверяем, перевернута ли система координат
        // В GameScreen используется camera.setToOrtho(true, ...), что означает перевернутую систему
        this.isYFlipped = true; // Предполагаем, что система координат перевернута

        // Настройка размера шторок
        this.curtainHeight = Gdx.graphics.getHeight() / 2f + (centerGap / 2f);

        // Вычисляем конечные позиции шторок с учетом перевернутой системы координат
        if (isYFlipped) {
            float centerY = Gdx.graphics.getHeight() / 2f;
            this.topCurtainFinalY = centerY - curtainHeight;
            this.bottomCurtainFinalY = centerY;



            // Начальные позиции шторок (за пределами экрана)
            this.curtainTopY = -curtainHeight; // Верхняя шторка полностью за верхней границей экрана
            this.curtainBottomY = Gdx.graphics.getHeight(); // Нижняя шторка начинается с нижней границы экрана
        } else {
            // В обычной системе Y=0 внизу экрана, Y=height вверху
            this.topCurtainFinalY = (Gdx.graphics.getHeight() + centerGap) / 2;
            this.bottomCurtainFinalY = (Gdx.graphics.getHeight() - centerGap) / 2 - curtainHeight;

            // Начальные позиции шторок (за пределами экрана)
            this.curtainTopY = Gdx.graphics.getHeight(); // Верхняя шторка начинается с верхней границы экрана
            this.curtainBottomY = -curtainHeight; // Нижняя шторка полностью за нижней границей экрана
        }

        // Добавляем отладочный вывод
        Gdx.app.log("LevelIntroAnimation", "Screen height: " + Gdx.graphics.getHeight());
        Gdx.app.log("LevelIntroAnimation", "Curtain height: " + curtainHeight);
        Gdx.app.log("LevelIntroAnimation", "Initial top curtain Y: " + curtainTopY);
        Gdx.app.log("LevelIntroAnimation", "Final top curtain Y: " + topCurtainFinalY);

        this.curtainSpeed = Gdx.graphics.getHeight() / 1.5f;  // Скорость движения шторок
        this.textAlpha = 0f;
        this.textDisplayTime = 2.0f;                          // Время отображения текста "STAGE X"
        this.isFinished = false;

        // Создаем шрифт для текста "STAGE X" (запасной вариант)
        font = new BitmapFont(Gdx.files.internal("fonts/pixel.fnt"));
        font.getData().setScale(5.0f); // Большой размер шрифта
        font.setColor(Color.WHITE);

        // Создаем цвет #636363 (99, 99, 99 в RGB)
        curtainColor = new Color(0x99/255f, 0x99/255f, 0x99/255f, 1);

        // Создаем текстуру для шторок
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(curtainColor);
        pixmap.fill();
        curtainTexture = new Texture(pixmap);
        pixmap.dispose();

        // Загружаем текстуры для "STAGE" и номера
        loadStageTextures();
    }

    private void loadStageTextures() {
        try {
            // Загружаем текстуру для "STAGE"
            stageTexture = new Texture(Gdx.files.internal("sprites/ui/stage.png"));
            Gdx.app.log("LevelIntroAnimation", "Loaded stage.png: " + stageTexture.getWidth() + "x" + stageTexture.getHeight());

            // Загружаем текстуру для номера уровня
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
                    // В перевернутой системе координат
                    // Двигаем верхнюю шторку вниз (увеличиваем Y)
                    curtainTopY += curtainSpeed * delta;
                    // Двигаем нижнюю шторку вверх (уменьшаем Y)
                    curtainBottomY -= curtainSpeed * delta;

                    // Отладочный вывод
                    if (stateTime < 0.1f) {
                        Gdx.app.log("LevelIntroAnimation", "Top curtain Y: " + curtainTopY);
                    }

                    // Проверяем, достигли ли шторки своих конечных позиций
                    if (curtainTopY >= topCurtainFinalY && curtainBottomY <= bottomCurtainFinalY) {
                        // Фиксируем позиции шторок точно на конечных позициях
                        curtainTopY = topCurtainFinalY;
                        curtainBottomY = bottomCurtainFinalY;

                        currentState = State.SHOW_STAGE_TEXT;
                        stateTime = 0f;

                        // Воспроизводим звук начала уровня
                        if (Assets.levelBeginSound != null) {
                            Assets.levelBeginSound.play();
                        }
                    }
                } else {
                    // В обычной системе координат
                    // Двигаем верхнюю шторку вниз (уменьшаем Y)
                    curtainTopY -= curtainSpeed * delta;
                    // Двигаем нижнюю шторку вверх (увеличиваем Y)
                    curtainBottomY += curtainSpeed * delta;

                    // Проверяем, достигли ли шторки своих конечных позиций
                    if (curtainTopY <= topCurtainFinalY && curtainBottomY >= bottomCurtainFinalY) {
                        // Фиксируем позиции шторок точно на конечных позициях
                        curtainTopY = topCurtainFinalY;
                        curtainBottomY = bottomCurtainFinalY;

                        currentState = State.SHOW_STAGE_TEXT;
                        stateTime = 0f;

                        // Воспроизводим звук начала уровня
                        if (Assets.levelBeginSound != null) {
                            Assets.levelBeginSound.play();
                        }
                    }
                }
                break;

            case SHOW_STAGE_TEXT:
                // Плавно показываем текст
                if (stateTime < 0.5f) {
                    textAlpha = stateTime / 0.5f; // Плавное появление за 0.5 секунды
                } else if (stateTime > textDisplayTime - 0.5f) {
                    textAlpha = (textDisplayTime - stateTime) / 0.5f; // Плавное исчезновение за 0.5 секунды
                } else {
                    textAlpha = 1.0f;
                }

                // Если время показа текста истекло
                if (stateTime >= textDisplayTime) {
                    currentState = State.CURTAINS_OPENING;
                    stateTime = 0f;
                }
                break;

            case CURTAINS_OPENING:
                if (isYFlipped) {
                    // В перевернутой системе координат
                    // Двигаем верхнюю шторку вверх (уменьшаем Y)
                    curtainTopY -= curtainSpeed * delta;
                    // Двигаем нижнюю шторку вниз (увеличиваем Y)
                    curtainBottomY += curtainSpeed * delta;

                    // Если шторки ушли за пределы экрана
                    if (curtainTopY <= -curtainHeight && curtainBottomY >= Gdx.graphics.getHeight()) {
                        currentState = State.FINISHED;
                        isFinished = true;
                    }
                } else {
                    // В обычной системе координат
                    // Двигаем верхнюю шторку вверх (увеличиваем Y)
                    curtainTopY += curtainSpeed * delta;
                    // Двигаем нижнюю шторку вниз (уменьшаем Y)
                    curtainBottomY -= curtainSpeed * delta;

                    // Если шторки ушли за пределы экрана
                    if (curtainTopY >= Gdx.graphics.getHeight() && curtainBottomY <= -curtainHeight) {
                        currentState = State.FINISHED;
                        isFinished = true;
                    }
                }
                break;

            case FINISHED:
                // Ничего не делаем, анимация завершена
                break;
        }
    }

    public void render(SpriteBatch batch) {
        // Верхняя штора
        batch.draw(curtainTexture, 0, curtainTopY, Gdx.graphics.getWidth(), curtainHeight);

        // Нижняя штора
        batch.draw(curtainTexture, 0, curtainBottomY, Gdx.graphics.getWidth(), curtainHeight);

        // Текст "STAGE X"
        if (currentState == State.SHOW_STAGE_TEXT && textAlpha > 0f) {
            font.setColor(1, 1, 1, textAlpha);
            String text = "STAGE " + levelNumber;
            GlyphLayout layout = new GlyphLayout(font, text);
            float textX = (Gdx.graphics.getWidth() - layout.width) / 2f;

            // Центр между шторками
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