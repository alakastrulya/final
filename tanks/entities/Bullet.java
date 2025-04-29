package tanks.entities;

import tanks.core.Sprite;
import tanks.core.Entity;

import java.awt.*;

public class Bullet extends Sprite implements Entity {
    private Tank shooter;

    public Bullet(Tank shooter) {
        this.shooter = shooter;

        setSideLength(6);
        setCenter(new Point(shooter.getCenter().x, shooter.getCenter().y));
        // Starting position and coordinates
    }

    @Override
    public void move() {
        // Function which make bullet's actions
    }

    @Override
    public void draw(Graphics g) {
        // Function which draw bullet
    }

}
