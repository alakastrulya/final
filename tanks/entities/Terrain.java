package tanks.entities;

import tanks.core.Sprite;
import tanks.factories.Entity;

import java.awt.*;

public class Terrain extends Sprite implements Entity {

    public Terrain(String type, int row, int col, int convID) {
        // Create parameters for Terrain
    }

    @Override
    public void move() {
        // Function which make terrain's action (delete some elements when Bullet touches it)
    }

    @Override
    public void draw(Graphics g) {
        // Create draw terrain
    }
}
