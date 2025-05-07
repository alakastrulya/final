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
            Gdx.app.error("MapLoader", "Файл карты не найден: " + mapPath);
            return;
        }

        String tileSheetPath = "sprites/tiles/tileset.png";
        FileHandle sheetHandle = Gdx.files.internal(tileSheetPath);
        if (!sheetHandle.exists()) {
            Gdx.app.error("MapLoader", "Тайлсет не найден: " + tileSheetPath);
            return;
        }

        Texture sheet = new Texture(sheetHandle);
        int sheetW = sheet.getWidth();
        int sheetH = sheet.getHeight();
        int cols = sheetW / TILE_SIZE;
        int rows = sheetH / TILE_SIZE;
        Gdx.app.log("MapLoader", "Loaded tilesheet " + sheetW + "×" + sheetH + ", TILE_SIZE=" + TILE_SIZE + " => rows=" + rows + ", cols=" + cols);

        TextureRegion[][] all = TextureRegion.split(sheet, TILE_SIZE, TILE_SIZE);

        String[] lines = mapHandle.readString().split("\\r?\\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split(",");
            if (parts.length != 4) {
                Gdx.app.error("MapLoader", "Неправильная строка в " + mapPath + " на строке " + (i + 1) + ": " + line);
                continue;
            }

            try {
                int mapRow = Integer.parseInt(parts[0].trim());
                int mapCol = Integer.parseInt(parts[1].trim());
                int tileRow = Integer.parseInt(parts[2].trim());
                int tileCol = Integer.parseInt(parts[3].trim());

                if (tileRow < 0 || tileRow >= rows || tileCol < 0 || tileCol >= cols) {
                    Gdx.app.error("MapLoader", "Тайл индекс вне диапазона: row=" + tileRow + ", col=" + tileCol);
                    continue;
                }

                TextureRegion region = all[tileRow][tileCol];

                boolean isSolid = false;
                boolean isDestructible = true;

                // === Настрой тайлы вручную ===
                if (tileRow == 0 && tileCol == 0) { // кирпич
                    isSolid = true;
                    isDestructible = true;
                } else if (tileRow == 1 && tileCol == 0) { // стальная стена (неразрушаемая)
                    isSolid = true;
                    isDestructible = false;
                }

                tiles.add(new MapTile(region, mapCol, mapRow, isSolid, isDestructible));

            } catch (NumberFormatException ex) {
                Gdx.app.error("MapLoader", "Ошибка разбора числа в строке " + (i + 1) + ": " + line);
            }
        }
    }
}
