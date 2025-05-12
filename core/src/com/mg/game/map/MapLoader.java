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
        Gdx.app.log("MapLoader", "CWD = " + System.getProperty("user.dir") + ", looking for: " + mapPath);

        FileHandle mapHandle = Gdx.files.internal(mapPath);
        if (!mapHandle.exists()) {
            Gdx.app.error("MapLoader", "Map file not found: " + mapPath);
            return;
        }

        FileHandle sheetHandle = Gdx.files.internal("sprites/tiles/tileset.png");
        if (!sheetHandle.exists()) {
            Gdx.app.error("MapLoader", "Tileset not found: sprites/tiles/tileset.png");
            return;
        }

        Texture sheet = new Texture(sheetHandle);

        // According to the screenshot, the eagle is found correctly, but we need to adjust the size
        String[] lines = mapHandle.readString().split("\\r?\\n");

        // Find base (eagle) coordinates in the map
        int baseMapRow = -1;
        int baseMapCol = -1;

        for (String raw : lines) {
            String line = raw.trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split(",");
            int mapRow = Integer.parseInt(parts[0].trim());
            int mapCol = Integer.parseInt(parts[1].trim());
            int tileRow = Integer.parseInt(parts[2].trim());
            int tileCol = Integer.parseInt(parts[3].trim());
            // Skip all 4 base tiles to avoid overwriting
            if ((mapRow == baseMapRow && mapCol == baseMapCol) ||
                    (mapRow == baseMapRow && mapCol == baseMapCol + 1) ||
                    (mapRow == baseMapRow + 1 && mapCol == baseMapCol) ||
                    (mapRow == baseMapRow + 1 && mapCol == baseMapCol + 1)) {
                continue;
            }

            // Detect the base (eagle) in the map file
            if (tileRow == 2 && tileCol == 3) {
                baseMapRow = mapRow;
                baseMapCol = mapCol - 1;
                continue;
            }
        }

        // If base found, create 2x2 tile for it
        if (baseMapRow != -1 && baseMapCol != -1) if (baseMapRow != -1 && baseMapCol != -1) {
            int eagleSheetRow = 0;
            int eagleSheetCol = 0;

            // Original eagle 32×32 (split into four 16×16 parts)

            TextureRegion fullEagle = new TextureRegion(
                    sheet,
                    eagleSheetCol * TILE_SIZE,
                    eagleSheetRow * TILE_SIZE,
                    TILE_SIZE * 2,
                    TILE_SIZE * 2
            );
            fullEagle.flip(true, true);

            TextureRegion fullBroken = new TextureRegion(
                    sheet,
                    (eagleSheetCol + 1) * TILE_SIZE,
                    eagleSheetRow * TILE_SIZE,
                    TILE_SIZE * 2,
                    TILE_SIZE * 2
            );
            fullBroken.flip(true, true);

            // Split into 4 parts (top-left, top-right, bottom two)
            TextureRegion[][] eagleParts = fullEagle.split(TILE_SIZE, TILE_SIZE);
            TextureRegion[][] brokenParts = fullBroken.split(TILE_SIZE, TILE_SIZE);

            for (int dy = 0; dy < 2; dy++) {
                for (int dx = 0; dx < 2; dx++) {
                    int row = baseMapRow + dy;
                    int col = baseMapCol + dx;

                    MapTile tile = new MapTile(eagleParts[dy][dx], col, row, true, true);
                    tile.setBase(true);
                    tile.setDamagedRegion(brokenParts[dy][dx]);
                    tiles.add(tile);
                }
            }

            Gdx.app.log("MapLoader", "Base (eagle) 2x2 created at: (" + baseMapCol + "," + baseMapRow + ") to (" + (baseMapCol + 1) + "," + (baseMapRow + 1) + ")");
        }

        // Load remaining tiles
        for (String raw : lines) {
            String line = raw.trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split(",");
            int mapRow = Integer.parseInt(parts[0].trim());
            int mapCol = Integer.parseInt(parts[1].trim());
            int tileRow = Integer.parseInt(parts[2].trim());
            int tileCol = Integer.parseInt(parts[3].trim());

            // Skip the base — it's already added
            if (tileRow == 2 && tileCol == 3) {
                continue;
            }

            TextureRegion region = new TextureRegion(
                    sheet,
                    tileCol * TILE_SIZE,
                    tileRow * TILE_SIZE,
                    TILE_SIZE,
                    TILE_SIZE
            );

            boolean isSolid = false;
            boolean isDestructible = false;
            TextureRegion damaged = null;

            if (tileRow == 0 && tileCol == 0) {         // brick
                isSolid = true;
                isDestructible = true;
                damaged = new TextureRegion(sheet, TILE_SIZE, 0, TILE_SIZE, TILE_SIZE);
            } else if (tileRow == 1 && tileCol == 0) {  // steel
                isSolid = true;
                isDestructible = false;
            }

            MapTile tile = new MapTile(region, mapCol, mapRow, isSolid, isDestructible);
            if (damaged != null) tile.setDamagedRegion(damaged);
            tiles.add(tile);
        }

        Gdx.app.log("MapLoader", "Total tiles loaded: " + tiles.size);
        for (MapTile tile : tiles) {
            if (tile.isBase) {
                Gdx.app.log("BASE_CHECK", "Base tile at map (" + tile.x + "," + tile.y + "), damagedRegion=" +
                        (tile.damagedRegion != null ? "YES" : "NO"));
            }
        }

    }
}
