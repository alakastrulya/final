package com.mg.game.desktop;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mg.game.gdxGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "Battle City";
		cfg.useGL30 = true;
		cfg.width = 640;
		cfg.height = 480;
		cfg.resizable = false;
		cfg.addIcon("sprites/icons/upIcon.png", Files.FileType.Internal);

		new LwjglApplication(new gdxGame(), cfg);
	}
}
