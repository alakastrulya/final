package com.mg.game.tank.state;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mg.game.assets.Assets;
import com.mg.game.tank.Tank;
import com.mg.game.tank.TankState;

public class StandingByState implements TankState {
    private Tank tank;

    public StandingByState(Tank tank) {
        this.tank = tank;
    }

    @Override
    public void handleInput(int keycode, float stateTime) {
        if (keycode == Input.Keys.UP || keycode == Input.Keys.W) {
            tank.setState(new MovingBackwardState(tank));
        } else if (keycode == Input.Keys.DOWN || keycode == Input.Keys.S) {
            tank.setState(new MovingForwardState(tank));
        } else if (keycode == Input.Keys.LEFT || keycode == Input.Keys.A) {
            tank.setState(new MovingLeftState(tank));
        } else if (keycode == Input.Keys.RIGHT || keycode == Input.Keys.D) {
            tank.setState(new MovingRightState(tank));
        } else if (keycode == Input.Keys.SPACE || keycode == Input.Keys.ENTER) {
            tank.shoot();
        }
    }

    @Override
    public TextureRegion getCurrentFrame(float stateTime) {
        switch (tank.getDirection()) {
            case FORWARD:
                return Assets.getStandByForwardAnimation(tank.getColour()).getKeyFrame(stateTime, true);
            case BACKWARD:
                return Assets.getStandByBackwardAnimation(tank.getColour()).getKeyFrame(stateTime, true);
            case LEFT:
                return Assets.getStandByLeftAnimation(tank.getColour()).getKeyFrame(stateTime, true);
            case RIGHT:
                return Assets.getStandByRightAnimation(tank.getColour()).getKeyFrame(stateTime, true);
            default:
                return Assets.getStandByForwardAnimation(tank.getColour()).getKeyFrame(stateTime, true);
        }
    }
}