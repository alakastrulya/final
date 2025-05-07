package com.mg.game;

<<<<<<< HEAD
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
=======
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
>>>>>>> ravil
import com.badlogic.gdx.math.Rectangle;

public class Bullet {
    private float positionX;
    private float positionY;
    private Tank.Direction direction;
<<<<<<< HEAD
    private Texture texture;
    private boolean active;
    private boolean fromEnemy;
    private static final float SPEED = 5.0f; // Скорость снаряда

    public Bullet(float startX, float startY, Tank.Direction direction, String tankColour) {
        this(startX, startY, direction, tankColour, false);
    }

    public Bullet(float startX, float startY, Tank.Direction direction, String tankColour, boolean fromEnemy) {
=======
    private float speed = 5f;
    private TextureRegion texture;
    private boolean active;

    public Bullet(float startX, float startY, Tank.Direction direction, String tankColour) {
>>>>>>> ravil
        this.positionX = startX;
        this.positionY = startY;
        this.direction = direction;
        this.active = true;
<<<<<<< HEAD
        this.fromEnemy = fromEnemy;
=======
>>>>>>> ravil

        // Создаём текстуру для снаряда (маленький прямоугольник)
        Pixmap pixmap = new Pixmap(4, 4, Pixmap.Format.RGBA8888);
        if (tankColour.equals("yellow")) {
<<<<<<< HEAD
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
=======
            pixmap.setColor(Color.YELLOW);
        } else if (tankColour.equals("green")) {
            pixmap.setColor(Color.GREEN);
        } else {
            pixmap.setColor(Color.WHITE);
        }
        pixmap.fill();
        this.texture = new TextureRegion(new Texture(pixmap));
>>>>>>> ravil
        pixmap.dispose();
    }

    public void update(float delta) {
<<<<<<< HEAD
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
=======
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
>>>>>>> ravil
            active = false;
        }
    }

<<<<<<< HEAD
    public Rectangle getBounds() {
        return new Rectangle(positionX, positionY, 4, 4);
    }

=======
>>>>>>> ravil
    public float getPositionX() {
        return positionX;
    }

    public float getPositionY() {
        return positionY;
    }

<<<<<<< HEAD
    public Texture getTexture() {
=======
    public TextureRegion getTexture() {
>>>>>>> ravil
        return texture;
    }

    public boolean isActive() {
        return active;
    }

<<<<<<< HEAD
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
=======
    public void dispose() {
        texture.getTexture().dispose();
    }

    public Rectangle getBounds() {
        return new Rectangle(positionX, positionY, 4, 4);
    }
}
>>>>>>> ravil
