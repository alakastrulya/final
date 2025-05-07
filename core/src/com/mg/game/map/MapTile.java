package com.mg.game.map;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public class MapTile {
    public final TextureRegion region;
    public final int x, y;

    public MapTile(TextureRegion region, int x, int y) {
        this.region = region;
        this.x = x;
        this.y = y;
    }

    public Rectangle getBounds(int tileSize) {
        return new Rectangle(x * tileSize, y * tileSize, tileSize, tileSize);
    }

    public int getFlippedY(int screenHeight, int tileSize) {
        int totalTilesY = screenHeight / tileSize;
        return totalTilesY - y - 1;
    }

}
