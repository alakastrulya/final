package com.mg.game.command;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.mg.game.GameScreen;
import com.mg.game.tank.Tank;

public class MoveUpCommand implements Command {
    private final Tank tank;
    private final GameScreen screen;
    private final boolean isPlayer1;

    public MoveUpCommand(Tank tank, GameScreen screen, boolean isPlayer1) {
        this.tank = tank;
        this.screen = screen;
        this.isPlayer1 = isPlayer1;
    }

    @Override
    public boolean canExecute() {
        boolean canExecute = tank.isAlive() && (isPlayer1 ? Gdx.input.isKeyPressed(Input.Keys.UP) : Gdx.input.isKeyPressed(Input.Keys.W));
        Gdx.app.log("MoveUpCommand", (isPlayer1 ? "Player1" : "Player2") + " canExecute=" + canExecute);
        return canExecute;
    }

    @Override
    public void execute() {
        tank.handleInput(isPlayer1 ? Input.Keys.UP : Input.Keys.W, screen.getStateTime());
        Gdx.app.log("MoveUpCommand", "Executing for " + (isPlayer1 ? "Player1" : "Player2"));
    }
}