package com.mg.game;

import com.badlogic.gdx.Game;
import com.mg.game.observer.GameObserver;
import com.mg.game.menu.MenuScreen;

import java.util.ArrayList;
import java.util.List;

public class gdxGame extends Game {

	public MenuScreen menuScreen;
	private static boolean gameOverFlag = false;

	// New: list of observers
	private static final List<GameObserver> observers = new ArrayList<>();

	public static void setGameOverFlag() {
		gameOverFlag = true;
		notifyBaseDestroyed(); // now notify everyone
	}

	public static boolean isGameOver() {
		return gameOverFlag;
	}

	public static void resetGameOverFlag() {
		gameOverFlag = false;
	}

	@Override
	public void create(){
		menuScreen = new MenuScreen(this);
		setScreen(menuScreen);
	}

	// Methods for managing subscribers
	public static void addObserver(GameObserver observer) {
		if (!observers.contains(observer)) {
			observers.add(observer);
		}
	}

	public static void removeObserver(GameObserver observer) {
		observers.remove(observer);
	}

	public static void notifyBaseDestroyed() {
		for (GameObserver o : observers) {
			o.onBaseDestroyed();
		}
	}
}