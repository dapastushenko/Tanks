package server.game;

import server.IO.Input;
import server.game.level.Level;
import server.graphics.Sprite;
import server.graphics.SpriteSheet;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static server.game.PlayerSide.CLIENT;
import static server.game.PlayerSide.SERVER;

public class Player extends Entity implements Serializable {

    public static final int SPRITE_SCALE = 16; //размер изображения 16 пикселей
    public static final int SPRITES_PER_HEADING = 1;


    private Heading heading;
    private transient Map<Heading, Sprite> spriteMap;
    private float scale;
    private float speed;
    private float bulletSpeed;
    private Bullet bullet;

    public PlayerSide side;
    private List<Bullet> bulletList;

    private boolean updated;

    public Player(PlayerSide side, float x, float y, float scale, float speed, Level lvl) {
        super(EntityType.Player, x, y, scale, lvl);

        heading = Heading.NORTH;
        spriteMap = new HashMap<>();
        this.scale = scale;
        this.speed = speed;
        bulletSpeed = 6;
        this.side = side;
        for (Heading h : Heading.values()) {
            SpriteSheet sheet = new SpriteSheet(h.texture(atlas), SPRITES_PER_HEADING, SPRITE_SCALE);
            Sprite sprite = new Sprite(sheet, scale);
            spriteMap.put(h, sprite);
        }
    }

    public void fillSpriteMap() {
//при изменении архитектуры обошлизь без этого метода
        if (spriteMap == null) {
            spriteMap = new HashMap<>();

            for (Heading h : Heading.values()) {
                SpriteSheet sheet = new SpriteSheet(h.texture(atlas), SPRITES_PER_HEADING, SPRITE_SCALE);
                Sprite sprite = new Sprite(sheet, scale);
                spriteMap.put(h, sprite);
            }
        }
    }

    @Override
    public void update(Input input) {
        boolean isSpace = input.getKey(KeyEvent.VK_SPACE);

        if (input.getKey(KeyEvent.VK_UP)) {
            update(Heading.NORTH, isSpace);
        } else if (input.getKey(KeyEvent.VK_RIGHT)) {
            update(Heading.EAST, isSpace);
        } else if (input.getKey(KeyEvent.VK_DOWN)) {
            update(Heading.SOUTH, isSpace);
        } else if (input.getKey(KeyEvent.VK_LEFT)) {
            update(Heading.WEST, isSpace);
        } else if (isSpace)
            update(null, true);

    }

    public synchronized void update(Heading direction, boolean isSpace) {
        float newX = x;
        float newY = y;

        if (direction != null || isSpace)
            updated = true;

        if (direction == Heading.NORTH) {
            newY -= speed;
            heading = Heading.NORTH;
        } else if (direction == Heading.EAST) {
            newX += speed;
            heading = Heading.EAST;
        } else if (direction == Heading.SOUTH) {
            newY += speed;
            heading = Heading.SOUTH;
        } else if (direction == Heading.WEST) {
            newX -= speed;
            heading = Heading.WEST;
        }
        if (isSpace) {
            if (bullet == null || !bullet.isActive()) {
                bullet = new Bullet(x, y, scale, bulletSpeed, heading.toString().substring(0, 4), atlas, lvl, EntityType.Player, side);
            }


//            if (bullet == null || !bullet.isActive()) {
//                if (ServerGame.getBullets(EntityType.Player).size() == 0) {
//                    bullet = new Bullet(x, y, scale, bulletSpeed, heading.toString().substring(0, 4), atlas, lvl,
//                            EntityType.Player);
//                }
//            }
        }

        /*
эта штука не работает
//      System.out.println(newX + " " + newY);
//        input.getKey(KeyEvent.VK_SPACE);
        for (Point p : Level.tilesCords) {
//            System.out.println(p.x+" "+p.y);
            if (newX <= p.x && newY == p.y) {
                System.out.println("1111");
                newX = p.x - SPRITE_SCALE * scale;
                newY = p.y - SPRITE_SCALE * scale;
            }
        }
*/
        if (newX < 0) {
            newX = 0;
        } else if (newX >= ServerGame.WIDTH - SPRITE_SCALE * scale) { //проверяем чтобы не уехать за экран
            newX = ServerGame.WIDTH - SPRITE_SCALE * scale;
        }
        if (newY < 0) {
            newY = 0;
        } else if (newY >= ServerGame.HEIGHT - SPRITE_SCALE * scale) { //проверяем чтобы не уехать за экран
            newY = ServerGame.HEIGHT - SPRITE_SCALE * scale;
        }

        switch (heading) {
            case NORTH:
                if (canMove(newX, newY, newX + (SPRITE_SCALE * scale / 2), newY, newX + (SPRITE_SCALE * scale), newY)) {
                    x = newX;
                    y = newY;
                }
                break;
            case SOUTH:
                if (canMove(newX, newY + (SPRITE_SCALE * scale), newX + (SPRITE_SCALE * scale / 2),
                        newY + (SPRITE_SCALE * scale), newX + (SPRITE_SCALE * scale), newY + (SPRITE_SCALE * scale))
                        ) {
                    x = newX;
                    y = newY;
                }
                break;
            case EAST:
                if (canMove(newX + (SPRITE_SCALE * scale), newY, newX + (SPRITE_SCALE * scale),
                        newY + (SPRITE_SCALE * scale / 2), newX + (SPRITE_SCALE * scale), newY + (SPRITE_SCALE * scale))
                        ) {
                    x = newX;
                    y = newY;
                }
                break;
            case WEST:
                if (canMove(newX, newY, newX, newY + (SPRITE_SCALE * scale / 2), newX, newY + (SPRITE_SCALE * scale))
                        ) {
                    x = newX;
                    y = newY;
                }
                break;
        }
//        x = newX;
//        y = newY;
        if (side == SERVER) {
            bulletList = ServerGame.getBullets(CLIENT);
            if (bulletList.size()>0) {
                for (Bullet clientPlayerBullet : bulletList) {
                    if (getRectangle().intersects(clientPlayerBullet.getRectangle()) && clientPlayerBullet.isActive()) {
                        isAlive = false;
                        clientPlayerBullet.setInactive();
                    }
                }
            }
        } else if (side == CLIENT) {
            bulletList = ServerGame.getBullets(SERVER);
            if (bulletList.size()>0) {
                for (Bullet serverPlayerBullet : bulletList) {
                    if (getRectangle().intersects(serverPlayerBullet.getRectangle())&& serverPlayerBullet.isActive() ) {
                        isAlive = false;
                        serverPlayerBullet.setInactive();
                    }
                }
            }
        }


//        updated = true;
    }

    public synchronized boolean updated() {
        boolean upd = updated;

        updated = false;

        return upd;
    }

    @Override
    public synchronized void render(Graphics2D g) {
        fillSpriteMap();

        //проверка направления стороны и вытаскивание нужной картинки(Sprite)
        spriteMap.get(heading).render(g, x, y);//получаем спрайт

    }

    public PlayerSide getSide() {
        return side;
    }

    @Override
    public void drawExplosion(Graphics2D g) {
        super.drawExplosion(g);
        ServerGame.setGameOver();
    }

    @Override
    public boolean isAlive() {
        return isAlive;
    }

    @Override
    public String toString() {
        return "Player{" +
                "heading=" + heading +
                ", spriteMap=" + spriteMap +
                ", scale=" + scale +
                ", speed=" + speed +
                ", bulletSpeed=" + bulletSpeed +
                ", bullet=" + bullet +
                ", side='" + side + '\'' +
                ", bulletList=" + bulletList +
                ", type=" + type +
                ", x=" + x +
                ", y=" + y +
                ", height=" + height +
                ", width=" + width +
                ", scale=" + scale +
                ", isAlive=" + isAlive +
                '}';
    }
}

