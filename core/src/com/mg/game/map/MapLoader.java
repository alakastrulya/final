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

            // Находим базу (орёл) в файле карты
            if (tileRow == 2 && tileCol == 3) {
                baseMapRow = mapRow;
                baseMapCol = mapCol;
                break;
            }
        }

        // Если нашли базу, создаем для неё тайл 2×2
        if (baseMapRow != -1 && baseMapCol != -1) {
            // Координаты орла в тайлсете (используем те, что уже работают)
            // Судя по скриншоту, орёл уже отображается правильно
            int eagleSheetRow = 2; // Строка орла в тайлсете
            int eagleSheetCol = 3; // Столбец орла в тайлсете

            // Создаём текстуру орла
            TextureRegion eagle = new TextureRegion(
                    sheet,
                    eagleSheetCol * TILE_SIZE,
                    eagleSheetRow * TILE_SIZE,
                    TILE_SIZE,
                    TILE_SIZE
            );

            // Создаём текстуру сломанного орла
            TextureRegion broken = new TextureRegion(
                    sheet,
                    (eagleSheetCol + 1) * TILE_SIZE, // Предполагаем, что сломанный орёл справа
                    eagleSheetRow * TILE_SIZE,
                    TILE_SIZE,
                    TILE_SIZE
            );

            // Исправляем перевёрнутость, если нужно
            eagle.flip(true, true); // Уже не нужно, т.к. орёл отображается правильно

            // Создаём тайл базы
            MapTile baseTile = new MapTile(eagle, baseMapCol, baseMapRow, true, true);
            baseTile.setBase(true);
            baseTile.setDamagedRegion(broken);
            tiles.add(baseTile);

            Gdx.app.log("MapLoader", "База (орёл) создана на позиции: " + baseMapCol + "," + baseMapRow);
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
    }
}