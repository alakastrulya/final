package com.mg.game;

import com.badlogic.gdx.Screen;

public interface GameState {
    void enter(); // entering  state
    void exit();  // exiting  state
    void update(); // Update game logic
    void render(); // rendering
    Screen getScreen(); // libgdx screen
}