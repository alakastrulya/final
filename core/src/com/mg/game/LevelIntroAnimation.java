package com.mg.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Align;

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

    public LevelIntroAnimation(int levelNumber) {
        this.levelNumber = levelNumber;
        this.currentState = State.CURTAINS_CLOSING;
        this.stateTime = 0f;

        // Вот здесь регулируется размер шторок - увеличиваем до 45% экрана
        this.curtainHeight = Gdx.graphics.getHeight() * 0.99f;

        // Начальные позиции шторок (за пределами экрана)
        this.curtainTopY = -curtainHeight;
        this.curtainBottomY = Gdx.graphics.getHeight();

        this.curtainSpeed = Gdx.graphics.getHeight() / 1.5f;  // Скорость движения шторок
        this.textAlpha = 0f;
        this.textDisplayTime = 2.0f;                          // Время отображения текста "STAGE X"
        this.isFinished = false;

        // Создаем шрифт для текста "STAGE X" (запасной вариант)
        font = new BitmapFont(false);
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
                // Двигаем верхнюю шторку вниз
                curtainTopY += curtainSpeed * delta;
                // Двигаем нижнюю шторку вверх
                curtainBottomY -= curtainSpeed * delta;

                // Если шторки встретились в центре
                if (curtainTopY >= 0 && curtainBottomY <= Gdx.graphics.getHeight() - curtainHeight) {
                    currentState = State.SHOW_STAGE_TEXT;
                    stateTime = 0f;

                    // Воспроизводим звук начала уровня
                    if (Assets.levelBeginSound != null) {
                        Assets.levelBeginSound.play();
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
                // Двигаем верхнюю шторку вверх
                curtainTopY -= curtainSpeed * delta;
                // Двигаем нижнюю шторку вниз
                curtainBottomY += curtainSpeed * delta;

                // Если шторки ушли за пределы экрана
                if (curtainTopY <= -curtainHeight && curtainBottomY >= Gdx.graphics.getHeight()) {
                    currentState = State.FINISHED;
                    isFinished = true;
                }
                break;

            case FINISHED:
                // Ничего не делаем, анимация завершена
                break;
        }
    }

    public void render(SpriteBatch batch) {
        // Сохраняем текущий цвет пакетного рендерера
        Color oldColor = batch.getColor().cpy();

        // Рисуем верхнюю шторку с цветом #636363
        batch.setColor(curtainColor);
        batch.draw(curtainTexture, 0, curtainTopY, Gdx.graphics.getWidth(), curtainHeight);

        // Рисуем нижнюю шторку с цветом #636363
        batch.draw(curtainTexture, 0, curtainBottomY - curtainHeight, Gdx.graphics.getWidth(), curtainHeight);

        // Если в состоянии показа текста, рисуем "STAGE X"
        if (currentState == State.SHOW_STAGE_TEXT) {
            batch.setColor(1, 1, 1, textAlpha); // Устанавливаем прозрачность

            // Проверяем, загружены ли текстуры
            if (stageTexture != null && numberTexture != null) {
                // Вычисляем размеры с учетом масштаба
                float stageWidth = stageTexture.getWidth() * textScale;
                float stageHeight = stageTexture.getHeight() * textScale;
                float numberWidth = numberTexture.getWidth() * textScale;
                float numberHeight = numberTexture.getHeight() * textScale;

                // Вычисляем позиции для центрирования
                float totalWidth = stageWidth + 20 + numberWidth;
                float stageX = Gdx.graphics.getWidth() / 2 - totalWidth / 2;
                float numberX = stageX + stageWidth + 20;
                float stageY = Gdx.graphics.getHeight() / 2 - stageHeight / 2;
                float numberY = Gdx.graphics.getHeight() / 2 - numberHeight / 2;

                // Рисуем текстуры с увеличенным размером
                batch.draw(stageTexture, stageX, stageY, stageWidth, stageHeight);
                batch.draw(numberTexture, numberX, numberY, numberWidth, numberHeight);

                Gdx.app.log("LevelIntroAnimation", "Drawing stage and number textures");
            } else {
                // Запасной вариант - рендерим текст, если текстуры не загрузились
                String stageText = "STAGE " + levelNumber;
                GlyphLayout layout = new GlyphLayout(font, stageText);

                font.setColor(1, 1, 1, textAlpha);
                font.draw(batch, stageText,
                        Gdx.graphics.getWidth() / 2 - layout.width / 2,
                        Gdx.graphics.getHeight() / 2 + layout.height / 2);

                Gdx.app.log("LevelIntroAnimation", "Drawing fallback text");
            }
        }

        // Восстанавливаем цвет пакетного рендерера
        batch.setColor(oldColor);
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
