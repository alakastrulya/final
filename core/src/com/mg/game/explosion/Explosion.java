package com.mg.game.explosion;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mg.game.assets.Assets;

public class Explosion {
    private float positionX;
    private float positionY;
    private Animation<TextureRegion> animation;
    private float stateTime;
    private boolean finished;

    public Explosion(float x, float y) {
        // Center the explosion around the impact point
        this.positionX = x - 14; // Shift by half the explosion size
        this.positionY = y - 14; // Shift by half the explosion size
        this.animation = Assets.explosionAnimation;
        this.stateTime = 0f;
        this.finished = (animation == null); // If animation is not loaded, mark as finished

        // Add log for debugging
        if (animation == null) {
            Gdx.app.error("Explosion", "Explosion animation not loaded");
        } else {
            Gdx.app.log("Explosion", "Explosion created at position " + positionX + ", " + positionY);
        }
    }

    public void update(float delta) {
        if (animation == null) {
            finished = true;
            return;
        }
        stateTime += delta;

        // Check if the animation is finished
        if (animation.getAnimationDuration() <= stateTime) {
            finished = true;
            Gdx.app.log("Explosion", "Explosion animation finished after " + stateTime + " seconds");
        }
    }

    public TextureRegion getCurrentFrame() {
        if (animation == null) {
            Gdx.app.error("Explosion", "Tried to get frame from null animation");
            return null;
        }

        TextureRegion frame = animation.getKeyFrame(stateTime, false); // false means animation doesn't loop
        if (frame == null) {
            Gdx.app.error("Explosion", "Null frame returned for time " + stateTime);
        }
        return frame;
    }

    public float getPositionX() {
        return positionX;
    }

    public float getPositionY() {
        return positionY;
    }

    public boolean isFinished() {
        return finished;
    }
}
