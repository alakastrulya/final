package com.mg.game.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

public class MapLoader {
    public static final int TILE_SIZE = 16;
    public final Array<MapTile> tiles = new Array<>();

    public MapLoader() {
        String mapPath = "maps/BattleCityTerrain01.txt";
        Gdx.app.log("MapLoader", "CWD = "
                + System.getProperty("user.dir")
                + ", looking for: " + mapPath);

        FileHandle mapHandle = Gdx.files.internal(mapPath);
        if (!mapHandle.exists()) {
            Gdx.app.error("MapLoader", "Map file not found: " + mapPath);
            return;
        }

        FileHandle sheetHandle = Gdx.files.internal("sprites/tiles/tileset.png");
        if (!sheetHandle.exists()) {
            Gdx.app.error("MapLoader", "Tileset not found");
            return;
        }

        Texture sheet = new Texture(sheetHandle);
        TextureRegion[][] all = TextureRegion.split(sheet, TILE_SIZE, TILE_SIZE);

        // Где в tileset лежит 2×2 спрайт орла (0-based)
        final int EAGLE_ROW       = 2;
        final int EAGLE_COL       = 3;
        final int BROKEN_COL      = 1;

        String[] lines = mapHandle.readString().split("\\r?\\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            String[] p = line.split(",");
            int mapRow  = Integer.parseInt(p[0].trim());
            int mapCol  = Integer.parseInt(p[1].trim());
            int tileRow = Integer.parseInt(p[2].trim());
            int tileCol = Integer.parseInt(p[3].trim());

            //  --- База: 2×2 клеток ---
            if (tileRow == EAGLE_ROW && tileCol == EAGLE_COL) {
                // захватываем 32×32 px сразу
                TextureRegion eagle = new TextureRegion(
                        sheet,
                        EAGLE_COL * TILE_SIZE,
                        EAGLE_ROW * TILE_SIZE,
                        TILE_SIZE * 2,
                        TILE_SIZE * 2
                );
                TextureRegion broken = new TextureRegion(
                        sheet,
                        BROKEN_COL * TILE_SIZE,
                        EAGLE_ROW * TILE_SIZE,
                        TILE_SIZE * 2,
                        TILE_SIZE * 2
                );
                // ставим вверх ногами
                eagle.flip(false, true);
                broken.flip(false, true);

                // один MapTile на 2×2
                MapTile tile = new MapTile(eagle, mapCol, mapRow, true, true);
                tile.setBase(true);
                tile.setDamagedRegion(broken);
                tiles.add(tile);
                continue;
            }

            //  --- Обычные тайлы (16×16) ---
            TextureRegion region = all[tileRow][tileCol];
            boolean isSolid = false, isDestructible = false;
            TextureRegion damaged = null;

            if (tileRow == 0 && tileCol == 0) {       // кирпич
                isSolid        = true;
                isDestructible = true;
                damaged        = all[0][1];
            } else if (tileRow == 1 && tileCol == 0) { // сталь
                isSolid        = true;
                isDestructible = false;
            }

            MapTile t = new MapTile(region, mapCol, mapRow, isSolid, isDestructible);
            if (damaged != null) t.setDamagedRegion(damaged);
            tiles.add(t);
        }

        Gdx.app.log("MapLoader", "Total tiles: " + tiles.size);
    }
}
