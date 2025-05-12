package com.mg.game.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mg.game.assets.Assets;

public  class TextureUtils {
    /**
     * Checks if a file exists and logs the result
     * @param path The path to check
     * @return true if the file exists, false otherwise
     */
    public static boolean
    fileExists(String path) {
        FileHandle file = Gdx.files.internal(path);
        boolean exists = file.exists();
        if (exists) {
            Gdx.app.log("TextureUtils", "File exists: " + path);
        } else {
            Gdx.app.error("TextureUtils", "File does not exist: " + path);
        }
        return exists;
    }

    /**
     * Lists all files in a directory
     * @param directory The directory to list
     */
    public static void
    listFiles(String directory) {
        FileHandle dir = Gdx.files.internal(directory);
        if (dir.exists() && dir.isDirectory()) {
            Gdx.app.log("TextureUtils", "Listing files in: " + directory);
            for (FileHandle file : dir.list())
                Gdx.app.log("TextureUtils", "- " + file.name());
        } else {
            Gdx.app.error("TextureUtils", "Directory does not exist: " + directory);
        }
    }

    /**
     * Loads a texture and flips it vertically
     * @param path The path to the texture
     * @return The flipped texture, or null if loading failed
     */
    public static Texture
    loadAndFlipTexture(String path) {
        try {
            if (!fileExists(path)) {
                return null;
            }

            Texture tex = new Texture(Gdx.files.internal(path));
            TextureRegion region = new TextureRegion(tex);
            region.flip(false, true);

            Pixmap pixmap = null;
            try {
                pixmap = tex.getTextureData().consumePixmap();
                Texture flipped = new Texture(pixmap);
                tex.dispose();
                return flipped;
            } finally {
                if (pixmap != null) {
                    pixmap.dispose();
                }
            }
        } catch (Exception e){
        Gdx.app.error("TextureUtils", "Failed to load texture: " + path, e);
        return null;
    }}

    /**
     * Adds debug information to the game screen
     * @param batch The SpriteBatch to draw with
     * @param font The font to use
     * @param x The x position
     * @param y The y position
     */
    public static void
    drawDebugInfo(com.badlogic.gdx.graphics.g2d.SpriteBatch batch,
                  com.badlogic.gdx.graphics.g2d.BitmapFont font,
                  float x, float y) {
        font.draw(batch, "UI Assets:", x, y);
        font.draw(batch, "hiScore: " + (Assets.hiScoreTexture != null), x, y + 20);
        font.draw(batch, "stage: " + (Assets.stageTexture != null), x, y + 40);
        font.draw(batch, "iPlayer: " + (Assets.iPlayerTexture != null), x, y + 60);
        font.draw(batch, "iiPlayer: " + (Assets.iiPlayerTexture != null), x, y + 80);
        font.draw(batch, "pts: " + (Assets.ptsTexture != null), x, y + 100);
        font.draw(batch, "total: " + (Assets.totalTexture != null), x, y + 120);
        font.draw(batch, "tankIcon: " + (Assets.tankIconTexture != null), x, y + 140);
        font.draw(batch, "arrow: " + (Assets.arrowTexture != null), x, y + 160);

        // Check if digit textures are loaded
        boolean digitsLoaded = Assets.digitTextures != null;
        int digitCount = digitsLoaded ? Assets.digitTextures.length : 0;
        font.draw(batch, "digits: " + digitsLoaded + " (" + digitCount + ")", x, y + 180);
    }
}
