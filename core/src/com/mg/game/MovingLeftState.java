package com.mg.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class MovingLeftState implements TankState {
    private Tank tank;
    private float stateTime;

    public MovingLeftState(Tank tank) {
        this.tank = tank;
        this.stateTime = 0;
    }

    @Override
    public void handleInput(int keycode, float stateTime) {
        this.stateTime = stateTime;
        if (keycode == Input.Keys.LEFT) {
            tank.moveLeft();
            tank.setDirection(Tank.Direction.LEFT); // Устанавливаем направление влево
        } else if (keycode == Input.Keys.RIGHT) {
            tank.setState(new MovingRightState(tank));
        } else if (keycode == Input.Keys.UP) {
            tank.setState(new MovingBackwardState(tank));
        } else if (keycode == Input.Keys.DOWN) {
            tank.setState(new MovingForwardState(tank));
        } else if (keycode == Input.Keys.SPACE) {
            tank.shoot();
        } else {
            tank.setState(new StandingByState(tank));
        }
    }

    @Override
    public TextureRegion getCurrentFrame() {
        return Assets.movingLeftAnimation.getKeyFrame(stateTime, true);
    }
}