package game;

import IO.Input;
import graphics.Sprite;
import graphics.SpriteSheet;
import graphics.TextureAtlas;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class Player extends Entity {

    public static final int SPRITE_SCALE = 16; //размер изображения 16 пикселей
    public static final int SPRITES_PER_HEADING = 1;

    private enum Heading{
        //сторона поворота танка
        NORTH(0 * SPRITE_SCALE, 0 * SPRITE_SCALE,1 * SPRITE_SCALE,1 * SPRITE_SCALE), //0 и 1 это номера танков в атласе
        EAST(6 * SPRITE_SCALE, 0 * SPRITE_SCALE, SPRITE_SCALE, SPRITE_SCALE),
        SOUTH(4 * SPRITE_SCALE, 0 * SPRITE_SCALE, SPRITE_SCALE, SPRITE_SCALE),
        WEST(2 * SPRITE_SCALE, 0 * SPRITE_SCALE, SPRITE_SCALE, SPRITE_SCALE);

        //координаты sprite
        private int x, y, h, w;

        Heading(int x, int y, int h, int w) {
            this.x = x;
            this.y = y;
            this.h = h;
            this.w = w;
        }

        protected BufferedImage texture(TextureAtlas atlas){
            return atlas.cut(x,y,w,h);
        }

    }

    private Heading heading;
    private Map<Heading, Sprite> spriteMap;
    private float scale;
    private float speed;

    protected Player(float x, float y, float scale, float speed, TextureAtlas atlas) {
        super(EntityType.Player, x, y);

        heading = Heading.NORTH;
        spriteMap = new HashMap<Heading,Sprite>();
        this.scale = scale;
        this.speed = speed;

        for (Heading h: Heading.values()){
            SpriteSheet sheet = new SpriteSheet(h.texture(atlas), SPRITES_PER_HEADING, SPRITE_SCALE);
            Sprite sprite = new Sprite(sheet, scale);
            spriteMap.put(h, sprite);
        }
    }

    @Override
    protected void update(Input input) {
        float newX = x;
        float newY = y;

        if(input.getKey(KeyEvent.VK_UP)){
            newY-= speed;
            heading=Heading.NORTH;
        }
        else if (input.getKey(KeyEvent.VK_RIGHT)){
            newX+=speed;
            heading=Heading.EAST;
        }
        else if (input.getKey(KeyEvent.VK_DOWN)){
            newX+=speed;
            heading=Heading.SOUTH;
        }
        else if (input.getKey(KeyEvent.VK_LEFT)){
            newX-=speed;
            heading=Heading.WEST;
        }

        if(newX<0) {
            newX=0;
        }
        else if (newX >= Game.WIDTH - SPRITE_SCALE * scale){ //проверяем чтобы не уехать за экран
            newX = Game.WIDTH - SPRITE_SCALE*scale;
        }
        if(newY<0) {
            newY = 0;
        }
        else if (newY >= Game.HEIGHT - SPRITE_SCALE * scale){ //проверяем чтобы не уехать за экран
            newY = Game.HEIGHT - SPRITE_SCALE * scale;
        }
    }

    @Override
    protected void render(Graphics2D g) {

    }
}