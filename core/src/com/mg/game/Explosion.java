package com.mg.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Explosion {
    private float positionX;
    private float positionY;
    private Animation<TextureRegion> animation;
    private float stateTime;
    private boolean finished;

    public Explosion(float x, float y) {
        // Центрируем взрыв относительно точки попадания
        this.positionX = x - 14; // Смещаем на половину размера взрыва
        this.positionY = y - 14; // Смещаем на половину размера взрыва
        this.animation = Assets.explosionAnimation;
        this.stateTime = 0f;
        this.finished = (animation == null); // Если анимация не загрузилась, сразу завершаем

        // Добавляем лог для отладки
        if (animation == null) {
            Gdx.app.error("Explosion", "Анимация взрыва не загружена");
        } else {
            Gdx.app.log("Explosion", "Создан взрыв на позиции " + positionX + ", " + positionY);
        }
    }

    public void update(float delta) {
        if (animation == null) {
            finished = true;
            return;
        }
        stateTime += delta;

        // Проверяем, завершилась ли анимация
        if (animation.getAnimationDuration() <= stateTime) {
            finished = true;
            Gdx.app.log("Explosion", "Анимация взрыва завершена после " + stateTime + " секунд");
        }
    }

    public TextureRegion getCurrentFrame() {
        if (animation == null) {
            Gdx.app.error("Explosion", "Попытка получить кадр из null анимации");
            return null;
        }

        TextureRegion frame = animation.getKeyFrame(stateTime, false); // false означает, что анимация не зацикливается
        if (frame == null) {
            Gdx.app.error("Explosion", "Получен null кадр для времени " + stateTime);
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