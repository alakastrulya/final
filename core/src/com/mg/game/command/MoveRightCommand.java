package com.mg.game.command;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.mg.game.GameScreen;
import com.mg.game.tank.Tank;

public class MoveRightCommand implements Command {
    private final Tank tank;
    private final GameScreen screen;
    private final boolean isPlayer1;

    public MoveRightCommand(Tank tank, GameScreen screen, boolean isPlayer1) {
        this.tank = tank;
        this.screen = screen;
        this.isPlayer1 = isPlayer1;
    }

    @Override
    public boolean canExecute() {
        return tank.isAlive() && (isPlayer1 ? Gdx.input.isKeyPressed(Input.Keys.RIGHT) : Gdx.input.isKeyPressed(Input.Keys.D));
    }

    @Override
    public void execute() {
        tank.handleInput(isPlayer1 ? Input.Keys.RIGHT : Input.Keys.D, screen.getStateTime());
        Gdx.app.log("MoveRightCommand", "Executing for " + (isPlayer1 ? "Player1" : "Player2"));
    }
}