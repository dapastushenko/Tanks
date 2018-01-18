package server.game;

import server.IO.Input;
import server.game.level.Level;
import server.game.level.TileType;
import server.graphics.Sprite;
import server.graphics.SpriteSheet;
import server.graphics.TextureAtlas;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;

import static server.game.Player.SPRITES_PER_HEADING;

public abstract class Entity implements Serializable {

    public final EntityType type;

    //местонахождение объектов
    protected float x;
    protected float y;
    protected float height;
    protected float width;
    protected float scale;
    protected static TextureAtlas atlas = Level.atlas;
    protected transient  static Level lvl;
    public static final int SPRITE_SCALE = 16;
    protected boolean isAlive;

    protected Entity(EntityType type, float x, float y, float scale, Level lvl) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.scale = scale;
        Entity.lvl = lvl;
        isAlive = true;
    }

    public abstract void update(Input input);

    public abstract void render(Graphics2D g);

    public abstract boolean isAlive();

    public Rectangle2D.Float getRectangle() {
        return new Rectangle2D.Float(x, y, SPRITE_SCALE * scale, SPRITE_SCALE * scale);
    }

    protected Rectangle2D.Float getRectangle(float newX, float newY) {
        return new Rectangle2D.Float(newX, newY, SPRITE_SCALE * scale, SPRITE_SCALE * scale);
    }

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

    public void drawExplosion(Graphics2D g) {

        float adjustedX = x - SPRITE_SCALE;
        float adjustedY = y - SPRITE_SCALE;

        SpriteSheet expSheet = new SpriteSheet(
                atlas.cut(19 * SPRITE_SCALE, 8 * SPRITE_SCALE, 2 * SPRITE_SCALE, 2 * SPRITE_SCALE), SPRITES_PER_HEADING,
                2 * SPRITE_SCALE);
        Sprite expSprite = new Sprite(expSheet, scale);
        SpriteSheet bigExpSheet = new SpriteSheet(
                atlas.cut(21 * SPRITE_SCALE, 8 * SPRITE_SCALE, 2 * SPRITE_SCALE, 2 * SPRITE_SCALE), SPRITES_PER_HEADING,
                2 * SPRITE_SCALE);
        Sprite bigExpSprite = new Sprite(bigExpSheet, scale);
        long curTime = System.currentTimeMillis();

        new Thread(new Runnable() {
            @Override
            public void run() {
                long time = System.currentTimeMillis();
                while (time < curTime + 150) {
                    expSprite.render(g, adjustedX, adjustedY);
                    bigExpSprite.render(g, adjustedX, adjustedY);
                    time = System.currentTimeMillis();
                }
            }
        }).start();

    }

    @Override
    public String toString() {
        return "Entity{" +
                "type=" + type +
                ", x=" + x +
                ", y=" + y +
                ", height=" + height +
                ", width=" + width +
                ", scale=" + scale +
                ", isAlive=" + isAlive +
                '}';
    }
}
