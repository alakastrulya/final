package com.mg.game.tank;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.mg.game.CollisionManager;
import com.mg.game.GameScreen;
import com.mg.game.bullet.Bullet;
import com.mg.game.bullet.BulletFactory;
import com.mg.game.assets.Assets;
import com.mg.game.strategy.EnemyStrategy;
import com.mg.game.tank.factory.Factory;
import com.mg.game.tank.state.StandingByState;

import java.util.Random;

public class Tank {
    public enum Direction {
        FORWARD, BACKWARD, LEFT, RIGHT
    }

    private String colour;
    private int level;
    private boolean isEnemy;
    private Direction direction;
    private boolean isMoving;
    private TextureRegion currentFrame;
    private float stateTime;
    private Random random;
    private float shootTimer;
    private static final float SHOOT_DELAY = 2.0f;

    private int health;
    private float invulnerabilityTimer;
    private static final float INVULNERABILITY_TIME = 2.0f;

    public int positionX;
    public int positionY;

    private TankState currentState;
    private EnemyStrategy strategy;
    private CollisionManager collisionManager;

    private GameScreen screen; // New field

    public Tank() {}

    public Tank(String colour, int level, boolean isEnemy) {
        this(colour, level, isEnemy, null);
    }

    public Tank(String colour, int level, boolean isEnemy, GameScreen screen) {
        this.colour = colour;
        this.level = level;
        this.isEnemy = isEnemy;
        this.screen = screen;
        this.direction = Direction.FORWARD;
        this.isMoving = false;
        this.random = new Random();
        this.shootTimer = 0;

        this.health = isEnemy ? 1 : 3;
        this.invulnerabilityTimer = 0;

        if (isEnemy) {
            chooseRandomDirection();
        }

        try {
            this.currentState = new StandingByState(this);
        } catch (Exception e) {
            Gdx.app.error("Tank", "Failed to initialize StandingByState: " + e.getMessage());
            this.currentState = null;
        }
    }
    public Tank(String colour, int level, boolean isEnemy, GameScreen screen, CollisionManager collisionManager) {
        this.colour = colour;
        this.level = level;
        this.isEnemy = isEnemy;
        this.screen = screen;
        this.collisionManager = collisionManager;
        this.direction = Direction.FORWARD;
        this.isMoving = false;
        this.random = new Random();
        this.shootTimer = 0;

        this.health = isEnemy ? 1 : 3;
        this.invulnerabilityTimer = 0;

        if (isEnemy) {
            chooseRandomDirection();
        }

        try {
            this.currentState = new StandingByState(this);
        } catch (Exception e) {
            Gdx.app.error("Tank", "Failed to initialize StandingByState: " + e.getMessage());
            this.currentState = null;
        }
    }

    public void handleInput(int keycode, float stateTime) {
        this.stateTime = stateTime;
        if (currentState == null) {
            Gdx.app.error("Tank", "currentState is null for tank: " + colour);
            return;
        }
        try {
            currentState.handleInput(keycode, stateTime);
        } catch (Exception e) {
            Gdx.app.error("Tank", "Error handling input: " + e.getMessage());
        }
        currentFrame = getCurrentFrame();
    }

    public void moveUp() {
        int newY = positionY - 1;
        if (screen != null && collisionManager.canMoveTo(this, positionX, newY)) {
            positionY = newY;
            Gdx.app.log("Tank", colour + " moved up to y=" + positionY);
        } else {
            Gdx.app.log("Tank", colour + " cannot move up to y=" + newY);
        }
    }

    public void moveDown() {
        int newY = positionY + 1;
        if (screen != null && collisionManager.canMoveTo(this, positionX, newY)) {
            positionY = newY;
            Gdx.app.log("Tank", colour + " moved down to y=" + positionY);
        } else {
            Gdx.app.log("Tank", colour + " cannot move down to y=" + newY);
        }
    }

    public void moveLeft() {
        int newX = positionX - 1;
        if (screen != null && collisionManager.canMoveTo(this, newX, positionY)) {
            positionX = newX;
            Gdx.app.log("Tank", colour + " moved left to x=" + positionX);
        } else {
            Gdx.app.log("Tank", colour + " cannot move left to x=" + newX);
        }
    }

    public void moveRight() {
        int newX = positionX + 1;
        if (screen != null && collisionManager.canMoveTo(this, newX, positionY)) {
            positionX = newX;
            Gdx.app.log("Tank", colour + " moved right to x=" + positionX);
        } else {
            Gdx.app.log("Tank", colour + " cannot move right to x=" + newX);
        }
    }

    public Bullet shoot() {
        float bulletX = positionX;
        float bulletY = positionY;

        switch (direction) {
            case FORWARD:
                bulletX += 13;
                bulletY += 26;
                break;
            case BACKWARD:
                bulletX += 13;
                bulletY -= 4;
                break;
            case LEFT:
                bulletX -= 4;
                bulletY += 13;
                break;
            case RIGHT:
                bulletX += 26;
                bulletY += 13;
                break;
        }
        Gdx.app.log("ShootDebug", "Shooting bullet: color=" + colour + ", isEnemy=" + isEnemy);
        return new Bullet(bulletX, bulletY, direction, colour, isEnemy);
    }
    public boolean isInLineOfSight(Tank player) {
        boolean sameRow = Math.abs(this.positionY - player.positionY) < 10;
        boolean sameColumn = Math.abs(this.positionX - player.positionX) < 10;

        if (!sameRow && !sameColumn) {
            return false;
        }

        int dx = 0;
        int dy = 0;

        if (sameRow) {
            dx = (player.positionX > this.positionX) ? 1 : -1;
        } else {
            dy = (player.positionY > this.positionY) ? 1 : -1;
        }

        boolean correctDirection = false;

        switch (direction) {
            case RIGHT:
                correctDirection = (dx > 0);
                break;
            case LEFT:
                correctDirection = (dx < 0);
                break;
            case BACKWARD:
                correctDirection = (dy > 0);
                break;
            case FORWARD:
                correctDirection = (dy < 0);
                break;
        }

        return correctDirection;
    }

    public void chooseRandomDirection() {
        int dir = random.nextInt(4);
        switch (dir) {
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

    public TextureRegion getCurrentFrame() {
        if (currentState != null) {
            try {
                return currentState.getCurrentFrame(stateTime);
            } catch (Exception e) {
                Gdx.app.error("Tank", "Error getting current frame from state: " + e.getMessage());
            }
        }

        try {
            switch (direction) {
                case FORWARD:
                    return Assets.getStandByForwardAnimation(colour).getKeyFrame(stateTime, true);
                case BACKWARD:
                    return Assets.getStandByBackwardAnimation(colour).getKeyFrame(stateTime, true);
                case LEFT:
                    return Assets.getStandByLeftAnimation(colour).getKeyFrame(stateTime, true);
                case RIGHT:
                    return Assets.getStandByRightAnimation(colour).getKeyFrame(stateTime, true);
                default:
                    return Assets.getStandByForwardAnimation(colour).getKeyFrame(stateTime, true);
            }
        } catch (Exception e) {
            Gdx.app.error("Tank", "Error getting default frame: " + e.getMessage());
            return null;
        }
    }

    public boolean collidesWith(Tank other) {
        Rectangle thisBounds = getBounds();
        Rectangle otherBounds = other.getBounds();
        return thisBounds.overlaps(otherBounds);
    }

    public Rectangle getBounds() {
        return new Rectangle(positionX, positionY, 26, 26);
    }

    public boolean takeDamage() {
        if (isInvulnerable()) {
            return false;
        }

        health--;
        invulnerabilityTimer = INVULNERABILITY_TIME;
        return health <= 0;
    }

    public boolean isAlive() {
        return health > 0;
    }

    public boolean isInvulnerable() {
        return invulnerabilityTimer > 0;
    }

    public void update(float delta) {
        if (invulnerabilityTimer > 0) {
            invulnerabilityTimer -= delta;
        }
    }

    public void setStrategy(EnemyStrategy strategy) {
        this.strategy = strategy;
    }

    public void performStrategy(float delta, GameScreen context) {
        if (strategy != null) strategy.update(this, delta, context);
    }

    public EnemyStrategy getStrategy() {
        return strategy;
    }

    public void reduceInvulnerability(float delta) {
        if (invulnerabilityTimer > 0) invulnerabilityTimer -= delta;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public void setState(TankState state) {
        this.currentState = state;
    }

    public String getColour() {
        return colour;
    }

    public int getLevel() {
        return level;
    }

    public Direction getDirection() {
        return direction;
    }

    public boolean isEnemy() {
        return isEnemy;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }
}