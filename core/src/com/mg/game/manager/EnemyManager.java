package com.mg.game.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.mg.game.GameScreen;
import com.mg.game.bullet.Bullet;
import com.mg.game.strategy.AggressiveChaseStrategy;
import com.mg.game.strategy.BaseAttackStrategy;
import com.mg.game.strategy.EnemyStrategy;
import com.mg.game.strategy.WanderStrategy;
import com.mg.game.tank.Tank;
import com.mg.game.tank.factory.EnemyTankFactory;
import com.mg.game.tank.factory.PlayerTankFactory;
import com.mg.game.tank.factory.TankParams;

import java.util.ArrayList;
import java.util.Random;

public class EnemyManager {
    private final GameScreen gameScreen;
    private final ArrayList<Tank> enemies;
    private final ArrayList<EnemyMovementInfo> enemyMovementInfos;
    private final int[][] spawnPoints;
    private int remainingEnemies;
    private float enemyRespawnTimer;
    private float enemyMoveTimer;
    private final Random random;

    // Constants
    private static final int MAX_ENEMIES_ON_MAP = 3;
    private static final int TOTAL_ENEMIES_PER_LEVEL = 10;
    private static final float ENEMY_RESPAWN_DELAY = 3.0f;
    private static final float ENEMY_MOVE_DELAY = 0.04f;
    private static final float MIN_DIRECTION_CHANGE_TIME = 1.0f;
    private static final float MAX_DIRECTION_CHANGE_TIME = 3.0f;
    private static final int STUCK_THRESHOLD = 10;
    private static final int MIN_MOVEMENT_BEFORE_CHANGE = 20;
    private CollisionManager collisionManager;

    // Structure for enemy movement information
    public static class EnemyMovementInfo {
        public Tank.Direction direction;
        public float directionChangeTimer;
        public int movementDistance;
        public boolean isStuck;
        public int stuckCounter;
        private CollisionManager collisionManager;
        public EnemyMovementInfo() {
            direction = Tank.Direction.BACKWARD;
            directionChangeTimer = (float) (Math.random() * 2.0f + 1.0f);
            movementDistance = 0;
            isStuck = false;
            stuckCounter = 0;
        }
    }

    public EnemyManager(GameScreen gameScreen, ArrayList<Tank> enemies) {
        this.gameScreen = gameScreen;
        this.enemies = enemies;
        this.enemyMovementInfos = new ArrayList<>();
        this.spawnPoints = new int[][]{{80, 40}, {240, 40}, {400, 40}};
        this.remainingEnemies = TOTAL_ENEMIES_PER_LEVEL - MAX_ENEMIES_ON_MAP;
        this.enemyRespawnTimer = 0f;
        this.enemyMoveTimer = 0f;
        this.random = new Random();
        this.collisionManager = gameScreen.getCollisionManager();

        initializeEnemies();
    }

    private void initializeEnemies() {
        EnemyTankFactory enemyFactory = new EnemyTankFactory();
        TankParams enemyParams = new TankParams("red", 1, true, gameScreen, collisionManager );
        for (int i = 0; i < MAX_ENEMIES_ON_MAP; i++) {
            Tank enemy = enemyFactory.create(enemyParams);
            enemy.setStrategy(getRandomStrategy());

            int spawnPointIndex = i % spawnPoints.length;
            int spawnX = spawnPoints[spawnPointIndex][0];
            int spawnY = spawnPoints[spawnPointIndex][1];

            if (collisionManager.isSpawnPointClear(spawnX, spawnY)) {
                enemy.positionX = spawnX;
                enemy.positionY = spawnY;
            } else {
                int[] freeSpawn = collisionManager.findNearestFreeSpot(spawnX, spawnY);
                enemy.positionX = freeSpawn[0];
                enemy.positionY = freeSpawn[1];
            }

            enemy.setDirection(Tank.Direction.BACKWARD);
            enemies.add(enemy);

            EnemyMovementInfo movementInfo = new EnemyMovementInfo();
            Tank.Direction[] directions = Tank.Direction.values();
            movementInfo.direction = directions[random.nextInt(directions.length)];
            enemy.setDirection(movementInfo.direction);
            enemyMovementInfos.add(movementInfo);

            Gdx.app.log("EnemyManager", "Spawned initial enemy at " + enemy.positionX + ", " + enemy.positionY);
        }
    }

    public void update(float delta) {
        enemyMoveTimer += delta;

        if (enemyMoveTimer >= ENEMY_MOVE_DELAY) {
            updateEnemies(delta);
            enemyMoveTimer = 0;
        }

        checkEnemyRespawn(delta);
    }

    private void updateEnemies(float delta){
        for (int i = 0; i < enemies.size(); i++) {
        Tank enemy = enemies.get(i);
        if (enemy == null || !enemy.isAlive()) continue;

        if (enemy.getStrategy() != null) {
            enemy.getStrategy().update(enemy, delta, gameScreen);
        }

        if (i >= enemyMovementInfos.size()) {
            enemyMovementInfos.add(new EnemyMovementInfo());
        }
        EnemyMovementInfo info = enemyMovementInfos.get(i);

        info.directionChangeTimer -= delta;

        boolean shouldChangeDirection = info.directionChangeTimer <= 0 ||
                (info.isStuck && info.stuckCounter >= STUCK_THRESHOLD) ||
                (info.movementDistance >= MIN_MOVEMENT_BEFORE_CHANGE && random.nextFloat() < 0.05);

        if (shouldChangeDirection) {
            chooseNewDirectionForEnemy(enemy, info, i);
            info.directionChangeTimer = MIN_DIRECTION_CHANGE_TIME + random.nextFloat() * (MAX_DIRECTION_CHANGE_TIME - MIN_DIRECTION_CHANGE_TIME);
            info.movementDistance = 0;
            info.isStuck = false;
            info.stuckCounter = 0;
        }

        boolean moved = moveEnemyInDirection(enemy, info);

        if (!moved) {
            info.isStuck = true;
            info.stuckCounter++;
            if (info.stuckCounter >= STUCK_THRESHOLD) {
                chooseNewDirectionForEnemy(enemy, info, i);
                info.directionChangeTimer = MIN_DIRECTION_CHANGE_TIME + random.nextFloat() * (MAX_DIRECTION_CHANGE_TIME - MIN_DIRECTION_CHANGE_TIME);
                info.movementDistance = 0;
                info.isStuck = false;
                info.stuckCounter = 0;
            }
        } else {
            info.movementDistance++;
            info.isStuck = false;
        }

        if (random.nextFloat() < 0.005) {
            Bullet bullet = enemy.shoot();
            if (bullet != null) {
                gameScreen.getBullets().add(bullet);
            }
        }
    }

    cleanupDeadEnemies();
}

private void chooseNewDirectionForEnemy(Tank enemy, EnemyMovementInfo info, int enemyIndex) {
    boolean targetBase = random.nextFloat() < 0.3;
    Tank targetPlayer = gameScreen.getNearestAlivePlayer(enemy);

    if (targetPlayer == null) {
        targetBase = true;
    }

    int targetX = targetBase ? gameScreen.getBaseX() : targetPlayer.positionX;
    int targetY = targetBase ? gameScreen.getBaseY() : targetPlayer.positionY;

    int dx = targetX - enemy.positionX;
    int dy = targetY - enemy.positionY;

    boolean moveHorizontally = Math.abs(dx) > Math.abs(dy) || random.nextFloat() < 0.3;

    if (moveHorizontally) {
        info.direction = dx > 0 ? (random.nextFloat() < 0.8 ? Tank.Direction.RIGHT : randomOtherDirection(Tank.Direction.RIGHT))
                : (random.nextFloat() < 0.8 ? Tank.Direction.LEFT : randomOtherDirection(Tank.Direction.LEFT));
    } else {
        info.direction = dy > 0 ? (random.nextFloat() < 0.8 ? Tank.Direction.BACKWARD : randomOtherDirection(Tank.Direction.BACKWARD))
                : (random.nextFloat() < 0.8 ? Tank.Direction.FORWARD : randomOtherDirection(Tank.Direction.FORWARD));
    }

    enemy.setDirection(info.direction);
}

private Tank.Direction randomOtherDirection(Tank.Direction exclude) {
    Tank.Direction[] directions = {Tank.Direction.FORWARD, Tank.Direction.BACKWARD, Tank.Direction.LEFT, Tank.Direction.RIGHT};
    Tank.Direction chosen;
    do {
        chosen = directions[random.nextInt(directions.length)];
    } while (chosen == exclude);
    return chosen;
}

private boolean moveEnemyInDirection(Tank enemy, EnemyMovementInfo info) {
    int newX = enemy.positionX;
    int newY = enemy.positionY;
    int keycode = -1;
    int moveSpeed = 3;

    switch (info.direction) {
        case FORWARD:
            newY -= moveSpeed;
            keycode = Input.Keys.UP;
            break;
        case BACKWARD:
            newY += moveSpeed;
            keycode = Input.Keys.DOWN;
            break;
        case LEFT:
            newX -= moveSpeed;
            keycode = Input.Keys.LEFT;
            break;
        case RIGHT:
            newX += moveSpeed;
            keycode = Input.Keys.RIGHT;
            break;
    }

    boolean canMove = newX >= 0 && newX <= 454 - 9 && newY >= 0 && newY <= 454 &&
            !collisionManager.checkCollisionWithPlayer(enemy, newX, newY) &&
            !collisionManager.checkCollisionWithEnemy(enemy, newX, newY) &&
            !collisionManager.checkCollisionWithMap(newX, newY, enemy);

    if (canMove) {
        enemy.positionX = newX;
        enemy.positionY = newY;
        try {
            enemy.handleInput(keycode, gameScreen.getStateTime());
        } catch (Exception e) {
            Gdx.app.error("EnemyManager", "Error handling input for enemy: " + e.getMessage());
        }
        return true;
    } else {
        try {
            enemy.handleInput(-1, gameScreen.getStateTime());
        } catch (Exception e) {
            Gdx.app.error("EnemyManager", "Error handling input for enemy: " + e.getMessage());
        }
        return false;
    }
}

    private void checkEnemyRespawn(float delta) {
        int aliveEnemies = countAliveEnemies();
        Gdx.app.log("EnemyManager", "Alive enemies: " + aliveEnemies + ", Remaining: " + remainingEnemies);
        if (aliveEnemies < MAX_ENEMIES_ON_MAP && remainingEnemies > 0) {
            enemyRespawnTimer += delta;
            if (enemyRespawnTimer >= ENEMY_RESPAWN_DELAY) {
                spawnNewEnemy();
                enemyRespawnTimer = 0f;
                remainingEnemies--;
                Gdx.app.log("EnemyManager", "Spawned new enemy. Remaining: " + remainingEnemies);
            }
        }
    }

private void spawnNewEnemy() {
    int spawnPointIndex = random.nextInt(spawnPoints.length);
    int spawnX = spawnPoints[spawnPointIndex][0];
    int spawnY = spawnPoints[spawnPointIndex][1];

    TankParams enemyParams = new TankParams("red", 1, true, gameScreen, collisionManager );
    EnemyTankFactory enemyFactory = new EnemyTankFactory();
    Tank enemy = enemyFactory.create(enemyParams);
    enemy.setStrategy(getRandomStrategy());

    if (collisionManager.isSpawnPointClear(spawnX, spawnY)) {
        enemy.positionX = spawnX;
        enemy.positionY = spawnY;
    } else {
        int[] freeSpawn = collisionManager.findNearestFreeSpot(spawnX, spawnY);
        enemy.positionX = freeSpawn[0];
        enemy.positionY = freeSpawn[1];
    }

    enemy.setDirection(Tank.Direction.BACKWARD);
    enemies.add(enemy);

    EnemyMovementInfo movementInfo = new EnemyMovementInfo();
    Tank.Direction[] directions = Tank.Direction.values();
    movementInfo.direction = directions[random.nextInt(directions.length)];
    enemy.setDirection(movementInfo.direction);
    enemyMovementInfos.add(movementInfo);
}

private int countAliveEnemies() {
    int count = 0;
    for (Tank enemy : enemies) {
        if (enemy != null && enemy.isAlive()) {
            count++;
        }
    }
    return count;
}

private void cleanupDeadEnemies() {
    for (int i = enemies.size() - 1; i >= 0; i--) {
        Tank enemy = enemies.get(i);
        if (enemy == null || !enemy.isAlive()) {
            enemies.remove(i);
            if (i < enemyMovementInfos.size()) {
                enemyMovementInfos.remove(i);
            }
        }
    }
}

private EnemyStrategy getRandomStrategy() {
    int r = random.nextInt(3);
    switch (r) {
        case 0: return new BaseAttackStrategy();
        case 1: return new AggressiveChaseStrategy();
        case 2: return new WanderStrategy();
        default: return new BaseAttackStrategy();
    }
}

public ArrayList<EnemyMovementInfo> getEnemyMovementInfos() {
    return enemyMovementInfos;
}
}