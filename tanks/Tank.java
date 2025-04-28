package tanks;

import java.awt.Graphics;

public class Tank extends Sprite implements Entity {

    public Tank(Team team) {
        setTeam(team);
        // Add health, speed, and etc
    }

    @Override
    public void move() {
        // Function which make tank's actions
    }

    @Override
    public void draw(Graphics g) {
        // Function which draw tanks
    }
}
