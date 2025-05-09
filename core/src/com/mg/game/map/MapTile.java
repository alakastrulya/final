package com.mg.game.map;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public class MapTile {
    public TextureRegion region;
    public TextureRegion damagedRegion;
    public final int x, y;
    public boolean isSolid;
    public boolean isDestructible;
    public boolean isBase = false; // ← база (орёл)
    private int hitPoints = 2;
    private TextureRegion damagedTop, damagedBottom, damagedLeft, damagedRight;

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

    // старый общий сеттер, если он ещё нужен
    public void setDamagedRegion(TextureRegion damaged) {
        this.damagedRegion = new TextureRegion(damaged);
    }

    public void setDamagedTopRegion(TextureRegion r)    { this.damagedTop = r; }
    public void setDamagedBottomRegion(TextureRegion r) { this.damagedBottom = r; }
    public void setDamagedLeftRegion(TextureRegion r)   { this.damagedLeft = r; }
    public void setDamagedRightRegion(TextureRegion r)  { this.damagedRight = r; }

    // —— Добавляем этот метод ——
    public void setBase(boolean isBase) {
        this.isBase = isBase;
    }

    public void takeHit() {
        if (!isDestructible) return;

        // Помечаем тайл как неавтразрушаемый и проходной
        isSolid = false;
        isDestructible = false;

        // Если хотите сразу менять спрайт на разрушенный (например, damagedRegion), можно раскомментировать:
        // if (damagedRegion != null) {
        //     this.region = new TextureRegion(damagedRegion);
        // }
    }
}
