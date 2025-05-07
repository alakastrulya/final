package com.mg.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

import java.util.Random;

public class Tank {
    // Перечисление для направлений
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
    private static final float SHOOT_DELAY = 2.0f; // Задержка между выстрелами врага

    // Переменные для здоровья и неуязвимости
    private int health;
    private float invulnerabilityTimer;
    private static final float INVULNERABILITY_TIME = 2.0f; // секунды

    public int positionX;
    public int positionY;

    // Поле для текущего состояния
    private TankState currentState;

    public Tank(String colour, int level, boolean isEnemy) {
        this.colour = colour;
        this.level = level;
        this.isEnemy = isEnemy;
        this.direction = Direction.FORWARD; // По умолчанию смотрит вперед
        this.isMoving = false;
        this.random = new Random();
        this.shootTimer = 0;

        // Инициализация здоровья
        this.health = isEnemy ? 1 : 3; // Враги умирают с одного выстрела, игроки с трех
        this.invulnerabilityTimer = 0;

        // Если это враг, выбираем случайное направление
        if (isEnemy) {
            chooseRandomDirection();
        }

        // Устанавливаем начальное состояние
        try {
            this.currentState = new StandingByState(this);
        } catch (Exception e) {
            Gdx.app.error("Tank", "Failed to initialize StandingByState: " + e.getMessage());
            this.currentState = null; // Устанавливаем null как запасной вариант
        }
    }

    public void handleInput(int keycode, float stateTime) {
        this.stateTime = stateTime;
        if (currentState != null) {
            try {
                currentState.handleInput(keycode, stateTime);
            } catch (Exception e) {
                Gdx.app.error("Tank", "Error handling input: " + e.getMessage());
            }
        }
        // Обновляем текущий кадр через состояние или по умолчанию
        currentFrame = getCurrentFrame();
    }

    public void moveUp() {
        positionY--;
    }

    public void moveDown() {
        positionY++;
    }

    public void moveLeft() {
        positionX--;
    }

    public void moveRight() {
        positionX++;
    }

    public Bullet shoot() {
        // Создаем снаряд в зависимости от направления танка
        float bulletX = positionX;
        float bulletY = positionY;

        // Корректируем начальную позицию снаряда в зависимости от направления
        switch (direction) {
            case FORWARD:
                bulletX += 13; // Центр танка по X
                bulletY += 26; // Перед танка по Y
                break;
            case BACKWARD:
                bulletX += 13; // Центр танка по X
                bulletY -= 4; // За танком по Y
                break;
            case LEFT:
                bulletX -= 4; // Слева от танка по X
                bulletY += 13; // Центр танка по Y
                break;
            case RIGHT:
                bulletX += 26; // Справа от танка по X
                bulletY += 13; // Центр танка по Y
                break;
        }

        return new Bullet(bulletX, bulletY, direction, colour, isEnemy);
    }

    public Bullet updateEnemy(float delta, Tank player1, Tank player2) {
        // Обновляем таймер стрельбы
        shootTimer -= delta;

        // Случайно меняем направление (с небольшой вероятностью)
        if (random.nextFloat() < 0.01) {
            chooseRandomDirection();
        }

        // Стреляем, если прошло достаточно времени
        if (shootTimer <= 0) {
            shootTimer = SHOOT_DELAY;
            return shoot();
        }

        return null;
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
        // Возвращаем текущий кадр через состояние, если оно доступно
        if (currentState != null) {
            try {
                return currentState.getCurrentFrame(stateTime);
            } catch (Exception e) {
                Gdx.app.error("Tank", "Error getting current frame from state: " + e.getMessage());
            }
        }

        // Возвращаем кадр по умолчанию, если состояние не установлено или произошла ошибка
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
            return null; // Возвращаем null, если не удалось получить кадр
        }
    }

    public boolean collidesWith(Tank other) {
        // Проверяем пересечение прямоугольников танков
        Rectangle thisBounds = getBounds();
        Rectangle otherBounds = other.getBounds();
        return thisBounds.overlaps(otherBounds);
    }

    public Rectangle getBounds() {
        // Возвращаем прямоугольник, представляющий границы танка
        return new Rectangle(positionX, positionY, 26, 26);
    }

    public boolean takeDamage() {
        // Если танк неуязвим, урон не наносится
        if (isInvulnerable()) {
            return false;
        }

        // Уменьшаем здоровье
        health--;

        // Делаем танк временно неуязвимым
        invulnerabilityTimer = INVULNERABILITY_TIME;

        // Возвращаем true, если танк уничтожен
        return health <= 0;
    }

    public boolean isAlive() {
        return health > 0;
    }

    public boolean isInvulnerable() {
        return invulnerabilityTimer > 0;
    }

    public void update(float delta) {
        // Уменьшаем таймер неуязвимости
        if (invulnerabilityTimer > 0) {
            invulnerabilityTimer -= delta;
        }
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
