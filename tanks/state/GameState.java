package tanks.state;

public interface GameState {
    void enter();
    void update();
    void render();
    void handleInput();
    void exit();
}
