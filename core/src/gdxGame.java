import com.badlogic.gdx.Game;

public class gdxGame extends Game {

    public MenuScreen menuScreen;

    @Override
    public void create(){
        menuScreen = new MenuScreen(this);
        setScreen(menuScreen);
    }
}