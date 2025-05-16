package com.mg.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.mg.game.map.MapLoader;
import com.mg.game.map.MapTile;
import com.mg.game.tank.Tank;

import java.util.ArrayList;

        public class CollisionManager {
            private final MapLoader mapLoader;
            private final Tank player1;
            private final Tank player2;
            private final ArrayList<Tank> enemies;
            private static final float TILE_SCALE = 0.87f;

            public CollisionManager(MapLoader mapLoader, Tank player1, Tank player2, ArrayList<Tank> enemies) {
                this.mapLoader = mapLoader;
                this.player1 = player1;
                this.player2 = player2;
                this.enemies = enemies;
            }

            public boolean canMoveTo(Tank tank, int newX, int newY) {
                if (tank == null || !tank.isAlive()) {
                    Gdx.app.log("CollisionManager", "Cannot move: tank is null or dead");
                    return false;
                }
                if (newX < 0 || newX > 454 - 9 || newY < 0 || newY > 454) {
                    Gdx.app.log("CollisionManager", "Cannot move: out of bounds at x=" + newX + ", y=" + newY);
                    return false;
                }
                if (checkCollisionWithTank(tank, newX, newY)) {
                    Gdx.app.log("CollisionManager", "Cannot move: collision with tank at x=" + newX + ", y=" + newY);
                    return false;
                }
                if (checkCollisionWithEnemy(tank, newX, newY)) {
                    Gdx.app.log("CollisionManager", "Cannot move: collision with enemy at x=" + newX + ", y=" + newY);
                    return false;
                }
                if (checkCollisionWithMap(newX, newY, tank)) {
                    Gdx.app.log("CollisionManager", "Cannot move: collision with map at x=" + newX + ", y=" + newY);
                    return false;
                }
                Gdx.app.log("CollisionManager", "Can move to x=" + newX + ", y=" + newY);
                return true;
            }

            public boolean checkCollisionWithTank(Tank tank, int newX, int newY) {
                if (tank == null) return false;

                int oldX = tank.positionX;
                int oldY = tank.positionY;

                tank.positionX = newX;
                tank.positionY = newY;

                boolean collides = false;

                // Check collision with the first player, only if alive
                if (tank == player2 && player1 != null && player1.isAlive() && tank.collidesWith(player1)) {
                    collides = true;
                }
                // Check collision with the second player, only if alive
                else if (tank == player1 && player2 != null && player2.isAlive() && tank.collidesWith(player2)) {
                    collides = true;
                }

                tank.positionX = oldX;
                tank.positionY = oldY;

                return collides;
            }

            public boolean checkCollisionWithEnemy(Tank tank, int newX, int newY) {
                if (tank == null) return false;

                int oldX = tank.positionX;
                int oldY = tank.positionY;

                tank.positionX = newX;
                tank.positionY = newY;

                boolean collides = false;
                for (Tank enemy : enemies) {
                    if (enemy != null && enemy.isAlive() && tank != enemy && tank.collidesWith(enemy)) {
                        collides = true;
                        break;
                    }
                }

                tank.positionX = oldX;
                tank.positionY = oldY;

                return collides;
            }

            public boolean checkCollisionWithMap(int newX, int newY, Tank tank) {
                Rectangle tankRect = new Rectangle(newX, newY, 26 / TILE_SCALE, 26 / TILE_SCALE);
                for (MapTile tile : mapLoader.tiles) {
                    if (tile.isSolid) {
                        Rectangle tileRect = tile.getBounds(MapLoader.TILE_SIZE, TILE_SCALE, -17, -17);
                        if (tankRect.overlaps(tileRect)) {
                            return true;
                        }
                    }
                }
                return false;
            }

            public boolean checkCollisionWithPlayer(Tank enemy, int newX, int newY) {
                if (enemy == null) return false;

                int oldX = enemy.positionX;
                int oldY = enemy.positionY;

                enemy.positionX = newX;
                enemy.positionY = newY;

                boolean collides =
                        (player1 != null && player1.isAlive() && enemy.collidesWith(player1)) ||
                                (player2 != null && player2.isAlive() && enemy.collidesWith(player2));

                enemy.positionX = oldX;
                enemy.positionY = oldY;

                return collides;
            }

            public boolean isSpawnPointClear(int x, int y) {
                if (checkCollisionWithMap(x, y, null)) {
                    return false;
                }

                Rectangle spawnRect = new Rectangle(x, y, 26 / TILE_SCALE, 26 / TILE_SCALE);
                if (player1 != null && player1.isAlive()) {
                    Rectangle playerRect = new Rectangle(player1.positionX, player1.positionY, 26 / TILE_SCALE, 26 / TILE_SCALE);
                    if (spawnRect.overlaps(playerRect)) {
                        return false;
                    }
                }

                if (player2 != null && player2.isAlive()) {
                    Rectangle playerRect = new Rectangle(player2.positionX, player2.positionY, 26 / TILE_SCALE, 26 / TILE_SCALE);
                    if (spawnRect.overlaps(playerRect)) {
                        return false;
                    }
                }

                for (Tank enemy : enemies) {
                    if (enemy != null && enemy.isAlive()) {
                        Rectangle enemyRect = new Rectangle(enemy.positionX, enemy.positionY, 26 / TILE_SCALE, 26 / TILE_SCALE);
                        if (spawnRect.overlaps(enemyRect)) {
                            return false;
                        }
                    }
                }

                return true;
            }

            public int[] findFreeSpawnPoint(int startX, int startY, int step) {
                for (int y = startY; y < 480; y += step) {
                    for (int x = startX; x < 440; x += step) {
                        Rectangle rect = new Rectangle(x, y, 26 / TILE_SCALE, 26 / TILE_SCALE);
                        boolean blocked = false;

                        // Check map
                        for (MapTile tile : mapLoader.tiles) {
                            if (tile.isSolid) {
                                Rectangle tileRect = tile.getBounds(MapLoader.TILE_SIZE, TILE_SCALE, -17, -17);
                                if (rect.overlaps(tileRect)) {
                                    blocked = true;
                                    break;
                                }
                            }
                        }

                        // Check player 1
                        if (!blocked && player1 != null) {
                            Rectangle r1 = new Rectangle(player1.positionX, player1.positionY, 26 / TILE_SCALE, 26 / TILE_SCALE);
                            if (rect.overlaps(r1)) {
                                blocked = true;
                            }
                        }

                        // Check player 2
                        if (!blocked && player2 != null) {
                            Rectangle r2 = new Rectangle(player2.positionX, player2.positionY, 26 / TILE_SCALE, 26 / TILE_SCALE);
                            if (rect.overlaps(r2)) {
                                blocked = true;
                            }
                        }

                        // Check other enemies
                        if (!blocked) {
                            for (Tank enemy : enemies) {
                                if (enemy != null) {
                                    Rectangle r = new Rectangle(enemy.positionX, enemy.positionY, 26 / TILE_SCALE, 26 / TILE_SCALE);
                                    if (rect.overlaps(r)) {
                                        blocked = true;
                                        break;
                                    }
                                }
                            }
                        }

                        if (!blocked) {
                            return new int[]{x, y};
                        }
                    }
                }
                return new int[]{50, 50};
            }

            public int[] findNearestFreeSpot(int startX, int startY) {
                for (int radius = 5; radius <= 50; radius += 5) {
                    for (int offsetX = -radius; offsetX <= radius; offsetX += 5) {
                        for (int offsetY = -radius; offsetY <= radius; offsetY += 5) {
                            int x = startX + offsetX;
                            int y = startY + offsetY;
                            if (x >= 0 && x <= 454 && y >= 0 && y <= 454 && isSpawnPointClear(x, y)) {
                                return new int[]{x, y};
                            }
                        }
                    }
                }
                Gdx.app.error("CollisionManager", "Could not find free spawn point near " + startX + ", " + startY);
                return new int[]{startX, startY};
            }
        }