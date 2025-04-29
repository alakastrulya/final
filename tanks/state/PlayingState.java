package tanks.state;

public class PlayingState implements GameState{
    private GameContext context;

    public PlayingState(GameContext context) {
        this.context = context;
    }

    @Override
    public void enter() {
        System.out.println("Starting the Game..");
    }

    @Override
    public void update() {
        System.out.println("Updating the Game..");
    }

    @Override
    public void render() {
        System.out.println("Rendering the Game Components.");
    }

    @Override
    public void handleInput() {
        System.out.println("Handle Input Pressed");
    }

    @Override
    public void exit() {
        System.out.println("Exiting the Game..");
    }
}
