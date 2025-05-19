package com.mg.game.map;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.Gdx;
import com.mg.game.observer.GameContext;
import com.mg.game.observer.GameEventPublisher;

public class MapTile {
    private com.mg.game.observer.GameEventPublisher eventPublisher;
    private int level;
    private int playerCount;
    public TextureRegion region;
    public TextureRegion damagedRegion;
    public final int x, y;
    public boolean isSolid;
    public boolean isDestructible;
    public boolean isBase = false; // ← base (eagle)
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

    // old general setter, if still needed
    public void setEventPublisher(GameEventPublisher publisher, int level, int playerCount) {
        this.eventPublisher = publisher;
        this.level = level;
        this.playerCount = playerCount;
    }
    public void setDamagedRegion(TextureRegion damaged) {
        this.damagedRegion = new TextureRegion(damaged);
    }

    public void setDamagedTopRegion(TextureRegion r)    { this.damagedTop = r; }
    public void setDamagedBottomRegion(TextureRegion r) { this.damagedBottom = r; }
    public void setDamagedLeftRegion(TextureRegion r)   { this.damagedLeft = r; }
    public void setDamagedRightRegion(TextureRegion r)  { this.damagedRight = r; }

    // —— Add this method ——
    public void setBase(boolean isBase) {
        this.isBase = isBase;
    }

    public void takeHit() {
        if (!isDestructible) return;

        isSolid = false;
        isDestructible = false;

        if (damagedRegion != null) {
            this.region = new TextureRegion(damagedRegion);
        }

        if (isBase) {
            Gdx.app.log("GameScreen", "Eagle was hit!");
            if (eventPublisher != null) {
                eventPublisher.notifyBaseDestroyed(new GameContext(level, playerCount, "enemy"));
            }
        }
    }
}