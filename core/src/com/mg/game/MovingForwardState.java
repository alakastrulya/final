package com.mg.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class MovingForwardState implements TankState {
    private Tank tank;
    private float stateTime;

    public MovingForwardState(Tank tank) {
        this.tank = tank;
        this.stateTime = 0;
    }

    @Override
    public void handleInput(int keycode, float stateTime) {
        this.stateTime = stateTime;
        if (keycode == Input.Keys.DOWN) {
            tank.moveDown();
            tank.setDirection(Tank.Direction.FORWARD); // Устанавливаем направление вниз
        } else if (keycode == Input.Keys.UP) {
            tank.setState(new MovingBackwardState(tank));
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
        return Assets.movingForwardAnimation.getKeyFrame(stateTime, true);
    }
}