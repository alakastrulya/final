import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

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

    // Render basics for each screen
    @Override
    public void render(float delta){

        Gdx.gl.glClearColor((float)192/255,(float)192/255,(float)192/255,1);
        Gdx.gl.glClear( GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT );
        camera.update();
        stateTime += Gdx.graphics.getDeltaTime();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(Assets.levelBack, 0, 0, 480, 480);
        batch.draw(frame, player1.positionX, player1.positionY, 26, 26);
        batch.end();
    }

    private TextureRegion checkKeyPress(){
        if(Gdx.input.isKeyPressed(Input.Keys.DOWN)){
            player1.moveDown();
            Assets.current_frame = Assets.movingForwardAnimation.getKeyFrame(stateTime, true);
        } }
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
