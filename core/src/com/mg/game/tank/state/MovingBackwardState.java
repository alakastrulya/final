package com.mg.game.tank.state;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mg.game.assets.Assets;
import com.mg.game.tank.Tank;
import com.mg.game.tank.TankState;

public class MovingBackwardState implements TankState {
    private Tank tank;

    public MovingBackwardState(Tank tank) {
        this.tank = tank;
    }

    @Override
    public void handleInput(int keycode, float stateTime) {
        if (keycode == Input.Keys.UP) {
            tank.setDirection(Tank.Direction.BACKWARD);
            tank.moveUp();
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

    public TextureRegion getCurrentFrame(float stateTime) {
        return Assets.getMovingBackwardAnimation(tank.getColour()).getKeyFrame(stateTime, true);
    }
}
