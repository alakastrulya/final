package com.mg.game;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Tank {
    private String colour;
    private int level;
    public static Sound levelBeginSound;

    public int positionX;
    public int positionY;
    private TankState currentState;
    private Direction direction; // Новое поле для хранения направления

    // Перечисление для направлений
    public enum Direction {
        FORWARD, BACKWARD, LEFT, RIGHT
    }

    public Tank(String colour, int level) {
        this.colour = colour;
        this.level = level;
        this.positionX = 0;
        this.positionY = 0;
        this.currentState = new StandingByState(this);
        this.direction = Direction.FORWARD; // Начальное направление - вниз
    }

    public void handleInput(int keycode, float stateTime) {
        currentState.handleInput(keycode, stateTime);
    }

    public void setState(TankState state) {
        this.currentState = state;
    }

    public TextureRegion getCurrentFrame() {
        return currentState.getCurrentFrame();
    }

    public void moveUp() {
        if (!(positionY - 1 < 0)) {
            positionY -= 1;
        }
    }

    public void moveDown() {
        if (!(positionY + 1 > 454)) {
            positionY += 1;
        }
    }

    public void moveLeft() {
        if (!(positionX - 1 < 0)) {
            positionX -= 1;
        }
    }

    public void moveRight() {
        if (!(positionX + 1 > 454)) {
            positionX += 1;
        }
    }

    public void shoot() {
        // Логика стрельбы (без изменений)
    }

    public String getColour() {
        return colour;
    }

    public void setColour(String colour) {
        this.colour = colour;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }
}