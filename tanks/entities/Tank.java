package tanks.entities;

import tanks.core.Sprite;
import tanks.factories.Entity;

import java.awt.*;

public class Tank extends Sprite implements Entity {
    private int id;

    public Tank(Team team, int id) {
        setTeam(team);
        this.id = id;

        setSideLength(24);
        setCenter(new Point(100, 100));
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
