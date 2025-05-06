package com.mg.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Tank {
    private String colour;
    private int level;
    public static Sound levelBeginSound;

    public int positionX;
    public int positionY;
    private TankState currentState;
    private Direction direction;
    private boolean isEnemy;
    private float timeSinceLastMove;
    private float timeSinceLastShot;
    private static final float MOVE_INTERVAL = 3f; // Увеличиваем интервал до 3 секунд для более медленного движения врагов
    private static final float SHOOT_INTERVAL = 2f; // Интервал стрельбы для врагов (сек)

    // Анимации для каждого танка (оставляем private)
    private Animation<TextureRegion> movingForwardAnimation;
    private Animation<TextureRegion> standByForwardAnimation;
    private Animation<TextureRegion> movingBackwardAnimation;
    private Animation<TextureRegion> standByBackwardAnimation;
    private Animation<TextureRegion> movingLeftAnimation;
    private Animation<TextureRegion> standByLeftAnimation;
    private Animation<TextureRegion> movingRightAnimation;
    private Animation<TextureRegion> standByRightAnimation;
    private float stateTime;

    public enum Direction {
        FORWARD, BACKWARD, LEFT, RIGHT
    }

    public Tank(String colour, int level, boolean isEnemy) {
        this.colour = colour;
        this.level = level;
        this.isEnemy = isEnemy;
        this.positionX = 0;
        this.positionY = 0;
        this.currentState = new StandingByState(this);
        this.direction = Direction.FORWARD;
        this.stateTime = 0;
        this.timeSinceLastMove = 0;
        this.timeSinceLastShot = 0;
        loadAnimations();
    }

    private void loadAnimations() {
        try {
            Texture right1 = new Texture(Gdx.files.internal("sprites/tanks/" + colour + "/level_" + level + "/right1.png"));
            Texture right2 = new Texture(Gdx.files.internal("sprites/tanks/" + colour + "/level_" + level + "/right2.png"));
            Texture left1 = new Texture(Gdx.files.internal("sprites/tanks/" + colour + "/level_" + level + "/left1.png"));
            Texture left2 = new Texture(Gdx.files.internal("sprites/tanks/" + colour + "/level_" + level + "/left2.png"));
            Texture backward1 = new Texture(Gdx.files.internal("sprites/tanks/" + colour + "/level_" + level + "/down1.png"));
            Texture backward2 = new Texture(Gdx.files.internal("sprites/tanks/" + colour + "/level_" + level + "/down2.png"));
            Texture forward1 = new Texture(Gdx.files.internal("sprites/tanks/" + colour + "/level_" + level + "/up1.png"));
            Texture forward2 = new Texture(Gdx.files.internal("sprites/tanks/" + colour + "/level_" + level + "/up2.png"));

            TextureRegion[] movingForwardSheetFrames = new TextureRegion[2];
            TextureRegion standByForwardFrame = new TextureRegion(forward1, 0, 0, 13, 13);
            movingForwardSheetFrames[0] = new TextureRegion(forward1, 0, 0, 13, 13);
            movingForwardSheetFrames[1] = new TextureRegion(forward2, 0, 0, 13, 13);
            movingForwardAnimation = new Animation<>(0.1F, movingForwardSheetFrames);
            standByForwardAnimation = new Animation<>(0.1F, standByForwardFrame);

            TextureRegion[] movingBackwardSheetFrames = new TextureRegion[2];
            TextureRegion standByBackwardFrame = new TextureRegion(backward1, 0, 0, 13, 13);
            movingBackwardSheetFrames[0] = new TextureRegion(backward1, 0, 0, 13, 13);
            movingBackwardSheetFrames[1] = new TextureRegion(backward2, 0, 0, 13, 13);
            movingBackwardAnimation = new Animation<>(0.1F, movingBackwardSheetFrames);
            standByBackwardAnimation = new Animation<>(0.1F, standByBackwardFrame);

            TextureRegion[] movingLeftSheetFrames = new TextureRegion[2];
            TextureRegion standByLeftFrame = new TextureRegion(left1, 0, 0, 13, 13);
            movingLeftSheetFrames[0] = new TextureRegion(left1, 0, 0, 13, 13);
            movingLeftSheetFrames[1] = new TextureRegion(left2, 0, 0, 13, 13);
            movingLeftAnimation = new Animation<>(0.1F, movingLeftSheetFrames);
            standByLeftAnimation = new Animation<>(0.1F, standByLeftFrame);

            TextureRegion[] movingRightSheetFrames = new TextureRegion[2];
            TextureRegion standByRightFrame = new TextureRegion(right1, 0, 0, 13, 13);
            movingRightSheetFrames[0] = new TextureRegion(right1, 0, 0, 13, 13);
            movingRightSheetFrames[1] = new TextureRegion(right2, 0, 0, 13, 13);
            movingRightAnimation = new Animation<>(0.1F, movingRightSheetFrames);
            standByRightAnimation = new Animation<>(0.1F, standByRightFrame);
        } catch (Exception e) {
            Gdx.app.error("Tank", "Error loading animations for " + colour + ": " + e.getMessage());
        }
    }

    public void handleInput(int keycode, float stateTime) {
        this.stateTime = stateTime;
        currentState.handleInput(keycode, stateTime);
    }

    public Bullet updateEnemy(float delta, Tank player1, Tank player2) {
        if (!isEnemy) return null;

        timeSinceLastMove += delta;
        timeSinceLastShot += delta;

        // Смена направления каждые MOVE_INTERVAL секунд
        if (timeSinceLastMove >= MOVE_INTERVAL) {
            chooseRandomDirection();
            timeSinceLastMove = 0;
        }

        // Стрельба в сторону ближайшего игрока
        if (timeSinceLastShot >= SHOOT_INTERVAL) {
            Tank target = findNearestPlayer(player1, player2);
            if (target != null) {
                // Определяем направление к игроку
                direction = getDirectionToTarget(target);
                handleInput(com.badlogic.gdx.Input.Keys.SPACE, stateTime);
                timeSinceLastShot = 0;
                return shoot();
            }
        }
        return null;
    }

    public void chooseRandomDirection() { // Изменяем с private на public
        int random = (int) (Math.random() * 4);
        switch (random) {
            case 0:
                direction = Direction.FORWARD;
                break;
            case 1:
                direction = Direction.BACKWARD;
                break;
            case 2:
                direction = Direction.LEFT;
                break;
            case 3:
                direction = Direction.RIGHT;
                break;
        }
    }

    private Tank findNearestPlayer(Tank player1, Tank player2) {
        Vector2 myPos = new Vector2(positionX, positionY);
        Vector2 player1Pos = new Vector2(player1.positionX, player1.positionY);
        Vector2 player2Pos = player2 != null ? new Vector2(player2.positionX, player2.positionY) : null;

        float distToPlayer1 = myPos.dst(player1Pos);
        float distToPlayer2 = player2Pos != null ? myPos.dst(player2Pos) : Float.MAX_VALUE;

        return distToPlayer1 <= distToPlayer2 ? player1 : player2;
    }

    private Direction getDirectionToTarget(Tank target) {
        float dx = target.positionX - positionX;
        float dy = target.positionY - positionY;

        if (Math.abs(dx) > Math.abs(dy)) {
            return dx > 0 ? Direction.RIGHT : Direction.LEFT;
        } else {
            return dy > 0 ? Direction.FORWARD : Direction.BACKWARD;
        }
    }

    public void setState(TankState state) {
        this.currentState = state;
    }

    public TextureRegion getCurrentFrame() {
        return currentState.getCurrentFrame(this.stateTime);
    }

    public void moveUp() {
        if (!(positionY - 1 < 0)) {
            positionY -= 1;
        }
    }

    public void moveDown() {
        if (!(positionY + 1 > 454)) {
            positionY += 1;
        }
    }

    public void moveLeft() {
        if (!(positionX - 1 < 0)) {
            positionX -= 1;
        }
    }

    public void moveRight() {
        if (!(positionX + 1 > 454)) {
            positionX += 1;
        }
    }

    public Bullet shoot() {
        // Снаряд появляется в центре танка
        float bulletX = positionX + 13; // 26/2
        float bulletY = positionY + 13; // 26/2
        return new Bullet(bulletX, bulletY, direction, colour);
    }

    public String getColour() {
        return colour;
    }

    public void setColour(String colour) {
        this.colour = colour;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public boolean isEnemy() {
        return isEnemy;
    }

    // Геттеры для анимаций
    public Animation<TextureRegion> getMovingForwardAnimation() {
        return movingForwardAnimation;
    }

    public Animation<TextureRegion> getStandByForwardAnimation() {
        return standByForwardAnimation;
    }

    public Animation<TextureRegion> getMovingBackwardAnimation() {
        return movingBackwardAnimation;
    }

    public Animation<TextureRegion> getStandByBackwardAnimation() {
        return standByBackwardAnimation;
    }

    public Animation<TextureRegion> getMovingLeftAnimation() {
        return movingLeftAnimation;
    }

    public Animation<TextureRegion> getStandByLeftAnimation() {
        return standByLeftAnimation;
    }

    public Animation<TextureRegion> getMovingRightAnimation() {
        return movingRightAnimation;
    }

    public Animation<TextureRegion> getStandByRightAnimation() {
        return standByRightAnimation;
    }

    // Метод для получения границ танка
    public Rectangle getBounds() {
        return new Rectangle(positionX, positionY, 26, 26);
    }

    // Проверка столкновения с другим танком
    public boolean collidesWith(Tank other) {
        if (other == null) return false;
        return getBounds().overlaps(other.getBounds());
    }
}