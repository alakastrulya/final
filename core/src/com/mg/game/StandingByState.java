package com.mg.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class StandingByState implements TankState {
    private Tank tank;

    public StandingByState(Tank tank) {
        this.tank = tank;
    }

    @Override
    public void handleInput(int keycode, float stateTime) {
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
    public TextureRegion getCurrentFrame(float stateTime) {
        switch (tank.getDirection()) {
            case FORWARD:
                return tank.getStandByForwardAnimation().getKeyFrame(stateTime, true);
            case BACKWARD:
                return tank.getStandByBackwardAnimation().getKeyFrame(stateTime, true);
            case LEFT:
                return tank.getStandByLeftAnimation().getKeyFrame(stateTime, true);
            case RIGHT:
                return tank.getStandByRightAnimation().getKeyFrame(stateTime, true);
            default:
                return tank.getStandByForwardAnimation().getKeyFrame(stateTime, true);
        }
    }
}