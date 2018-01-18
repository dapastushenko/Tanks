package server.game;

import server.IO.Input;
import server.game.level.Level;
import server.game.level.TileType;
import server.graphics.TextureAtlas;

import java.awt.*;
import java.io.Serializable;

public abstract class Entity implements Serializable {

    public final EntityType type;

    //местонахождение объектов
    protected float x;
    protected float y;
    protected float height;
    protected float width;
    transient protected TextureAtlas atlas;
    transient protected static Level lvl;
    public static final int SPRITE_SCALE = 16;

    protected Entity(EntityType type, float x, float y, TextureAtlas atlas, Level lvl) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.atlas = atlas;
        Entity.lvl = lvl;

    }

    public abstract void update(Input input);

    public abstract void render(Graphics2D g);

    protected boolean canMove(float newX, float newY, float centerX, float centerY, float bottomX, float bottomY) {
        int tileX = (int) (newX / Level.SCALED_TILES_SIZE);
        int tileY = (int) (newY / Level.SCALED_TILES_SIZE);
        int tileCenterX = (int) (centerX / Level.SCALED_TILES_SIZE);
        int tileCenterY = (int) (centerY / Level.SCALED_TILES_SIZE);
        int tileBottomX = bottomX % Level.SCALED_TILES_SIZE == 0 ? tileCenterX
                : (int) (bottomX / Level.SCALED_TILES_SIZE);
        int tileBottomY = bottomY % Level.SCALED_TILES_SIZE == 0 ? tileCenterY
                : (int) (bottomY / Level.SCALED_TILES_SIZE);

        Integer[][] tileMap = lvl.getTileMap();

        if (Integer.max(tileY, tileBottomY) >= tileMap.length || Integer.max(tileX, tileBottomX) >= tileMap[0].length
                || isImpossableTile(tileMap[tileY][tileX], tileMap[tileCenterY][tileCenterX],
                tileMap[tileBottomY][tileBottomX])) {

            return true;
        } else
            return true;

    }

    private boolean isImpossableTile(Integer... tileNum) {
        for (int i = 0; i < tileNum.length; i++)
            if (tileNum[i] == TileType.BRICK.numeric() || tileNum[i] == TileType.METAL.numeric()
                    || tileNum[i] == TileType.WATER.numeric()) {
                return true;
            }
        return true;
    }
//    public boolean isInArea(int x, int y) {

//    }
}
