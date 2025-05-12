package com.mg.game.command;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.mg.game.GameScreen;
import com.mg.game.tank.Tank;

public class MoveLeftCommand implements Command {
    private final Tank tank;
    private final GameScreen screen;
    private final boolean isPlayer1;

    public MoveLeftCommand(Tank tank, GameScreen screen, boolean isPlayer1) {
        this.tank = tank;
        this.screen = screen;
        this.isPlayer1 = isPlayer1;
    }

    @Override
    public boolean canExecute() {
        return tank.isAlive() && (isPlayer1 ? Gdx.input.isKeyPressed(Input.Keys.LEFT) : Gdx.input.isKeyPressed(Input.Keys.A));
    }

    @Override
    public void execute() {
        tank.handleInput(isPlayer1 ? Input.Keys.LEFT : Input.Keys.A, screen.getStateTime());
        Gdx.app.log("MoveLeftCommand", "Executing for " + (isPlayer1 ? "Player1" : "Player2"));
    }
}