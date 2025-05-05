package com.mg.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class MenuScreen implements Screen{

    private gdxGame game;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private int selectorPosition = 305;
    private float stateTime;

    MenuScreen(gdxGame game){
        this.game = game;
        camera = new OrthographicCamera();
        camera.setToOrtho(true, 640, 480);
        batch = new SpriteBatch();
        stateTime = 0F;
        Assets.loadMenuAssets();
    }

    @Override
    public void render(float delta){

        Gdx.gl.glClearColor(0.95F, 0.95F, 0.95F, 0.95F);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();
        stateTime += Gdx.graphics.getDeltaTime();
        Assets.current_frame = Assets.movingTankAnimation.getKeyFrame(stateTime, true);
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        batch.draw(Assets.spriteBack, 0, 0, 640, 480);
        batch.draw(Assets.current_frame, 190, selectorPosition, 26, 26);
        batch.end();

        if(Gdx.input.isKeyPressed(Input.Keys.DOWN)){
            if (selectorPosition != 342){
                Assets.selectionSound.play();
                selectorPosition = 342;
            }
        } else if (Gdx.input.isKeyPressed(Input.Keys.UP)){
            if (selectorPosition != 305){
                Assets.selectionSound.play();
                selectorPosition = 305;
            }
        } else if (Gdx.input.isKeyPressed(Input.Keys.ENTER)){
            if (selectorPosition == 305){
                game.setScreen(new GameScreen(game, 1));
            } else if (selectorPosition == 342){
                game.setScreen(new GameScreen(game, 2));
            }
        }
    }

    @Override
    public void resize(int width, int height){
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
}
