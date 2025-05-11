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

        FileHandle sheetHandle = Gdx.files.internal("sprites/tiles/tileset.png");
        if (!sheetHandle.exists()) {
            Gdx.app.error("MapLoader", "Тайлсет не найден: sprites/tiles/tileset.png");
            return;
        }

        Texture sheet = new Texture(sheetHandle);

        // Судя по скриншоту, орёл уже найден правильно, но нужно настроить размер
        String[] lines = mapHandle.readString().split("\\r?\\n");

        // Найдем координаты базы в карте
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
            // Пропускаем все 4 тайла базы, чтобы не перезаписать их
            if ((mapRow == baseMapRow && mapCol == baseMapCol) ||
                    (mapRow == baseMapRow && mapCol == baseMapCol + 1) ||
                    (mapRow == baseMapRow + 1 && mapCol == baseMapCol) ||
                    (mapRow == baseMapRow + 1 && mapCol == baseMapCol + 1)) {
                continue;
            }


            // Находим базу (орёл) в файле карты
            if (tileRow == 2 && tileCol == 3) {
                baseMapRow = mapRow;
                baseMapCol = mapCol - 1;
                continue;
            }
        }

        // Если нашли базу, создаем для неё тайл 2×2
        if (baseMapRow != -1 && baseMapCol != -1) if (baseMapRow != -1 && baseMapCol != -1) {
            int eagleSheetRow = 2;
            int eagleSheetCol = 3;

            // Оригинальный орёл 32×32 (разбиваем на 4 части 16×16)
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
                    (eagleSheetCol + 2) * TILE_SIZE,
                    eagleSheetRow * TILE_SIZE,
                    TILE_SIZE * 2,
                    TILE_SIZE * 2
            );
            fullBroken.flip(true, true);

            // Разбиваем на 4 части (левый верхний, правый верхний, нижние два)
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

            Gdx.app.log("MapLoader", "База (орёл) 2x2 создана на позициях: (" + baseMapCol + "," + baseMapRow + ") до (" + (baseMapCol + 1) + "," + (baseMapRow + 1) + ")");
        }

        // Загружаем остальные тайлы
        for (String raw : lines) {
            String line = raw.trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split(",");
            int mapRow = Integer.parseInt(parts[0].trim());
            int mapCol = Integer.parseInt(parts[1].trim());
            int tileRow = Integer.parseInt(parts[2].trim());
            int tileCol = Integer.parseInt(parts[3].trim());

            // Пропускаем базу, мы уже её создали
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

            if (tileRow == 0 && tileCol == 0) {         // кирпич
                isSolid = true;
                isDestructible = true;
                damaged = new TextureRegion(sheet, TILE_SIZE, 0, TILE_SIZE, TILE_SIZE);
            } else if (tileRow == 1 && tileCol == 0) {  // сталь
                isSolid = true;
                isDestructible = false;
            }

            MapTile tile = new MapTile(region, mapCol, mapRow, isSolid, isDestructible);
            if (damaged != null) tile.setDamagedRegion(damaged);
            tiles.add(tile);
        }

        Gdx.app.log("MapLoader", "Всего тайлов загружено: " + tiles.size);
        for (MapTile tile : tiles) {
            if (tile.isBase) {
                Gdx.app.log("BASE_CHECK", "Base tile at map (" + tile.x + "," + tile.y + "), damagedRegion=" +
                        (tile.damagedRegion != null ? "YES" : "NO"));
            }
        }

    }
}