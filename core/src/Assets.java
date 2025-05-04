import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Assets {
    public static Texture textureBack;
    public static Sprite spriteBack;
    public static Texture yellowTankRight1_Texture;
    public static Texture yellowTankRight2_Texture;
    public static Animation<TextureRegion> movingTankAnimation;
    public static TextureRegion[] sheet_frames ;
    public static TextureRegion current_frame;
    public static Sprite levelBack;
    public static Sound selectionSound;
    public static Sound levelBeginSound;

    public static Animation<TextureRegion> movingForwardAnimation;
    public static Animation<TextureRegion> standByForwardAnimation;
    public static Animation<TextureRegion> movingBackwardAnimation;
    public static Animation<TextureRegion> standByBackwardAnimation;
    public static Animation<TextureRegion> movingLeftAnimation;
    public static Animation<TextureRegion> standByLeftAnimation;
    public static Animation<TextureRegion> movingRightAnimation;
    public static Animation<TextureRegion> standByRightAnimation;

    public static float elapsedTime;
    public static String colour;
    public static int level;

    public static void loadMenuAssets(){
        textureBack = new Texture(Gdx.files.internal("sprites/menu/menu.jpg"));
        textureBack.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        spriteBack = new Sprite(textureBack);
        spriteBack.flip(false, true);

        yellowTankRight1_Texture = new Texture(Gdx.files.internal("sprites/tanks/yellow/level_1/right1.png"));
        yellowTankRight2_Texture = new Texture(Gdx.files.internal("sprites/tanks/yellow/level_1/right2.png"));
        sheet_frames = new TextureRegion[2];

        sheet_frames[1] = new TextureRegion(yellowTankRight1_Texture, 0,0, 13, 13);
        sheet_frames[0] = new TextureRegion(yellowTankRight2_Texture, 0,0, 13, 13);

        movingTankAnimation = new Animation<>(0.1F, sheet_frames);
        selectionSound = Gdx.audio.newSound(Gdx.files.internal("sounds/menuSelect.mp3"));
    }

    public static void loadGameAssets(String colour, int level){
        loadLevel(1);
        loadTankAnimations(colour, level);
        loadSounds();

    }

}
