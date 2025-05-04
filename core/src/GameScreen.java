import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class GameScreen implements Screen {
    private int playerCount;
    private gdxGame game;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private float stateTime;
    private Tank player1;
    private Sound startLevelSound;
    private Sound engineSound;

    public GameScreen(gdxGame game, int playerCount) {
        this.playerCount = playerCount;
        this.game = game;
        camera = new OrthographicCamera();
        camera.setToOrtho(true, 640, 480);
        batch = new SpriteBatch();
        stateTime = 0F;
        player1 = new Tank("yellow", 1);
    }
    public GameScreen(int playerCount) {
        this.playerCount = playerCount;
    }

    @Override
    public void pause(){
        System.out.println("Game Paused");
    }

    @Override
    public void resume(){
        System.out.println("Game resumed");
    }

    @Override
    public void show(){
        System.out.println("Player count: " + playerCount);
    }

    @Override
    public void hide(){
        System.out.println("Hiding tanks");
    }

    @Override
    public void dispose(){
        System.out.println("Game disposed");
    }

    @Override
    public void resize(int width, int height){
        System.out.println("Game resized");
    }
}
