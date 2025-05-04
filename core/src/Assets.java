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

}
