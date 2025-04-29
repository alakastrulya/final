package tanks.state;


public class MenuState implements GameState {
    private GameContext context;

    public MenuState(GameContext context) {
        this.context = context;
    }

    public void enter(){
        System.out.println("Here will be the graphics of the menu");
    }

    @Override
    public void update() {
        //here will be something in the future
    }

    public void render() {
        System.out.println("Here will be the components of the menu");
    }

    public void handleInput() {
        System.out.println("Press S to start");
        context.setState(new PlayingState(context));
    }

    @Override
    public void exit() {
        System.out.println("Exiting menu");
    }
}
