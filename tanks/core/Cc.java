package tanks.core;

import tanks.entities.Tank;
import tanks.enums.CollisionOpList;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Cc { // Class Cc (Singleton), it manages all date from game

    private static final Cc instance = new Cc();
    private final List<Sprite> sprites;

    private Cc() {
        this.sprites = new ArrayList<>();
    }

    public static Cc getInstance() { // Function which return object of this class (Singleton pattern)
        return instance;
    }

    public void addSprite(Sprite sprite) {
        sprites.add(sprite);
    }

    public List<Sprite> getSprites() {
        return sprites;
    }

    public void updateAll() {
        for (Sprite sprite : sprites) {
            sprite.move();
        }
    }

    public void draw(Graphics g) {
        for (Sprite sprite : sprites) {
            sprite.draw(g);
        }
    }
}
