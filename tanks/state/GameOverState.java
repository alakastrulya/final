package tanks.state;

public class GameOverState implements GameState{
    private GameContext context;
    public GameOverState(GameContext context) {
        this.context = context;
    }

    @Override
    public void enter() {
        System.out.println("Action after starting game");
    }

    @Override
    public void update() {
        System.out.println("Action after updating game");
    }

    @Override
    public void render() {
        System.out.println("Action after rendering game");
    }

    @Override
    public void handleInput() {
        System.out.println("Action after handling game");
    }

    @Override
    public void exit() {
        System.out.println("Action after exiting game");
    }
}
