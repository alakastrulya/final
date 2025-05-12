package com.mg.game.command;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.mg.game.GameScreen;
import com.mg.game.bullet.Bullet;
import com.mg.game.tank.Tank;

import java.util.ArrayList;

public class InputHandler {
    private Tank player1;
    private Tank player2;
    private ArrayList<Bullet> bullets;
    private int playerCount;
    private GameScreen gameScreen;

    // Commands for player 1
    private Command player1MoveUp;
    private Command player1MoveDown;
    private Command player1MoveLeft;
    private Command player1MoveRight;
    private ShootCommand player1Shoot; // Specifically ShootCommand

    // Commands for player 2 (if present)
    private Command player2MoveUp;
    private Command player2MoveDown;
    private Command player2MoveLeft;
    private Command player2MoveRight;
    private ShootCommand player2Shoot; // Specifically ShootCommand

    public InputHandler(Tank player1, Tank player2, ArrayList<Bullet> bullets, int playerCount, GameScreen gameScreen) {
        this.player1 = player1;
        this.player2 = player2;
        this.bullets = bullets;
        this.playerCount = playerCount;
        this.gameScreen = gameScreen;

        // Initialize commands for player 1
        player1MoveUp = new MoveUpCommand(player1, gameScreen, true);
        player1MoveDown = new MoveDownCommand(player1, gameScreen, true);
        player1MoveLeft = new MoveLeftCommand(player1, gameScreen, true);
        player1MoveRight = new MoveRightCommand(player1, gameScreen, true);
        player1Shoot = new ShootCommand(player1, playerCount == 2, bullets); // Enter in multiplayer, space in singleplayer

        // Initialize commands for player 2 (if multiplayer mode)
        if (playerCount == 2 && player2 != null) {
            player2MoveUp = new MoveUpCommand(player2, gameScreen, false);
            player2MoveDown = new MoveDownCommand(player2, gameScreen, false);
            player2MoveLeft = new MoveLeftCommand(player2, gameScreen, false);
            player2MoveRight = new MoveRightCommand(player2, gameScreen, false);
            player2Shoot = new ShootCommand(player2, false, bullets); // Always space
        }
    }

    public void handleInput(float delta) {
        // Handle input for player 1
        if (player1 != null && player1.isAlive()) {
            // Movement
            if (player1MoveUp.canExecute()) {
                player1MoveUp.execute();
            }
            if (player1MoveDown.canExecute()) {
                player1MoveDown.execute();
            }
            if (player1MoveLeft.canExecute()) {
                player1MoveLeft.execute();
            }
            if (player1MoveRight.canExecute()) {
                player1MoveRight.execute();
            }
            // Shooting
            player1Shoot.update(delta); // Works now since player1Shoot is ShootCommand
            if (player1Shoot.canExecute()) {
                player1Shoot.execute();
            }
        }

        // Handle input for player 2 (only in multiplayer)
        if (playerCount == 2 && player2 != null && player2.isAlive()) {
            // Movement
            if (player2MoveUp.canExecute()) {
                player2MoveUp.execute();
            }
            if (player2MoveDown.canExecute()) {
                player2MoveDown.execute();
            }
            if (player2MoveLeft.canExecute()) {
                player2MoveLeft.execute();
            }
            if (player2MoveRight.canExecute()) {
                player2MoveRight.execute();
            }
            // Shooting
            if (player2Shoot != null) {
                player2Shoot.update(delta); // Works now since player2Shoot is ShootCommand
                if (player2Shoot.canExecute()) {
                    player2Shoot.execute();
                }
            }
        }
    }
}
