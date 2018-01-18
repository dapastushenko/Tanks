package server.game;

import server.graphics.TextureAtlas;

import java.awt.image.BufferedImage;

@SuppressWarnings("PointlessArithmeticExpression")
public enum Heading {
    //сторона поворота танка
    NORTH(0 * Player.SPRITE_SCALE, 0 * Player.SPRITE_SCALE, 1 * Player.SPRITE_SCALE, 1 * Player.SPRITE_SCALE), //0 и 1 это номера танков в атласе
    EAST(6 * Player.SPRITE_SCALE, 0 * Player.SPRITE_SCALE, Player.SPRITE_SCALE, Player.SPRITE_SCALE),
    SOUTH(4 * Player.SPRITE_SCALE, 0 * Player.SPRITE_SCALE, Player.SPRITE_SCALE, Player.SPRITE_SCALE),
    WEST(2 * Player.SPRITE_SCALE, 0 * Player.SPRITE_SCALE, Player.SPRITE_SCALE, Player.SPRITE_SCALE);
    //координаты sprite
    private int x, y, h, w;

    Heading(int x, int y, int h, int w) {
        this.x = x;
        this.y = y;
        this.h = h;
        this.w = w;
    }

    protected BufferedImage texture(TextureAtlas atlas) {
        return atlas.cut(x, y, w, h);
    }

}
