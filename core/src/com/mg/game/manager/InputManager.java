package com.mg.game.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.mg.game.GameScreen;
import com.mg.game.bullet.Bullet;
import com.mg.game.command.InputHandler;
import com.mg.game.tank.Tank;

import java.util.ArrayList;

public class InputManager {
    private final GameScreen gameScreen;
    private final Tank player1;
    private final Tank player2;
    private final ArrayList<Bullet> bullets;
    private final int playerCount;
    private final InputHandler inputHandler;
    private float moveTimer = 0f;
    private static final float MOVE_DELAY = 0.003f;

    public InputManager(GameScreen gameScreen, Tank player1, Tank player2, ArrayList<Bullet> bullets, int playerCount) {
        this.gameScreen = gameScreen;
        this.player1 = player1;
        this.player2 = player2;
        this.bullets = bullets;
        this.playerCount = playerCount;
        this.inputHandler = new InputHandler(player1, player2, bullets, playerCount, gameScreen);
    }

    public void handleInput(float delta) {
        // Игнорируем ввод во время интро
        if (gameScreen.isLevelIntroPlaying()) {
            return;
        }

        // Пауза
        if (Gdx.input.isKeyJustPressed(Input.Keys.P) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            gameScreen.togglePause();
        }

        // Дебаг-режим
        if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
            gameScreen.toggleDebugMode();
        }

        // Рестарт при геймовере
        if (gameScreen.isGameOver() && Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            gameScreen.restartGame();
            return;
        }

        // Обработка ввода игрока
        if (!gameScreen.isGameOver() && !gameScreen.isPaused() && !gameScreen.isLevelComplete()) {
            moveTimer += delta;
            if (moveTimer >= MOVE_DELAY) {
                inputHandler.handleInput(delta);
                moveTimer = 0;
            }
        }
    }

    public void dispose() {
        // Пустой метод, так как InputHandler не требует освобождения ресурсов
    }
}