package com.mg.game;

import com.badlogic.gdx.Game;
import com.mg.game.menu.MenuScreen;
import com.mg.game.observer.GameContext;
import com.mg.game.observer.GameEventDispatcher;
import com.mg.game.observer.GameEventPublisher;
import com.mg.game.observer.GameObserver;

public class gdxGame extends Game {

	public MenuScreen menuScreen;

	private static boolean gameOverFlag = false;

	private final GameEventDispatcher eventDispatcher = new GameEventDispatcher();

	public GameEventPublisher getEventPublisher() {
		return eventDispatcher;
	}

	public static boolean isGameOver() {
		return gameOverFlag;
	}

	public static void resetGameOverFlag() {
		gameOverFlag = false;
	}

	@Override
	public void create() {
		menuScreen = new MenuScreen(this);
		setScreen(menuScreen);
	}

	public void setGameOver(GameContext context) {
		gameOverFlag = true;
		eventDispatcher.notifyBaseDestroyed(context);
	}
}
