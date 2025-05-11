package com.mg.game;


import com.badlogic.gdx.Game;

public class gdxGame extends Game {

	public MenuScreen menuScreen;
	private static boolean gameOverFlag = false;

	public static void setGameOverFlag() {
		gameOverFlag = true;
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
}
