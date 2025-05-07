package com.mg.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;

public class Bullet {
    private float positionX;
    private float positionY;
    private Tank.Direction direction;
    private Texture texture;
    private boolean active;
    private boolean fromEnemy;
    private static final float SPEED = 5.0f; // Скорость снаряда

    public Bullet(float startX, float startY, Tank.Direction direction, String tankColour) {
        this(startX, startY, direction, tankColour, false);
    }

    public Bullet(float startX, float startY, Tank.Direction direction, String tankColour, boolean fromEnemy) {
        this.positionX = startX;
        this.positionY = startY;
        this.direction = direction;
        this.active = true;
        this.fromEnemy = fromEnemy;

        // Создаём текстуру для снаряда (маленький прямоугольник)
        Pixmap pixmap = new Pixmap(4, 4, Pixmap.Format.RGBA8888);
        if (tankColour.equals("yellow")) {
            pixmap.setColor(1, 1, 0, 1); // Желтый цвет
        } else if (tankColour.equals("green")) {
            pixmap.setColor(0, 1, 0, 1); // Зеленый цвет
        } else if (tankColour.equals("red")) {
            pixmap.setColor(1, 0, 0, 1); // Красный цвет
        } else {
            pixmap.setColor(1, 1, 1, 1); // Белый цвет по умолчанию
        }
        pixmap.fillRectangle(0, 0, 4, 4);
        texture = new Texture(pixmap);
        pixmap.dispose();
    }

    public void update(float delta) {
        // Обновляем позицию снаряда в зависимости от направления
        switch (direction) {
            case FORWARD:
                positionY += SPEED;
                break;
            case BACKWARD:
                positionY -= SPEED;
                break;
            case LEFT:
                positionX -= SPEED;
                break;
            case RIGHT:
                positionX += SPEED;
                break;
        }

        // Проверяем, не вышел ли снаряд за пределы экрана
        if (positionX < 0 || positionX > Gdx.graphics.getWidth() ||
                positionY < 0 || positionY > Gdx.graphics.getHeight()) {
            active = false;
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(positionX, positionY, 4, 4);
    }

    public float getPositionX() {
        return positionX;
    }

    public float getPositionY() {
        return positionY;
    }

    public Texture getTexture() {
        return texture;
    }

    public boolean isActive() {
        return active;
    }

    public void deactivate() {
        active = false;
    }

    public boolean isFromEnemy() {
        return fromEnemy;
    }

    public void dispose() {
        if (texture != null) {
            texture.dispose();
        }
    }
}
