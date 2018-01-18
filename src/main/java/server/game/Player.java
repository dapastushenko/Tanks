package server.game;

import server.IO.Input;
import server.game.level.Level;
import server.graphics.Sprite;
import server.graphics.SpriteSheet;
import server.graphics.TextureAtlas;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Player extends Entity implements Serializable {

    public static final int SPRITE_SCALE = 16; //размер изображения 16 пикселей
    public static final int SPRITES_PER_HEADING = 1;


    private Heading heading;
    transient private Map<Heading, Sprite> spriteMap;
    private float scale;
    private float speed;
    private float bulletSpeed;
    private Bullet bullet;

    public static String playerName;
    private List<Bullet> bulletList;


    public Player(String playerName, float x, float y, float scale, float speed, TextureAtlas atlas, Level lvl) {
        super(EntityType.Player, x, y, atlas, lvl);

        heading = Heading.NORTH;
        spriteMap = new HashMap<>();
        this.scale = scale;
        this.speed = speed;
        bulletSpeed = 6;
        this.playerName = playerName;
        for (Heading h : Heading.values()) {
            SpriteSheet sheet = new SpriteSheet(h.texture(atlas), SPRITES_PER_HEADING, SPRITE_SCALE);
            Sprite sprite = new Sprite(sheet, scale);
            spriteMap.put(h, sprite);
        }
    }

    public void FillSpriteMap() {
//при изменении архитектуры обошлизь без этого метода
        spriteMap = new HashMap<>();

        for (Heading h : Heading.values()) {
            SpriteSheet sheet = new SpriteSheet(h.texture(atlas), SPRITES_PER_HEADING, SPRITE_SCALE);
            Sprite sprite = new Sprite(sheet, scale);
            spriteMap.put(h, sprite);
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
                bullet = new Bullet(x, y, scale, bulletSpeed, heading.toString().substring(0, 4), atlas, lvl, EntityType.Player, playerName);
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
        if (playerName == "serverPlayer") {
            bulletList = ServerGame.getBullets("clientPlayer");
            if (bulletList != null) {
                for (Bullet clientPlayerBullet : bulletList) {
                    if (getRectangle().intersects(clientPlayerBullet.getRectangle()) && clientPlayerBullet.isActive()) {
                        isAlive = false;
                        clientPlayerBullet.setInactive();
                    }
                }
            }
        } else if (playerName == "clientPlayer") {
            bulletList = ServerGame.getBullets("serverPlayer");
            if (bulletList != null) {
                for (Bullet serverPlayerBullet : bulletList) {
                    if (getRectangle().intersects(serverPlayerBullet.getRectangle()) && serverPlayerBullet.isActive()) {
                        isAlive = false;
                        serverPlayerBullet.setInactive();
                    }
                }
            }
        }

    }

    @Override
    public synchronized void render(Graphics2D g) {
        //проверка направления стороны и вытаскивание нужной картинки(Sprite)
        spriteMap.get(heading).render(g, x, y);//получаем спрайт

    }

    public static String getPlayerName() {
        return playerName;
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
}

