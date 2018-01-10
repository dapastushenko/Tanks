package ClientServer.game;

import ClientServer.game.level.Level;
import ClientServer.graphics.Sprite;
import ClientServer.graphics.SpriteSheet;
import ClientServer.graphics.TextureAtlas;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Bullet {
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
    private Map<BulletHeading, Sprite> spriteMap;
    private BulletHeading bulletHeading;
    private float x;
    private float y;
    private float scale;
    private boolean isActive;
    private Level lvl;
    private EntityType type;
    private boolean explosionDone;
    private List<Sprite> explosionList;
    private int animationCount;

    public Bullet(float x, float y, float scale, float speed, String direction, TextureAtlas atlas, Level lvl,
                  EntityType type) {
        spriteMap = new HashMap<BulletHeading, Sprite>();
        this.lvl = lvl;
        isActive = true;
        this.type = type;
        animationCount = 0;
        this.scale = scale;
        this.speed = speed;

        explosionDone = false;
        //допилить кусок
//        explosionList = new ArrayList<>();
//        explosionList.add(new Sprite(new SpriteSheet(atlas.cut(16 * Player.SPRITE_SCALE, 8 * Player.SPRITE_SCALE,
//                Player.SPRITE_SCALE, Player.SPRITE_SCALE), Player.SPRITE_SCALE, Player.SPRITE_SCALE),
//                scale));
//        explosionList.add(new Sprite(new SpriteSheet(atlas.cut(17 * Player.SPRITE_SCALE, 8 * Player.SPRITE_SCALE,
//                Player.SPRITE_SCALE, Player.SPRITE_SCALE), Player.SPRITE_SCALE, Player.SPRITE_SCALE),
//                scale));
 //       explosionList.add(new Sprite(new SpriteSheet(atlas.cut(18 * Player.SPRITE_SCALE, 8 * Player.SPRITE_SCALE,
 //               Player.SPRITE_SCALE, Player.SPRITE_SCALE), Player.SPRITE_SCALE, Player.SPRITE_SCALE),
 //               scale));

        for (BulletHeading bh : BulletHeading.values()) {
            SpriteSheet sheet = new SpriteSheet(bh.texture(atlas), Player.SPRITES_PER_HEADING, Player.SPRITE_SCALE / 2);
            Sprite sprite = new Sprite(sheet, scale);
            spriteMap.put(bh, sprite);
        }

        switch (direction) {
            case "EAST":
                bulletHeading = BulletHeading.B_EAST;
                this.x = x + Player.SPRITE_SCALE * scale / 2;
                this.y = y + (Player.SPRITE_SCALE * scale) / 4;
                break;
            case "NORTH":
                bulletHeading = BulletHeading.B_NORTH;
                this.x = x + (Player.SPRITE_SCALE * scale) / 4;
                this.y = y;
                break;
            case "WEST":
                bulletHeading = BulletHeading.B_WEST;
                this.x = x;
                this.y = y + (Player.SPRITE_SCALE * scale) / 4;
                break;
            case "SOUTH":
                bulletHeading = BulletHeading.B_SOUTH;
                this.x = x + (Player.SPRITE_SCALE * scale) / 4;
                this.y = y + Player.SPRITE_SCALE * scale / 2;
                break;
        }
        Game.registerBullet(type, this);
    }

    public void update() {
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

        if (type == EntityType.Player) {
            List<Bullet> enemyBullets = Game.getBullets(EntityType.Enemy);
            for (Bullet bullet : enemyBullets)
                if (getRectangle().intersects(bullet.getRectangle())) {
                    isActive = false;
                    bullet.setInactive();
                    bullet.disableExplosion();
                    explosionDone = true;
                }

        }

        if (x < 0 || x >= Game.WIDTH || y < 0 || y > Game.HEIGHT) {
            isActive = false;
        }

    }
    public void render(Graphics2D g) {
        if (!isActive && explosionDone) {
            Game.unregisterBullet(type, this);
            return;
        }
        if (!isActive)
            drawExplosion(g);

        if (isActive) {
            spriteMap.get(bulletHeading).render(g, x, y);
        }
    }
    public void drawExplosion(Graphics2D g) {
        if (explosionDone)
            return;

        float adjustedX = x - Player.SPRITE_SCALE * scale / 4;
        float adjustedY = y - Player.SPRITE_SCALE * scale / 4;

        if (animationCount % 9 < 3)
            explosionList.get(0).render(g, adjustedX, adjustedY);
        else if (animationCount % 9 >= 3 && animationCount % 9 < 6)
            explosionList.get(1).render(g, adjustedX, adjustedY);
        else if (animationCount % 9 > 6)
            explosionList.get(2).render(g, adjustedX, adjustedY);
        animationCount++;

        if (animationCount > 12)
            explosionDone = true;

    }

    private boolean canFly(float startX, float startY, float endX, float endY) {
               return true;
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
