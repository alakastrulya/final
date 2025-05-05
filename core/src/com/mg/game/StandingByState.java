package com.mg.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class StandingByState implements TankState {
    private Tank tank;
    private float stateTime;

    public StandingByState(Tank tank) {
        this.tank = tank;
        this.stateTime = 0;
    }

    @Override
    public void handleInput(int keycode, float stateTime) {
        this.stateTime = stateTime;
        if (keycode == Input.Keys.UP) {
            tank.setState(new MovingBackwardState(tank));
        } else if (keycode == Input.Keys.DOWN) {
            tank.setState(new MovingForwardState(tank));
        } else if (keycode == Input.Keys.LEFT) {
            tank.setState(new MovingLeftState(tank));
        } else if (keycode == Input.Keys.RIGHT) {
            tank.setState(new MovingRightState(tank));
        } else if (keycode == Input.Keys.SPACE) {
            tank.shoot();
        }
    }

    @Override
    public TextureRegion getCurrentFrame() {
        // Выбираем анимацию ожидания на основе текущего направления
        switch (tank.getDirection()) {
            case FORWARD:
                return Assets.standByForwardAnimation.getKeyFrame(stateTime, true);
            case BACKWARD:
                return Assets.standByBackwardAnimation.getKeyFrame(stateTime, true);
            case LEFT:
                return Assets.standByLeftAnimation.getKeyFrame(stateTime, true);
            case RIGHT:
                return Assets.standByRightAnimation.getKeyFrame(stateTime, true);
            default:
                return Assets.standByForwardAnimation.getKeyFrame(stateTime, true); // Запасной вариант
        }
    }
}