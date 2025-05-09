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
        int cols = sheet.getWidth() / TILE_SIZE;
        int rows = sheet.getHeight() / TILE_SIZE;
        Gdx.app.log("MapLoader", "Loaded tilesheet " + sheet.getWidth() + "×" + sheet.getHeight() +
                ", TILE_SIZE=" + TILE_SIZE + " => rows=" + rows + ", cols=" + cols);

        TextureRegion[][] all = TextureRegion.split(sheet, TILE_SIZE, TILE_SIZE);

        String[] lines = mapHandle.readString().split("\r?\n");
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

                // … внутри цикла по строкам карты, вместо старой логики:
                TextureRegion region = all[tileRow][tileCol];
                TextureRegion damagedTop    = null;
                TextureRegion damagedBottom = null;
                TextureRegion damagedLeft   = null;
                TextureRegion damagedRight  = null;
                boolean isSolid      = false;
                boolean isDestructible = false;
                boolean isBase       = false;

                if (tileRow == 0 && tileCol == 0) { // Кирпич
                    isSolid       = true;
                    isDestructible = true;
                    // Четыре состояния повреждения
                    damagedTop    = all[0][2];
                    damagedBottom = all[0][1];
                    damagedLeft   = all[0][4];
                    damagedRight  = all[0][3];
                } else if (tileRow == 1 && tileCol == 0) { // Сталь
                    isSolid       = true;
                    isDestructible = false;
                } else if (tileRow == 2 && tileCol == 0) { // Орёл (база)
                    isSolid       = true;
                    isDestructible = true;
                    damagedTop    = all[2][1]; // разрушённая база
                    isBase        = true;
                }

// Создаём тайл и устанавливаем повреждения
                MapTile tile = new MapTile(region, mapCol, mapRow, isSolid, isDestructible);
                if (damagedTop != null) {
                    tile.setDamagedTopRegion(damagedTop);
                    tile.setDamagedBottomRegion(damagedBottom);
                    tile.setDamagedLeftRegion(damagedLeft);
                    tile.setDamagedRightRegion(damagedRight);
                }
                if (isBase) {
                    tile.setBase(true);
                }
                tiles.add(tile);


            } catch (NumberFormatException ex) {
                Gdx.app.error("MapLoader", "Ошибка разбора числа в строке " + (i + 1) + ": " + line);
            }
        }
    }
}
