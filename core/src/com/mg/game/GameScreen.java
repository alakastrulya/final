package com.mg.game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class GameScreen implements Screen{

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
        Music levelSound = Gdx.audio.newMusic(Gdx.files.internal("sounds/startLevel.mp3"));
        levelSound.play();
        Assets.loadGameAssets(player1.getColour(), player1.getLevel());
    }

    // Render basics for each screen
    @Override
    public void render(float delta){

        Gdx.gl.glClearColor((float)192/255,(float)192/255,(float)192/255,1);
        Gdx.gl.glClear( GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT );
        camera.update();
        stateTime += Gdx.graphics.getDeltaTime();
        TextureRegion frame = checkKeyPress();
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
        } else if (Gdx.input.isKeyPressed(Input.Keys.UP)){
            player1.moveUp();
            Assets.current_frame = Assets.movingBackwardAnimation.getKeyFrame(stateTime, true);
        } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            player1.moveLeft();
            Assets.current_frame = Assets.movingLeftAnimation.getKeyFrame(stateTime, true);
        } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            player1.moveRight();
            Assets.current_frame = Assets.movingRightAnimation.getKeyFrame(stateTime, true);
        } else if (Gdx.input.isKeyPressed(Input.Keys.SPACE)){
            player1.shoot();

        }

        return Assets.current_frame;
    }

    @Override
    public void pause(){
    }

    @Override
    public void resume(){
    }

    @Override
    public void show(){

    }

    @Override
    public void hide(){
    }

    @Override
    public void dispose(){
    }

    @Override
    public void resize(int width, int height){

    }
}
