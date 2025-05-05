import com.badlogic.gdx.audio.Sound;
public class Tank{

    public String colour;
    public int level;
    public static Sound levelBeginSound;

    public int positionX;
    public int positionY;

    Tank(String colour, int level){
        this.colour = colour;
        this.level = level;
    }

    void moveUp(){
        if (!(positionY - 1 < 0)){
            positionY -= 1;
        }
    }

    void moveDown(){
        if (!(positionY + 1 > 454)){
            positionY += 1;
        }
    }

    void moveLeft(){
        if (!(positionX - 1 < 0)){
            positionX -= 1;
        }
    }

    void moveRight(){
        if (!(positionX + 1 > 454)){
            positionX += 1;
        }
    }

    void shoot(){

    }

    public String getColour(){
        return colour;
    }

    public void setColour(String colour){
        this.colour = colour;
    }

    public int getLevel(){
        return level;
    }

    public void setLevel(int level){
        this.level = level;
    }
}