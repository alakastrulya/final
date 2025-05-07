package com.mg.game.map;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public class MapTile {
    public final TextureRegion region;
    public final int x, y;
    public boolean isSolid;
    public boolean isDestructible; // <- добавлено

    public MapTile(TextureRegion region, int x, int y, boolean isSolid, boolean isDestructible) {
        this.region = region;
        this.x = x;
        this.y = y;
        this.isSolid = isSolid;
        this.isDestructible = isDestructible;
    }

    public MapTile(TextureRegion region, int x, int y) {
        this(region, x, y, false, false); // по умолчанию — не solid и не разрушается
    }

    public Rectangle getBounds(int tileSize, float scale, int offsetX, int offsetY) {
        float scaledSize = tileSize / scale;
        float drawX = x * scaledSize + offsetX;
        float drawY = y * scaledSize + offsetY;
        return new Rectangle(drawX, drawY, scaledSize, scaledSize);
    }

    public int getFlippedY(int screenHeight, int tileSize) {
        int totalTilesY = screenHeight / tileSize;
        return totalTilesY - y - 1;
    }
}
