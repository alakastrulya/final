package com.mg.game;
import com.badlogic.gdx.Screen;
import com.mg.game.gdxGame;
import com.mg.game.menu.MenuScreen;

public class Page implements Screen {
    private gdxGame game;
    private MenuScreen menuScreen;

    public Page(gdxGame game) {
        this.game = game;
        this.menuScreen = new MenuScreen(game);
    }

    @Override
    public void show() {
        game.setScreen(menuScreen);
    }

    @Override
    public void render(float delta) {
        // Delegate rendering to the menu screen
        menuScreen.render(delta);
    }

    @Override
    public void resize(int width, int height) {
        menuScreen.resize(width, height);
    }

    @Override
    public void pause() {
        menuScreen.pause();
    }

    @Override
    public void resume() {
        menuScreen.resume();
    }

    @Override
    public void hide() {
        menuScreen.hide();
    }

    @Override
    public void dispose() {
        menuScreen.dispose();
    }
}