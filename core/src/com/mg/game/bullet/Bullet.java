package com.mg.game.bullet;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.mg.game.tank.Tank;

public class Bullet {
    private float positionX;
    private float positionY;
    private Tank.Direction direction;
    private TextureRegion texture;
    private boolean active;
    private boolean fromEnemy;
    private static final float SPEED = 5.0f;

    public Bullet(float startX, float startY, Tank.Direction direction, String tankColour) {
        this(startX, startY, direction, tankColour, false);
    }

    public Bullet(float startX, float startY, Tank.Direction direction, String tankColour, boolean fromEnemy) {
        this.positionX = startX;
        this.positionY = startY;
        this.direction = direction;
        this.active = true;
        this.fromEnemy = fromEnemy;

        Pixmap pixmap = new Pixmap(4, 4, Pixmap.Format.RGBA8888);
        if (tankColour.equals("yellow")) {
            pixmap.setColor(Color.YELLOW);
        } else if (tankColour.equals("green")) {
            pixmap.setColor(Color.GREEN);
        } else if (tankColour.equals("red")) {
            pixmap.setColor(Color.RED);
        } else {
            pixmap.setColor(Color.WHITE);
        }
        pixmap.fill();
        this.texture = new TextureRegion(new Texture(pixmap));
        pixmap.dispose();
    }

    public void update(float delta) {
        if (!active) return;

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

        if (positionX < 0 || positionX > Gdx.graphics.getWidth() ||
                positionY < 0 || positionY > Gdx.graphics.getHeight()) {
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

    public void deactivate() {
        active = false;
    }

    public boolean isFromEnemy() {
        return fromEnemy;
    }

    public void dispose() {
        if (texture != null && texture.getTexture() != null) {
            texture.getTexture().dispose();
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(positionX, positionY, 4, 4);
    }

    public Tank.Direction getDirection() {
        return direction;
    }
}
