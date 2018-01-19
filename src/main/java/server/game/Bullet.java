package server.game;

import server.game.level.Level;
import server.game.level.TileType;
import server.graphics.Sprite;
import server.graphics.SpriteSheet;
import server.graphics.TextureAtlas;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Bullet implements Serializable {
    public enum BulletHeading {
        B_NORTH(20 * Player.SPRITE_SCALE, 6 * Player.SPRITE_SCALE + 4, Player.SPRITE_SCALE / 2,
                1 * Player.SPRITE_SCALE / 2),
        B_EAST(21 * Player.SPRITE_SCALE + Player.SPRITE_SCALE / 2,
                6 * Player.SPRITE_SCALE + 4, Player.SPRITE_SCALE / 2, 1 * Player.SPRITE_SCALE / 2),
        B_SOUTH(
                21 * Player.SPRITE_SCALE, 6 * Player.SPRITE_SCALE + 4, Player.SPRITE_SCALE / 2,
                1 * Player.SPRITE_SCALE / 2),
        B_WEST(20 * Player.SPRITE_SCALE + Player.SPRITE_SCALE / 2,
                6 * Player.SPRITE_SCALE + 4, Player.SPRITE_SCALE / 2,
                1 * Player.SPRITE_SCALE / 2);

        private int x, y, h, w;

        BulletHeading(int x, int y, int h, int w) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }

        protected BufferedImage texture(TextureAtlas atlas) {
            return atlas.cut(x, y, w, h);
        }
    }

    private float speed;
    private transient Map<BulletHeading, Sprite> spriteMap; // todo
    private BulletHeading bulletHeading;
    private float x;
    private float y;
    private float scale;
    private boolean isActive;
    transient private Level lvl;
    private EntityType type;
    private boolean explosionDone;
    private transient List<Sprite> explosionList; // todo
    private int animationCount;
    private String playerName;

    public Bullet(float x, float y, float scale, float speed, String direction, TextureAtlas atlas, Level lvl, EntityType type, PlayerSide side) {
//        spriteMap = new HashMap<BulletHeading, Sprite>();
        this.lvl = lvl;
        isActive = true;
        this.type = type;
        animationCount = 0;
        this.scale = scale;
        this.speed = speed;

        explosionDone = false;
        fillSpriteMap();

        //допилить кусок
//        explosionList = new ArrayList<>();
//        explosionList.add(new Sprite(new SpriteSheet(atlas.cut(16 * Player.SPRITE_SCALE, 8 * Player.SPRITE_SCALE,
//                Player.SPRITE_SCALE, Player.SPRITE_SCALE), Player.SPRITE_SCALE, Player.SPRITE_SCALE),
//                scale));
//        explosionList.add(new Sprite(new SpriteSheet(atlas.cut(17 * Player.SPRITE_SCALE, 8 * Player.SPRITE_SCALE,
//                Player.SPRITE_SCALE, Player.SPRITE_SCALE), Player.SPRITE_SCALE, Player.SPRITE_SCALE),
//                scale));
//        explosionList.add(new Sprite(new SpriteSheet(atlas.cut(18 * Player.SPRITE_SCALE, 8 * Player.SPRITE_SCALE,
//                Player.SPRITE_SCALE, Player.SPRITE_SCALE), Player.SPRITE_SCALE, Player.SPRITE_SCALE),
//                scale));
//
//        for (BulletHeading bh : BulletHeading.values()) {
//            SpriteSheet sheet = new SpriteSheet(bh.texture(atlas), Player.SPRITES_PER_HEADING, Player.SPRITE_SCALE / 2);
//            Sprite sprite = new Sprite(sheet, scale);
//            spriteMap.put(bh, sprite);
//        }

        switch (direction) {
            case "EAST":
                bulletHeading = BulletHeading.B_EAST;
                this.x = x + Player.SPRITE_SCALE * scale / 2;
                this.y = y + (Player.SPRITE_SCALE * scale) / 4;
                break;
            case "NORT":
                bulletHeading = BulletHeading.B_NORTH;
                this.x = x + (Player.SPRITE_SCALE * scale) / 4;
                this.y = y;
                break;
            case "WEST":
                bulletHeading = BulletHeading.B_WEST;
                this.x = x;
                this.y = y + (Player.SPRITE_SCALE * scale) / 4;
                break;
            case "SOUT":
                bulletHeading = BulletHeading.B_SOUTH;
                this.x = x + (Player.SPRITE_SCALE * scale) / 4;
                this.y = y + Player.SPRITE_SCALE * scale / 2;
                break;
        }
        ServerGame.registerBullet(type, this);
        ServerGame.registerBulletinList(side, this);
    }

    public void fillSpriteMap() {
        spriteMap = new HashMap<BulletHeading, Sprite>();
        TextureAtlas atlas = Level.atlas;

        explosionList = new ArrayList<>();
        explosionList.add(new Sprite(new SpriteSheet(atlas.cut(16 * Player.SPRITE_SCALE, 8 * Player.SPRITE_SCALE,
                Player.SPRITE_SCALE, Player.SPRITE_SCALE), Player.SPRITE_SCALE, Player.SPRITE_SCALE),
                scale));
        explosionList.add(new Sprite(new SpriteSheet(atlas.cut(17 * Player.SPRITE_SCALE, 8 * Player.SPRITE_SCALE,
                Player.SPRITE_SCALE, Player.SPRITE_SCALE), Player.SPRITE_SCALE, Player.SPRITE_SCALE),
                scale));
        explosionList.add(new Sprite(new SpriteSheet(atlas.cut(18 * Player.SPRITE_SCALE, 8 * Player.SPRITE_SCALE,
                Player.SPRITE_SCALE, Player.SPRITE_SCALE), Player.SPRITE_SCALE, Player.SPRITE_SCALE),
                scale));

        for (BulletHeading bh : BulletHeading.values()) {
            SpriteSheet sheet = new SpriteSheet(bh.texture(atlas), Player.SPRITES_PER_HEADING, Player.SPRITE_SCALE / 2);
            Sprite sprite = new Sprite(sheet, scale);
            spriteMap.put(bh, sprite);
        }
    }

    public void update() {

            fillSpriteMap();

        switch (bulletHeading) {
            case B_EAST:
                x += speed;
                if (!canFly(x + Player.SPRITE_SCALE * scale / 4, y, x + Player.SPRITE_SCALE * scale / 4,
                        y + Player.SPRITE_SCALE * scale / 4))
                    isActive = false;
                break;
            case B_NORTH:
                y -= speed;
                if (!canFly(x, y, x + Player.SPRITE_SCALE * scale / 4, y))
                    isActive = false;
                break;
            case B_SOUTH:
                y += speed;
                if (!canFly(x, y + Player.SPRITE_SCALE * scale / 4, x + Player.SPRITE_SCALE * scale / 4,
                        y + Player.SPRITE_SCALE * scale / 4))
                    isActive = false;
                break;
            case B_WEST:
                x -= speed;
                if (!canFly(x, y, x, y + Player.SPRITE_SCALE * scale / 4))
                    isActive = false;
                break;
        }


//        if (type == EntityType.Player) {
//            List<Bullet> enemyBullets = server.game.getBullets(EntityType.Player);
//            for (Bullet bullet : enemyBullets)
//                if (getRectangle().intersects(bullet.getRectangle())) {
//                    isActive = false;
//                    bullet.setInactive();
//                    bullet.disableExplosion();
//                    explosionDone = true;
//                }
//
//        }

        if (x < 0 || x >= ServerGame.WIDTH || y < 0 || y > ServerGame.HEIGHT) {
            isActive = false;
        }

    }

    public void render(Graphics2D g, PlayerSide side) {
        if (!isActive && explosionDone) {

            ServerGame.unregisterBullet(type, this, side);
            return;
        }
        if (!isActive)
            drawExplosion(g);
if (spriteMap==null)
    fillSpriteMap();
        if (isActive) {
            spriteMap.get(bulletHeading).render(g, x, y);

        }
    }

    public void drawExplosion(Graphics2D g) {
        if (explosionDone)
            return;
fillSpriteMap();
        float adjustedX = x - Player.SPRITE_SCALE * scale / 4;
        float adjustedY = y - Player.SPRITE_SCALE * scale / 4;

        if (animationCount % 9 < 3)
            explosionList.get(0).render(g, adjustedX, adjustedY);
        else if (animationCount % 9 >= 3 && animationCount % 9 < 6)
            explosionList.get(1).render(g, adjustedX, adjustedY);
        else if (animationCount % 9 > 6)
            explosionList.get(2).render(g, adjustedX, adjustedY);
        animationCount++;

        if (animationCount > 2)
            explosionDone = true;

    }

    private boolean canFly(float startX, float startY, float endX, float endY) {
        int tileStartX = (int) (startX / Level.SCALED_TILES_SIZE);
        int tileStartY = (int) (startY / Level.SCALED_TILES_SIZE);
        int tileEndX = (int) (endX / Level.SCALED_TILES_SIZE);
        int tileEndY = (int) (endY / Level.SCALED_TILES_SIZE);

        Integer[][] tileArray = lvl.getTileMap();

        if (Integer.max(tileStartY, tileEndY) >= tileArray.length
                || Integer.max(tileStartX, tileEndX) >= tileArray[0].length || Integer.min(tileStartY, tileEndY) < 0
                || Integer.min(tileStartX, tileEndX) < 0)
            return false;
        else if (isImpossableTile(tileArray[tileStartY][tileStartX], tileArray[tileEndY][tileEndX])) {

            if (isDestroyableTile(tileArray[tileStartY][tileStartX]))
                lvl.update(tileStartX, tileStartY);

            if (isDestroyableTile(tileArray[tileEndY][tileEndX]))
                lvl.update(tileEndX, tileEndY);

            return false;
        } else
            return true;
    }

    private boolean isDestroyableTile(int tileNum) {
        if (tileNum == TileType.BRICK.numeric()) {
            return true;
        }

        return false;
    }

    private boolean isImpossableTile(Integer... tileNum) {
        for (int i = 0; i < tileNum.length; i++) {
            if (tileNum[i] == TileType.BRICK.numeric() || tileNum[i] == TileType.METAL.numeric()) {
                return true;
            }
        }
        return false;
    }

    public Rectangle2D.Float getRectangle() {
        return new Rectangle2D.Float(x, y, Player.SPRITE_SCALE * scale / 2, Player.SPRITE_SCALE * scale / 2);
    }

    public boolean isActive() {
        return isActive;
    }

    public void setInactive() {
        isActive = false;
    }

    public void disableExplosion() {
        explosionDone = true;
    }
}
