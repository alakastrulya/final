package com.mg.game;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public interface TankState {
    void handleInput(int keycode, float stateTime);
    TextureRegion getCurrentFrame(float stateTime);
}