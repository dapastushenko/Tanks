package game.level;

import game.Game;
import graphics.TextureAtlas;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Level {
    public static final int TILE_SCALE = 8; //размер 1 блока объекта
    public static final int TILE_IN_GAME_SCALE = 2; //константа для увеличения изображения
    public static final int SCALED_TILES_SIZE = TILE_SCALE * TILE_IN_GAME_SCALE;
    public static final int TILE_IN_WIDTH = Game.WIDTH / SCALED_TILES_SIZE;
    public static final int TILE_IN_HEIGHT = Game.HEIGHT / SCALED_TILES_SIZE;

    private int[][] tileMap;
    private Map<TileType, Tile> tiles;
    private List<Point> grassCords;

    public Level(TextureAtlas atlas) {
        tileMap = new int[TILE_IN_WIDTH][TILE_IN_HEIGHT];
        tiles = new HashMap<TileType, Tile>();
        tiles.put(TileType.BRICK, new Tile(atlas.cut(32 * TILE_SCALE, 0 * TILE_SCALE, TILE_SCALE, TILE_SCALE), TILE_IN_GAME_SCALE, TileType.BRICK));
        tiles.put(TileType.METAL, new Tile(atlas.cut(32 * TILE_SCALE, 2 * TILE_SCALE, TILE_SCALE, TILE_SCALE), TILE_IN_GAME_SCALE, TileType.METAL));
        tiles.put(TileType.WATER, new Tile(atlas.cut(32 * TILE_SCALE, 4 * TILE_SCALE, TILE_SCALE, TILE_SCALE), TILE_IN_GAME_SCALE, TileType.WATER));
        tiles.put(TileType.GRASS, new Tile(atlas.cut(34 * TILE_SCALE, 4 * TILE_SCALE, TILE_SCALE, TILE_SCALE), TILE_IN_GAME_SCALE, TileType.GRASS));
        tiles.put(TileType.ICE, new Tile(atlas.cut(36 * TILE_SCALE, 4 * TILE_SCALE, TILE_SCALE, TILE_SCALE), TILE_IN_GAME_SCALE, TileType.ICE));
        tiles.put(TileType.EMPTY, new Tile(atlas.cut(36 * TILE_SCALE, 6 * TILE_SCALE, TILE_SCALE, TILE_SCALE), TILE_IN_GAME_SCALE, TileType.EMPTY));
// tileMap = наименование файла
        grassCords = new ArrayList<Point>();
        for (int i = 0; i < tileMap.length; i++) {
            for (int j = 0; i < tileMap.length; j++) {
                Tile tile = tiles.get(TileType.fromNumeric(tileMap[i][j]));
                if (tile.type() == TileType.GRASS) {
//                    tiles.get(TileType.fromNumeric(tileMap[i][j])).render(g, j * SCALED_TILES_SIZE, i * SCALED_TILES_SIZE);//прыгаем по координатам где находит ся след тайл
                    grassCords.add(new Point(j * SCALED_TILES_SIZE, i * SCALED_TILES_SIZE));

                }
            }

        }
    }

    public void update() {

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
}