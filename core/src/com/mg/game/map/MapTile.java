package com.mg.game.map;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.mg.game.Tank;

public class MapTile {
    public TextureRegion region;
    public TextureRegion damagedRegion;
    public final int x, y;
    public boolean isSolid;
    public boolean isDestructible;
    private int hitPoints = 2;

    public MapTile(TextureRegion region, int x, int y, boolean isSolid, boolean isDestructible) {
        this.region = new TextureRegion(region);
        this.x = x;
        this.y = y;
        this.isSolid = isSolid;
        this.isDestructible = isDestructible;
    }

    public Rectangle getBounds(int tileSize, float scale, int offsetX, int offsetY) {
        float scaledSize = tileSize / scale;
        float drawX = x * scaledSize + offsetX;
        float drawY = y * scaledSize + offsetY;
        return new Rectangle(drawX, drawY, scaledSize, scaledSize);
    }

    public void setDamagedRegion(TextureRegion damaged) {
        this.damagedRegion = new TextureRegion(damaged);
    }

    public void takeHit() {
        if (!isDestructible) return;
        hitPoints--;
        if (hitPoints == 1 && damagedRegion != null) {
            this.region = new TextureRegion(damagedRegion);
        } else if (hitPoints <= 0) {
            isSolid = false;
            isDestructible = false;
        }
    }
}

