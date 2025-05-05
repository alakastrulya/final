package com.mg.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class MovingBackwardState implements TankState {
    private Tank tank;
    private float stateTime;

    public MovingBackwardState(Tank tank) {
        this.tank = tank;
        this.stateTime = 0;
    }

    @Override
    public void handleInput(int keycode, float stateTime) {
        this.stateTime = stateTime;
        if (keycode == Input.Keys.UP) {
            tank.moveUp();
            tank.setDirection(Tank.Direction.BACKWARD); // Устанавливаем направление вверх
        } else if (keycode == Input.Keys.DOWN) {
            tank.setState(new MovingForwardState(tank));
        } else if (keycode == Input.Keys.LEFT) {
            tank.setState(new MovingLeftState(tank));
        } else if (keycode == Input.Keys.RIGHT) {
            tank.setState(new MovingRightState(tank));
        } else if (keycode == Input.Keys.SPACE) {
            tank.shoot();
        } else {
            tank.setState(new StandingByState(tank));
        }
    }

    @Override
    public TextureRegion getCurrentFrame() {
        return Assets.movingBackwardAnimation.getKeyFrame(stateTime, true);
    }
}