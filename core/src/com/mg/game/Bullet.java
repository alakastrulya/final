package com.mg.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public class Bullet {
    private float positionX;
    private float positionY;
    private Tank.Direction direction;
    private float speed = 5f;
    private TextureRegion texture;
    private boolean active;

    public Bullet(float startX, float startY, Tank.Direction direction, String tankColour) {
        this.positionX = startX;
        this.positionY = startY;
        this.direction = direction;
        this.active = true;

        // Создаём текстуру для снаряда (маленький прямоугольник)
        Pixmap pixmap = new Pixmap(4, 4, Pixmap.Format.RGBA8888);
        if (tankColour.equals("yellow")) {
            pixmap.setColor(Color.YELLOW);
        } else if (tankColour.equals("green")) {
            pixmap.setColor(Color.GREEN);
        } else {
            pixmap.setColor(Color.WHITE);
        }
        pixmap.fill();
        this.texture = new TextureRegion(new Texture(pixmap));
        pixmap.dispose();
    }

    public void update(float delta) {
        if (!active) return;

        // Двигаем снаряд в зависимости от направления
        switch (direction) {
            case FORWARD:
                positionY += speed;
                break;
            case BACKWARD:
                positionY -= speed;
                break;
            case LEFT:
                positionX -= speed;
                break;
            case RIGHT:
                positionX += speed;
                break;
        }

        // Если снаряд вышел за пределы экрана, деактивируем его
        if (positionX < 0 || positionX > 480 || positionY < 0 || positionY > 480) {
            active = false;
        }
    }

    public float getPositionX() {
        return positionX;
    }

    public float getPositionY() {
        return positionY;
    }

    public TextureRegion getTexture() {
        return texture;
    }

    public boolean isActive() {
        return active;
    }

    public void dispose() {
        texture.getTexture().dispose();
    }

    public Rectangle getBounds() {
        return new Rectangle(positionX, positionY, 4, 4);
    }
}