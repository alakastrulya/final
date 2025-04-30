package tanks.state;
//context that will manage the current state of the game
public class GameContext {
    private GameState currentState;


    //if the state changes then it exits the current state
    public void setState(GameState state) {
        if(currentState != null) {
            currentState.exit();
        }
        //entering a new state
        currentState = state;
        currentState.enter();
    }

    //updating
    public void update() {
        if(currentState != null) {
            currentState.update();
        }
    }

    //rendering
    public void render (){
        if(currentState != null) {
            currentState.handleInput();
        }
    }

    //game input processing
    public void handleInput(){
        if(currentState != null) {
            currentState.handleInput();
        }
    }


}
