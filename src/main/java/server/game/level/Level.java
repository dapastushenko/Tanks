package server.game.level;

import server.game.ServerGame;
import server.graphics.TextureAtlas;
import server.utils.Utils;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static server.game.ServerGame.ATLAS_FILE_NAME;

public class Level implements Serializable {
    public static final int TILE_SCALE = 8; //размер 1 блока объекта
    public static final int TILE_IN_GAME_SCALE = 2; //константа для увеличения изображения
    public static final int SCALED_TILES_SIZE = TILE_SCALE * TILE_IN_GAME_SCALE;
    public static final int TILE_IN_WIDTH = ServerGame.WIDTH / SCALED_TILES_SIZE;
    public static final int TILE_IN_HEIGHT = ServerGame.HEIGHT / SCALED_TILES_SIZE;

    private Integer[][] tileMap;
    private static Map<TileType, Tile> tiles;
    public static final TextureAtlas atlas;
    private List<Point> grassCords;
    public List<Point> tilesCords;

    static {
        atlas = new TextureAtlas(ATLAS_FILE_NAME);

        tiles = new HashMap<>();
        tiles.put(TileType.BRICK, new Tile(atlas.cut(32 * TILE_SCALE, 0 * TILE_SCALE, TILE_SCALE, TILE_SCALE), TILE_IN_GAME_SCALE, TileType.BRICK));
        tiles.put(TileType.METAL, new Tile(atlas.cut(32 * TILE_SCALE, 2 * TILE_SCALE, TILE_SCALE, TILE_SCALE), TILE_IN_GAME_SCALE, TileType.METAL));
        tiles.put(TileType.WATER, new Tile(atlas.cut(32 * TILE_SCALE, 4 * TILE_SCALE, TILE_SCALE, TILE_SCALE), TILE_IN_GAME_SCALE, TileType.WATER));
        tiles.put(TileType.GRASS, new Tile(atlas.cut(34 * TILE_SCALE, 4 * TILE_SCALE, TILE_SCALE, TILE_SCALE), TILE_IN_GAME_SCALE, TileType.GRASS));
        tiles.put(TileType.ICE, new Tile(atlas.cut(36 * TILE_SCALE, 4 * TILE_SCALE, TILE_SCALE, TILE_SCALE), TILE_IN_GAME_SCALE, TileType.ICE));
        tiles.put(TileType.EMPTY, new Tile(atlas.cut(36 * TILE_SCALE, 6 * TILE_SCALE, TILE_SCALE, TILE_SCALE), TILE_IN_GAME_SCALE, TileType.EMPTY));
    }

    public Level() {
        tileMap = new Integer[TILE_IN_WIDTH][TILE_IN_HEIGHT];

        // tileMap = наименование файла

        tileMap = Utils.lvlParser("level.lvl");
        grassCords = new ArrayList<Point>();
        tilesCords = new ArrayList<Point>();
        for (int i = 0; i < tileMap.length; i++) {
            for (int j = 0; j < tileMap[i].length; j++) {
                Tile tile = tiles.get(TileType.fromNumeric(tileMap[i][j]));
                if (tile.type() == TileType.GRASS) {
//                    tiles.get(TileType.fromNumeric(tileMap[i][j])).render(g, j * SCALED_TILES_SIZE, i * SCALED_TILES_SIZE);//прыгаем по координатам где находит ся след тайл
                    grassCords.add(new Point(j * SCALED_TILES_SIZE, i * SCALED_TILES_SIZE));
                    tilesCords.add(new Point(j * SCALED_TILES_SIZE, i * SCALED_TILES_SIZE));
                } else if (tile.type() == TileType.BRICK) {
                    tilesCords.add(new Point(j * SCALED_TILES_SIZE, i * SCALED_TILES_SIZE));
                }
            }

        }
    }

    public void update(int tileX, int tileY) {
        tileMap[tileY][tileX] = TileType.EMPTY.numeric();
    }

    public void render(Graphics2D g) {
        for (int i = 0; i < tileMap.length; i++) {
            for (int j = 0; j < tileMap[i].length; j++) {
                Tile tile = tiles.get(TileType.fromNumeric(tileMap[i][j]));
                if (tile.type() != TileType.GRASS) {
//                    tiles.get(TileType.fromNumeric(tileMap[i][j])).render(g, j * SCALED_TILES_SIZE, i * SCALED_TILES_SIZE);//прыгаем по координатам где находит ся след тайл
                    tile.render(g, j * SCALED_TILES_SIZE, i * SCALED_TILES_SIZE);
                }
            }
        }
    }

    public void renderGrass(Graphics2D g) {
        for (Point p : grassCords) {
            tiles.get(TileType.GRASS).render(g, p.x, p.y);
        }
    }

    public Integer[][] getTileMap() {
        return tileMap;
    }
}
