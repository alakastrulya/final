package com.mg.game.tank;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.mg.game.GameScreen;
import com.mg.game.bullet.Bullet;
import com.mg.game.bullet.BulletFactory;
import com.mg.game.assets.Assets;
import com.mg.game.strategy.EnemyStrategy;
import com.mg.game.tank.state.StandingByState;

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

    // Для определения стратегии танка
    private EnemyStrategy strategy;

    public Tank() {}

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

        // Используем фабрику для создания пули
        BulletFactory bulletFactory = new BulletFactory(bulletX, bulletY, direction, colour, isEnemy);
        return bulletFactory.create();
    }

//    // Добавьте эти методы в класс Tank для улучшения AI врагов
//    public void improveEnemyAI(float delta, Tank player1, Tank player2) {
//        // Обновляем таймер неуязвимости
//        if (isInvulnerable()) {
//            invulnerabilityTimer -= delta;
//        }
//
//        // Продолжаем только если это вражеский танк
//        if (!isEnemy) {
//            return;
//        }
//
//        // Проверяем, есть ли игрок на линии огня
//        Tank targetPlayer = null;
//
//        if (player1 != null && player1.isAlive()) {
//            if (isInLineOfSight(player1)) {
//                targetPlayer = player1;
//            }
//        }
//
//        if (targetPlayer == null && player2 != null && player2.isAlive()) {
//            if (isInLineOfSight(player2)) {
//                targetPlayer = player2;
//            }
//        }
//
//        // Если игрок на линии огня, увеличиваем шанс выстрела
//        if (targetPlayer != null && Math.random() < 0.1) { // 10% шанс выстрелить, когда игрок на линии огня
//            shoot();
//        }
//    }

    // Добавьте этот метод для проверки, находится ли игрок на линии огня
    public boolean isInLineOfSight(Tank player) {
        // Проверяем, находится ли игрок в том же ряду или колонке
        boolean sameRow = Math.abs(this.positionY - player.positionY) < 10;
        boolean sameColumn = Math.abs(this.positionX - player.positionX) < 10;

        if (!sameRow && !sameColumn) {
            return false;
        }

        // Определяем направление для проверки
        int dx = 0;
        int dy = 0;

        if (sameRow) {
            dx = (player.positionX > this.positionX) ? 1 : -1;
        } else {
            dy = (player.positionY > this.positionY) ? 1 : -1;
        }

        // Проверяем, соответствует ли текущее направление линии огня
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
